package com.gotako.govoz;

import static com.gotako.govoz.VozConstant.THREAD_URL_T;
import static com.gotako.govoz.VozConstant.VOZ_LINK;
import static com.gotako.govoz.VozConstant.VOZ_SIGN;
import static com.gotako.govoz.VozConstant.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.http.impl.client.HttpClients;
import org.jsoup.nodes.Document;
import org.w3c.dom.Text;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;
import com.bugsense.trace.ExceptionCallback;
import com.gotako.gofast.GoFastEngine;
import com.gotako.govoz.data.NavDrawerItem;
import com.gotako.govoz.data.Post;
import com.gotako.govoz.data.ThreadDumpObject;
import com.gotako.govoz.data.UrlDrawable;
import com.gotako.govoz.data.WebViewClickHolder;
import com.gotako.govoz.service.CachePostService;
import com.gotako.govoz.tasks.DownloadImageTask;
import com.gotako.govoz.tasks.GotReplyQuoteTask;
import com.gotako.govoz.tasks.IgnoreUserTask;
import com.gotako.govoz.tasks.TaskHelper;
import com.gotako.govoz.tasks.VozThreadDownloadTask;
import com.gotako.govoz.utils.DefaultVozWebClient;
import com.gotako.util.Utils;

import info.hoang8f.android.segment.SegmentedGroup;

public class ThreadActivity extends VozFragmentActivity implements
        ActivityCallback<Post>, ExceptionCallback, OnLongClickListener,
        GotReplyTaskCallback {
    private List<Post> posts;
    private int lastPage;
    private int selectIndex;
    private ScrollView listView = null;
    private LayoutInflater viewInflater;
    //private TextView pageNumber;
    private LinearLayout layout;
    private SparseArray<WebView> webViewList;
    private String threadName;
    private String pValue;
    private String replyLink;
    private int threadId;
    private int threadPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater mInflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View thread_layout = mInflater.inflate(R.layout.activity_thread, null);
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_container);
        frameLayout.addView(thread_layout);

        listView = (ScrollView) thread_layout.findViewById(R.id.postsList);
        listView.setAnimationCacheEnabled(true);
        listView.setDrawingCacheEnabled(true);
        listView.setAlwaysDrawnWithCacheEnabled(true);
        //pageNumber = (TextView) thread_layout.findViewById(R.id.pageNumber);
        layout = (LinearLayout) listView.findViewById(R.id.postListLayout);
        if (VozCache.instance().getCookies() != null) {
            VozCache.instance().setCanShowReplyMenu(true);
        } else {
            VozCache.instance().setCanShowReplyMenu(false);
        }
        webViewList = new SparseArray<WebView>();
        overridePendingTransition(R.animator.right_slide_in, R.animator.left_slide_out);
        registerMenu();
        posts = new ArrayList<Post>();
        GoFastEngine.initialize(this);
        processNavigationLink();
        updateStatus();
        doTheming();
        SegmentedGroup segmentedGroup = (SegmentedGroup) thread_layout.findViewById(R.id.navigation_group);
        if (segmentedGroup != null) {
            segmentedGroup.setTintColor(Utils.getColorByTheme(this, R.color.white, R.color.voz_front_color),
                    Utils.getColorByTheme(this, R.color.black, R.color.white));
        }
        LinearLayout navigationRootPanel = (LinearLayout) thread_layout.findViewById(R.id.navigationRootPanel);
        if (navigationRootPanel != null)
            navigationRootPanel.setBackgroundColor(Utils.getColorByTheme(this, R.color.black, R.color.voz_back_color));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean ret = super.onCreateOptionsMenu(menu);
        if (VozCache.instance().isLoggedIn()) {
            menu.findItem(R.id.action_reply).setVisible(true);
        }
        return ret;
    }

    private void processNavigationLink() {
        // last element of list should be forum link
        String threadLink = VozCache.instance().navigationList.get(VozCache.instance().navigationList.size() - 1);
        String[] parameters = threadLink.split("\\?")[1].split("\\&");
        String firstParam = parameters[0];
        threadId = Integer.parseInt(firstParam.split("=")[1]);
        threadPage = 1;
        if (parameters.length > 1) {
            threadPage = Integer.parseInt(parameters[1].split("\\=")[1]);
        }
        int currentForumId = VozCache.instance().getCurrentThread();
        if (currentForumId != threadId) {
            getThreads(false, threadId, threadPage);
        } else {
            VozCache.instance().setCurrentThreadPage(threadPage);
            getThreads();
        }
    }

    private void registerMenu() {
        registerForContextMenu(listView);
    }

    private void updateStatus() {
        this.setTitle(threadName);
        updateNavigationPanel();
    }

    private void getThreads() {
        getThreads(false, VozCache.instance().getCurrentThread(), VozCache.instance().getCurrentThreadPage());
    }

    private void getThreads(boolean forceReload, int threadId, int threadPage) {
        posts.clear();
        int currentThreadId = VozCache.instance().getCurrentThread();
        int currentThreadPage = VozCache.instance().getCurrentThreadPage();
        if (threadId > 0 && currentThreadId != threadId) {
            VozCache.instance().setCurrentThread(threadId);
            currentThreadId = threadId;
        }
        if (threadPage > 0 && threadPage != currentThreadPage) {
            VozCache.instance().setCurrentThreadPage(threadPage);
            currentThreadPage = threadPage;
        }
        String key = String.valueOf(currentThreadId) + "_" + currentThreadPage;
        Object cacheObject = VozCache.instance().getDataFromCache(key);
        if (!forceReload && cacheObject != null && currentThreadPage < lastPage) {
            VozThreadDownloadTask task = new VozThreadDownloadTask(this);
            ThreadDumpObject threadDumpObject = (ThreadDumpObject) cacheObject;
            List<Post> posts = task.processResult(threadDumpObject.document);
            threadName = threadDumpObject.threadName;
            threadIsClosed = threadDumpObject.closed;
            pValue = threadDumpObject.pValue;
            replyLink = threadDumpObject.replyLink;
            int lastPage = task.getLastPage();
            processResult(posts, lastPage);
        } else {
            doGetThread(currentThreadId, currentThreadPage);
        }
    }

    private void doGetThread(int currentThreadId, int currentThreadPage) {
        VozThreadDownloadTask task = new VozThreadDownloadTask(this);
        task.setContext(this);
        task.setRetries(1);
        task.setShowProcessDialog(true);
        String url = THREAD_URL_T + String.valueOf(currentThreadId)
                + "&page="
                + String.valueOf(currentThreadPage);
        task.execute(url);
    }

    @Override
    public void doRep() {
        if (threadIsClosed) {
            CharSequence text = "Sorry! This thread is closed!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(this, text, duration);
            toast.show();
        } else { // reply thread
            String httpLink = VOZ_LINK + "/" + replyLink;
            VozCache.instance().navigationList.add(httpLink);
            Intent intent = new Intent(this, PostActivity.class);
            intent.putExtra("threadName", threadName);
            intent.putExtra("replyLink", replyLink);
            startActivityForResult(intent, 1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            getThreads(true, VozCache.instance().getCurrentThread(), VozCache.instance().getCurrentThreadPage());
        }
    }

    @Override
    public void doCallback(List<Post> result, Object... extra) {
        if (result == null || result.size() == 0) {
            String errorMessage = (String) extra[0];
            if (errorMessage != null)
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, getResources().getString(R.string.err_cannot_access_forum), Toast.LENGTH_SHORT).show();
        } else {
            threadName = (String) extra[2];
            threadIsClosed = (Boolean) extra[3];
            pValue = (String) extra[4];
            replyLink = (String) extra[5];
            processResult(result, (Integer) extra[1]);
        }
    }

    private void processResult(List<Post> result, int last) {
        posts = result;
        lastPage = last;
        layout.removeAllViews();
        threadPage = VozCache.instance().getCurrentThreadPage();
        viewInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        webViewList = new SparseArray<WebView>();
        for (int i = 0; i < posts.size(); i++) {
            View view = viewInflater.inflate(R.layout.post_item, null);
            Post post = posts.get(i);
            view.findViewById(R.id.postInfo).setBackgroundColor(Utils.getColorByTheme(this, R.color.black, R.color.voz_back_color));
            view.findViewById(R.id.postInfo).setBackgroundColor(Utils.getColorByTheme(this, R.color.black, R.color.voz_user_panel_color));
            view.findViewById(R.id.separate_line).setBackgroundColor(Utils.getColorByTheme(this, R.color.white, R.color.black));
            view.findViewById(R.id.banner).setBackground(Utils.getDrawableByTheme(this, R.drawable.gradient, R.drawable.gradient_light));
            final WebView webView = (WebView) view.findViewById(R.id.content);
            webView.getSettings().setJavaScriptEnabled(false);
            // disable all click listener in webview
            webView.setClickable(false);
            webView.setLongClickable(true);
            webView.setFocusable(false);
            webView.setFocusableInTouchMode(false);
            webView.setOnLongClickListener(this);
            webView.getSettings().setDefaultFontSize(VozConfig.instance().getFontSize());
            webView.setTag(i); // position
            // webView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
            String utfContent = null;
            try {
                utfContent = new String(post.getContent().getBytes("UTF-8"))
                        .replace("\r", "").replace("\n", "");
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            Display display = getWindowManager().getDefaultDisplay();
            DisplayMetrics outMetrics = new DisplayMetrics();
            display.getMetrics(outMetrics);
            String css = VozConfig.instance().isDarkTheme() ? "body{color: #e7e7e7; background-color: #000;}" : "body{color: #000; background-color: #F5F5F5;}";
            String head = "<head><style type='text/css'>" +
                    css + "\n" +
                    "div#permalink_section\n" +
                    "{\n" +
                    "    white-space: pre-wrap; \n" +
                    "    white-space: -moz-pre-wrap;\n" +
                    "    white-space: -pre-wrap; \n" +
                    "    white-space: -o-pre-wrap;\n" +
                    "    word-wrap: break-word;\n" +
                    "    overflow: hidden;\n" +
                    "}\n" +
                    "</style></head>";
            utfContent = head + "<div style='width="
                    + String.valueOf(outMetrics.widthPixels) + "'>"
                    + utfContent + "</div>";
            post.setContent(utfContent);
            webView.getSettings().setBuiltInZoomControls(false);
            webView.getSettings().setSupportZoom(false);
            webView.setBackgroundColor(getResources().getColor(Utils.getValueByTheme(R.color.black, R.color.white)));
            try {
                TaskHelper.disableSSLCertCheck();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            String shouldLoadContent = post.getContent();
            if (VozConfig.instance().isShowSign()) {
                shouldLoadContent += post.getUserSign() != null ? post.getUserSign() : "";
            }
            webView.loadDataWithBaseURL(VOZ_LINK + "/", shouldLoadContent, "text/html", "utf-8", null);
            setListenerToWebView(webView);
            webViewList.append(i, webView);

            ImageView imageView = (ImageView) view.findViewById(R.id.avatar);
            if (post.getAvatar() != null) {
                UrlDrawable drawable = new UrlDrawable();
                drawable.setWidth(75);
                drawable.setHeight(75);
                //imageView.setImageDrawable(drawable);
                imageView.setScaleType(ScaleType.CENTER_CROP);
                DownloadImageTask task = new DownloadImageTask(drawable,
                        imageView, this);
                task.execute(post.getAvatar());
            }
            imageView.setClickable(false);
            imageView.setLongClickable(true);
            imageView.setFocusable(false);
            imageView.setFocusableInTouchMode(false);
            imageView.setOnLongClickListener(this);
            imageView.setTag(i);

            // post date
            TextView postDate = (TextView) view.findViewById(R.id.postDate);
            postDate.setText(post.getPostDate());

            // post count
            TextView postCount = (TextView) view.findViewById(R.id.postCount);
            postCount.setText(post.getPostCount());

            // user
            TextView user = (TextView) view.findViewById(R.id.user);
            user.setText(post.getUser());

            // join date
            TextView joinDate = (TextView) view.findViewById(R.id.joinDate);
            joinDate.setText(post.getJoinDate());

            // rank
            TextView rank = (TextView) view.findViewById(R.id.rank);
            rank.setText(post.getRank());

            // posted
            TextView posted = (TextView) view.findViewById(R.id.posted);
            posted.setText(post.getPosted());

            // subtitle
            final TextView subTitle = (TextView) view
                    .findViewById(R.id.subTitle);
            subTitle.setText(post.getSubTitle());

            ImageView imageViewHide = (ImageView) view
                    .findViewById(R.id.imageViewHidePost);
            imageViewHide.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Boolean status = (Boolean) v.getTag();
                    if (status != null && status.booleanValue()) {
                        webView.setVisibility(View.GONE);
                        subTitle.setVisibility(View.GONE);
                        ((ImageView) v).setImageResource(R.drawable.ic_arrow_drop_down_white_18dp);
                        v.setTag(Boolean.valueOf(false));
                    } else {
                        webView.setVisibility(View.VISIBLE);
                        subTitle.setVisibility(View.VISIBLE);
                        ((ImageView) v).setImageResource(R.drawable.ic_arrow_drop_up_white_18dp);
                        v.setTag(Boolean.valueOf(true));
                    }
                }
            });
            imageViewHide.setTag(true);
            layout.addView(view);

        }

        updateStatus();
        listView.fullScroll(ScrollView.FOCUS_UP);
    }

    private void updateNavigationPanel() {
        SegmentedGroup navigationGroup = (SegmentedGroup) findViewById(R.id.navigation_group);
        navigationGroup.removeAllViews();
        LayoutInflater mInflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        int currentPage = VozCache.instance().getCurrentThreadPage();
        if (currentPage > 3) {
            RadioButton first = (RadioButton) mInflater.inflate(R.layout.navigation_button, null);
            first.setText("<<");
            first.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goFirst();
                }
            });
            navigationGroup.addView(first);
        }
        int prevStart = currentPage - 2;
        if (prevStart < 1) prevStart = 1;
        int extraRight = 0;
        for (int i = prevStart; i <= currentPage; i++) {
            RadioButton prevPage = (RadioButton) mInflater.inflate(R.layout.navigation_button, null);
            prevPage.setText(String.valueOf(i));
            final int page = i;
            if (i == currentPage) prevPage.setChecked(true);
            prevPage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToPage(page);
                }
            });
            navigationGroup.addView(prevPage);
            extraRight++;
        }
        int nextEnd = currentPage + 2;
        if (nextEnd < 5) nextEnd = 5;
        if (nextEnd > lastPage) nextEnd = lastPage;
        for (int i = currentPage + 1; i <= nextEnd; i++) {
            RadioButton nextPage = (RadioButton) mInflater.inflate(R.layout.navigation_button, null);
            nextPage.setText(String.valueOf(i));
            final int page = i;
            nextPage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToPage(page);
                }
            });
            navigationGroup.addView(nextPage);
        }
        if (nextEnd < lastPage) {
            RadioButton last = (RadioButton) mInflater.inflate(R.layout.navigation_button, null);
            last.setText(">>");
            last.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goLast();
                }
            });
            navigationGroup.addView(last);
        }
        navigationGroup.updateBackground();
        navigationGroup.requestLayout();
        navigationGroup.invalidate();
    }

    private void setListenerToWebView(WebView webView) {

        final Resources resources = getResources();
        final ThreadActivity context = ThreadActivity.this;
        webView.setWebViewClient(new DefaultVozWebClient(this));
        final WebViewClickHolder holder = new WebViewClickHolder();
        final Context thisContext = ThreadActivity.this;
        final GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (holder.getType() > 0) {
                    // start new view which support view image
                    String url = holder.getLink() == null ? "NULL" : holder.getLink();
                    //Toast.makeText(thisContext, String.valueOf(holder.getType()) + toast,Toast.LENGTH_LONG).show();
                    if (isImageUrl(url)) { // image link ?
                        Intent intent = new Intent(thisContext, ShowImageActivity.class);
                        intent.putExtra("IMAGE_URL", url);
                        thisContext.startActivity(intent);
                    } else {

                    }
                }
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                String url = holder.getLink();
                if (!Utils.isNullOrEmpty(url)) ThreadActivity.this.processLink(url);
                return true;
            }

        });

        WebView.OnTouchListener gestureListener = new WebView.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                holder.setWebView((WebView) v);
                HitTestResult result = holder.getWebView().getHitTestResult();
                if (result != null) {
                    holder.setLink(result.getExtra());
                    holder.setType(result.getType());
                }
                return gestureDetector.onTouchEvent(event);
            }
        };
        webView.setOnTouchListener(gestureListener);
    }

    protected void processLink(String url) {
        boolean processed = false;
        String checkedUrl = url;
        if (isVozLink(url)) {
            checkedUrl = rebuiltVozLink(url);
            VozCache.instance().navigationList.add(checkedUrl);
            if (checkedUrl.contains(FORUM_SIGN)) {
                processed = true;
                Intent intent = new Intent(this, ForumActivity.class);
                startActivity(intent);
            } else if (checkedUrl.contains(THREAD_SIGN)) {
                processed = true;
                Intent intent = new Intent(this, ThreadActivity.class);
                startActivity(intent);
            } else if (checkedUrl.contains("attachment.php")) {
                processed = true;
                Intent intent = new Intent(this, ShowImageActivity.class);
                intent.putExtra("IMAGE_URL", checkedUrl);
                startActivity(intent);
            }
        } else {
            VozCache.instance().navigationList.add(checkedUrl);
        }
        if (!processed) {
            // if http link so open in browser
            if (checkedUrl.startsWith(HTTP_PROTOCOL) || checkedUrl.startsWith(HTTPS_PROTOCOL)) {
                Intent intent = new Intent(this, BrowserActivity.class);
                intent.putExtra("link", checkedUrl);
                startActivity(intent);
            }
            // other links are not processed at this moment.
        }
    }

    private String rebuiltVozLink(String url) {
        String processedUrl = url;
        if (processedUrl.startsWith(HTTP_PROTOCOL))
            processedUrl = processedUrl.replace(HTTP_PROTOCOL, HTTPS_PROTOCOL);
        if (processedUrl.startsWith(FORUM_SIGN) || processedUrl.startsWith(THREAD_SIGN) || url.startsWith(ATTTACHMENT_SIGN))
            processedUrl = VOZ_LINK + "/" + processedUrl;
        return processedUrl;
    }

    private boolean isVozLink(String url) {
        return url.contains(VOZ_SIGN) || url.startsWith(FORUM_SIGN) || url.startsWith(THREAD_SIGN) || url.startsWith(ATTTACHMENT_SIGN);
    }

    protected boolean isImageUrl(String url) {
        return url.endsWith(".jpg") || url.endsWith(".gif") || url.endsWith(".png") || url.endsWith(".jpeg") || url.endsWith(".bmp");
    }

    public void goFirst() {
        VozCache.instance().setCurrentThreadPage(1);
        getThreads();
    }

    public void goToPage(int page) {
        VozCache.instance().setCurrentThreadPage(page);
        getThreads();

    }

    public void goLast() {
        VozCache.instance().setCurrentThreadPage(lastPage);
        getThreads();

    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshActionBarIcon();
        updateNavigationPanel();
        VozCache.instance().setCurrentThread(threadId);
        VozCache.instance().setCurrentThreadPage(threadPage);
    }

    @Override
    public void refresh() {
        getThreads();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.post, menu);
        MenuItem showImageMenu = menu.findItem(R.id.show_image);
        if (VozConfig.instance().isLoadImageByDemand()) {
            showImageMenu.setEnabled(true);
        } else {
            showImageMenu.setEnabled(false);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.reply_quote:
                replyQuote();
                break;
            case R.id.send_pm:
                sendPM();
                break;
            case R.id.view_profile:
                viewProfile();
                break;
            case R.id.ignore:
                ignore();
                break;
            case R.id.show_image:
                showImageInWebView();
                break;
            default: // last option

        }
        return true;
    }

    private void showImageInWebView() {
        Post post = posts.get(selectIndex);
        WebView webView = webViewList.get(selectIndex);
        //webView.setWebViewClient(new WebViewClient() {});
        setListenerToWebView(webView);
        webView.loadDataWithBaseURL(VOZ_LINK,
                post.getContent(), "text/html", "utf-8", null);
        webView.refreshDrawableState();
        webView.invalidate();
    }

    private void ignore() {
        final Post post = posts.get(selectIndex);
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.ignore_one_user)
                .setMessage(Utils.getString(this, R.string.really_want_to_ignore) + " " + post.getUser() + "?")
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        IgnoreUserTask task = new IgnoreUserTask();
                        task.execute(post.getUserId(), post.getUser());
                        try {
                            String result = task.get();
                            refresh();
                        } catch (InterruptedException e) {

                        } catch (ExecutionException e) {

                        }
                    }

                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void viewProfile() {
        Toast.makeText(this, Utils.getString(this, R.string.not_implemented_yet), Toast.LENGTH_SHORT).show();
    }

    private void sendPM() {
        String httpLink = VOZ_LINK + "/private.php?do=newpm";
        VozCache.instance().navigationList.add(httpLink);
        Post post = posts.get(selectIndex);
        Intent intent = new Intent(this, CreatePMActivity.class);
        intent.putExtra("pmRecipient", post.getUser() + ";");
        startActivityForResult(intent, 1);
    }

    /**
     *
     */
    private void replyQuote() {
        Post post = posts.get(selectIndex);
        post.getPostId();
        GotReplyQuoteTask task = new GotReplyQuoteTask(this);
        task.setCallback(this);
        task.execute(post.getPostId());
    }

    @Override
    public boolean onLongClick(View view) {
        selectIndex = (Integer) view.getTag();
        listView.showContextMenu();
        return true;
    }

    @Override
    public void workWithQuote(String quote) {
        Intent intent = new Intent(this, PostActivity.class);
        intent.putExtra("quote", quote);
        intent.putExtra("replyLink", replyLink);
        intent.putExtra("threadName", threadName);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, CachePostService.class);
        // bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        /*
         * if (bounded) { unbindService(serviceConnection); } bounded = false;
		 */
    }

    protected boolean canShowPinnedMenu() {
        /*if (navDrawerItems != null) {
            intcurrentThread = VozCache.instance()
					.getCurrentThread();
			String url = VOZ_LINK
					+ currentThread.getThreadUrl()
					+ "&page="
					+ String.valueOf(VozCache.instance().getCurrentThreadPage());
			NavDrawerItem item = new NavDrawerItem(currentThread.getTitle(),
					url, "thread");

			if (navDrawerItems.contains(item)) {
				return true;
			} else {
				return false;
			}
		}*/
        return false;
    }

    protected boolean canShowUnpinnedMenu() {
        /*if (navDrawerItems != null) {
            com.gotako.govoz.data.Thread currentThread = VozCache.instance()
					.getCurrentThread();
			String url = VOZ_LINK
					+ currentThread.getThreadUrl()
					+ "&page="
					+ String.valueOf(VozCache.instance().getCurrentThreadPage());
			NavDrawerItem item = new NavDrawerItem(currentThread.getTitle(),
					url, "thread");

			if (navDrawerItems.contains(item)) {
				return false;
			} else {
				return true;
			}
		}*/
        return true;
    }

    protected void doPin() {
		/*com.gotako.govoz.data.Thread currentThread = VozCache.instance()
				.getCurrentThread();
		String url = VOZ_LINK + currentThread.getThreadUrl()
				+ "&page="
				+ String.valueOf(VozCache.instance().getCurrentThreadPage());
		NavDrawerItem item = new NavDrawerItem(currentThread.getTitle(), url,
				"thread");
		item.tag = currentThread;
		item.page = VozCache.instance().getCurrentThreadPage();

		//pinPage(item);
		pinMenu.setVisible(true);
		unpinMenu.setVisible(false);*/
    }

    protected void doUnpin() {
		/*com.gotako.govoz.data.Thread currentThread = VozCache.instance()
				.getCurrentThread();
		String url = VOZ_LINK + currentThread.getThreadUrl()
				+ "&page="
				+ String.valueOf(VozCache.instance().getCurrentThreadPage());
		NavDrawerItem item = new NavDrawerItem(currentThread.getTitle(), url,
				"thread");
		item.tag = VozCache.instance().getCurrentThread();
		item.page = VozCache.instance().getCurrentThreadPage();

		//unpinPage(item);
		pinMenu.setVisible(false);
		unpinMenu.setVisible(true);*/
    }
}
