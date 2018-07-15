package com.gotako.govoz;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.felipecsl.gifimageview.library.GifImageView;
import com.gotako.govoz.data.Post;
import com.gotako.govoz.data.ThreadDumpObject;
import com.gotako.govoz.data.UrlDrawable;
import com.gotako.govoz.data.WebViewClickHolder;
import com.gotako.govoz.service.ImageDownloadService;
import com.gotako.govoz.tasks.CustomProgressDialog;
import com.gotako.govoz.tasks.DownloadImageTask;
import com.gotako.govoz.tasks.TaskHelper;
import com.gotako.govoz.tasks.VozThreadDownloadTask;
import com.gotako.govoz.utils.CacheUtils;
import com.gotako.govoz.utils.DefaultVozWebClient;
import com.gotako.util.Utils;

import org.sufficientlysecure.htmltextview.HtmlAssetsImageGetter;
import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter;
import org.sufficientlysecure.htmltextview.HtmlResImageGetter;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static com.gotako.govoz.VozConstant.ATTTACHMENT_SIGN;
import static com.gotako.govoz.VozConstant.FORUM_SIGN;
import static com.gotako.govoz.VozConstant.HTTPS_PROTOCOL;
import static com.gotako.govoz.VozConstant.HTTP_PROTOCOL;
import static com.gotako.govoz.VozConstant.THREAD_SIGN;
import static com.gotako.govoz.VozConstant.THREAD_URL_T;
import static com.gotako.govoz.VozConstant.VOZ_LINK;
import static com.gotako.govoz.VozConstant.VOZ_SIGN;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ThreadFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ThreadFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ThreadFragment extends VozFragment implements ActivityCallback<Post>, View.OnLongClickListener, PageNavigationListener {

    private OnFragmentInteractionListener mListener;
    private int mThreadId;
    private List<Post> mPosts;
    private ScrollView listView = null;
    private LayoutInflater viewInflater;
    //private TextView pageNumber;
    private LinearLayout layout;
    private SparseArray<WebView> webViewList;
    private String threadName;
    private String pValue;
    private String replyLink;
    private List<GifImageView> gifImageViews;
    private Boolean threadIsClosed;


    public ThreadFragment() {
        // Required empty public constructor
        mPosts = new ArrayList<>();
        gifImageViews = new ArrayList<>();
        webViewList = new SparseArray<>();
    }

    public static ThreadFragment newInstance() {
        ThreadFragment fragment = new ThreadFragment();
        return fragment;
    }

    @Override
    public void forceRefresh() {
        for (int i = 1; i <= 255; i++) {
            String key = String.valueOf(mThreadId) + "_" + String.valueOf(i);
            VozCache.instance().clearDumpCache(key);
        }
        getThreads();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // processNavigationLink();
    }

    private void processNavigationLink() {
        String threadLink = VozCache.instance().currentNavigateItem().mLink;
        String[] parameters = threadLink.split("\\?")[1].split("\\&");
        String firstParam = parameters[0];
        mThreadId = Integer.parseInt(firstParam.split("=")[1]);
        int threadPage = 1;
        if (parameters.length > 1) {
            threadPage = Integer.parseInt(parameters[1].split("\\=")[1]);
        }
        int currentForumId = VozCache.instance().getCurrentThread();
        if (currentForumId != mThreadId) {
            getThreads(mThreadId, threadPage);
        } else {
            VozCache.instance().currentNavigateItem().mCurrentPage = threadPage;
            getThreads();
        }
    }

    private void getThreads() {
        if (VozCache.instance().currentNavigateItem().mCurrentPage < 1) {
            VozCache.instance().currentNavigateItem().mCurrentPage = 1;
        }
        getThreads(VozCache.instance().getCurrentThread(), VozCache.instance().currentNavigateItem().mCurrentPage);
    }

    private void getThreads(int threadId, int threadPage) {
        mPosts.clear();
        int currentThreadId = VozCache.instance().getCurrentThread();
        int currentThreadPage = VozCache.instance().currentNavigateItem().mCurrentPage;
        if (threadId > 0 && currentThreadId != threadId) {
            VozCache.instance().setCurrentThread(threadId);
            currentThreadId = threadId;
        }

        if (threadPage > 0 && threadPage != currentThreadPage) {
            VozCache.instance().currentNavigateItem().mCurrentPage = threadPage;
            currentThreadPage = threadPage;
        }
        String key = String.valueOf(currentThreadId) + "_" + currentThreadPage;
        Object cacheObject = VozCache.instance().getDataFromCache(key);
        boolean forceReload = VozConfig.instance().isAutoReloadForum();

        if (!forceReload && cacheObject != null) {
            VozThreadDownloadTask task = new VozThreadDownloadTask(this);
            ThreadDumpObject threadDumpObject = (ThreadDumpObject) cacheObject;
            List<Post> mPosts = task.processResult(threadDumpObject.document);
            threadName = threadDumpObject.threadName;
            threadIsClosed = threadDumpObject.closed;
            pValue = threadDumpObject.pValue;
            replyLink = threadDumpObject.replyLink;
            int mLastPage = task.getLastPage();
            processResult(mPosts, mLastPage);
            updateNavigationPanel();
        } else {
            doGetThread(currentThreadId, currentThreadPage);
        }
    }

    private void doGetThread(int currentThreadId, int currentThreadPage) {
        VozThreadDownloadTask task = new VozThreadDownloadTask(this);
        task.setContext(getActivity());
        task.setRetries(1);
        task.setShowProcessDialog(true);
        String url = THREAD_URL_T + String.valueOf(currentThreadId)
                + "&page="
                + String.valueOf(currentThreadPage);
        task.execute(url);
    }

    private void updateNavigationPanel() {
        if (mListener != null) mListener.updateNavigationPanel(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_layout, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void doCallback(List<Post> result, Object... extra) {
        if (result == null || result.size() == 0) {
            String errorMessage = (String) extra[0];
            if (errorMessage != null)
                Toast.makeText(getActivity(),
                        errorMessage,
                        Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.err_cannot_access_forum),
                        Toast.LENGTH_SHORT).show();
        } else {
            threadName = (String) extra[2];
            threadIsClosed = (Boolean) extra[3];
            pValue = (String) extra[4];
            replyLink = (String) extra[5];
            processResult(result, (Integer) extra[1]);
            updateNavigationPanel();
        }
    }

    private void stopAllGifViews() {
        for (GifImageView imageView : gifImageViews) {
            if (imageView != null) imageView.stopAnimation();
        }
    }

    protected void processResult(List<Post> result, int last) {
        mPosts = result;
        VozCache.instance().currentNavigateItem().mLastPage = last;
        if (VozConfig.instance().isPreloadForumsAndThreads()) {
            CacheUtils.preload(getActivity(), VozCache.instance().currentNavigateItem());
        }
        webViewList = new SparseArray<>();
        Handler handler = new Handler();
        handler.post(() -> {
            stopAllGifViews();
            gifImageViews.clear();
            viewInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            layout = (LinearLayout) getView().findViewById(R.id.linearMain);
            layout.removeAllViews();
            LinearLayout postLayout = (LinearLayout) viewInflater.inflate(R.layout.thread_post_item, null);
            ((TextView) postLayout.findViewById(R.id.threadTitle)).setText(threadName);
            layout.addView(postLayout);
            LinearLayout postsPlaceHolder = (LinearLayout) postLayout.findViewById(R.id.linearPosts);
            layout.invalidate();
            for (int i = 0; i < mPosts.size(); i++) {
                View view = viewInflater.inflate(R.layout.neo_post_item, null);
                Post post = mPosts.get(i);
                final WebView webView = (WebView) view.findViewById(R.id.content);
                final HtmlTextView htmlTextView = (HtmlTextView) view.findViewById(R.id.textContent);
                if (post.isComplexStructure()) {
                    webView.getSettings().setJavaScriptEnabled(true);
                    webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
                    webView.getSettings().setPluginState(WebSettings.PluginState.ON);
                    if (webView.isHardwareAccelerated() && VozConfig.instance().isHardwareAccelerated()) {
                        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                    } else {
                        webView.setLayerType(View.LAYER_TYPE_NONE, null);
                    }

                    webView.setDrawingCacheEnabled(false);
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
                        Log.d("DEBUG", "UnsupportedEncodingException", e);
                    }

                    Display display = getActivity().getWindowManager().getDefaultDisplay();
                    DisplayMetrics outMetrics = new DisplayMetrics();
                    display.getMetrics(outMetrics);
                    String css = "body{color: #000; background-color: #FFF;}";
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

                    webView.setBackgroundColor(getResources().getColor(Utils.getValueByTheme(R.color.black, R.color.voz_back_color)));
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
                    if (VozConfig.instance().isUseBackgroundService()) {
                        ImageDownloadService.service().get(i).to(webView, shouldLoadContent);
                    }
                    setListenerToWebView(webView);
                    webView.loadDataWithBaseURL(VOZ_LINK + "/", shouldLoadContent, "text/html", "utf-8", null);
                    webView.invalidate();
                    webViewList.append(i, webView);
                } else {
                    webView.setVisibility(View.GONE);
                    htmlTextView.setVisibility(View.VISIBLE);
                    htmlTextView.setHtml(post.getContent(),
                            new HtmlHttpImageGetter(htmlTextView));
                    htmlTextView.invalidate();
                }
                //ImageView imageView = (ImageView) view.findViewById(R.id.avatar);
                GifImageView avatarView = (GifImageView) view.findViewById(R.id.avatar);
                if (post.getAvatar() != null) {
                    UrlDrawable drawable = new UrlDrawable();
                    drawable.setWidth(75);
                    drawable.setHeight(75);
                    drawable.setDrawable(getResources().getDrawable(R.drawable.user_icon));
                    avatarView.setImageDrawable(drawable);
                    avatarView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    DownloadImageTask task = new DownloadImageTask(drawable,
                            avatarView, getActivity());
                    task.execute(post.getAvatar());
                }
                avatarView.setClickable(false);
                avatarView.setLongClickable(true);
                avatarView.setFocusable(false);
                avatarView.setFocusableInTouchMode(false);
                avatarView.setOnLongClickListener(this);
                avatarView.setTag(i);
                gifImageViews.add(avatarView);
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

                postsPlaceHolder.addView(view);

            }
            if (listView == null) {
                listView = (ScrollView) getView().findViewById(R.id.scrollviewMain);
            }
            listView.fullScroll(ScrollView.FOCUS_UP);
            listView.invalidate();
        });
    }

    private void setListenerToWebView(WebView webView) {

        final Resources resources = getResources();
        webView.setWebViewClient(new DefaultVozWebClient(getActivity()));
        final WebViewClickHolder holder = new WebViewClickHolder();
        final Context thisContext = getActivity();
        final GestureDetector gestureDetector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {

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
                if (!Utils.isNullOrEmpty(url)) ThreadFragment.this.processLink(url);
                return true;
            }

        });

        WebView.OnTouchListener gestureListener = new WebView.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                holder.setWebView((WebView) v);
                WebView.HitTestResult result = holder.getWebView().getHitTestResult();
                if (result != null) {
                    holder.setLink(result.getExtra());
                    holder.setType(result.getType());
                }
                return gestureDetector.onTouchEvent(event);
            }
        };
        webView.setOnTouchListener(gestureListener);

    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    protected void processLink(String url) {
        boolean processed = false;
        String checkedUrl = url;
        if (isVozLink(url)) {
            checkedUrl = rebuiltVozLink(url);
            VozCache.instance().navigationList.add(checkedUrl);
            if (checkedUrl.contains(FORUM_SIGN)) {
                processed = true;
//                Intent intent = new Intent(this, ForumActivity.class);
//                startActivity(intent);
            } else if (checkedUrl.contains(THREAD_SIGN)) {
                String[] params = checkedUrl.split("\\?")[1].split("\\&");
                if (params.length > 1 && params[1].startsWith("page")) {
                    processed = true;
//                    Intent intent = new Intent(this, ThreadActivity.class);
//                    startActivity(intent);
                }
            } else if (checkedUrl.contains("attachment.php")) {
                processed = true;
//                Intent intent = new Intent(this, ShowImageActivity.class);
//                intent.putExtra("IMAGE_URL", checkedUrl);
//                startActivity(intent);
            }
        } else {
            VozCache.instance().navigationList.add(checkedUrl);
        }
        if (!processed) {
            // if http link so open in browser
            if (checkedUrl.startsWith(HTTP_PROTOCOL) || checkedUrl.startsWith(HTTPS_PROTOCOL)) {
//                Intent intent = new Intent(this, BrowserActivity.class);
//                intent.putExtra("link", checkedUrl);
//                startActivity(intent);
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

    @Override
    public void goFirst() {
        VozCache.instance().currentNavigateItem().mCurrentPage = 1;
        getThreads();
    }

    @Override
    public void goLast() {
        VozCache.instance().currentNavigateItem().mCurrentPage = VozCache.instance().currentNavigateItem().mLastPage;
        getThreads();
    }

    @Override
    public void goToPage(int page) {
        VozCache.instance().currentNavigateItem().mCurrentPage = page;
        getThreads();
    }

    @Override
    protected void doRefresh() {
        processNavigationLink();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onPostClicked(String postLink);

        void onThreadClicked(String postLink);

        void onForumClicked(String postLink);

        void onOutsideLinkClicked(String postLink);

        void onOutsidePictureClicked(String postLink);

        void updateNavigationPanel(boolean visible);
    }
}
