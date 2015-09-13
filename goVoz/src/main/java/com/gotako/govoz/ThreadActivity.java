package com.gotako.govoz;

import static com.gotako.govoz.VozConstant.THREAD_URL_T;
import static com.gotako.govoz.VozConstant.VOZ_LINK;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.jsoup.nodes.Document;
import org.kobjects.htmlview.HtmlView;
import org.w3c.dom.Text;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.GestureDetector;
import android.view.LayoutInflater;
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
		overridePendingTransition(R.animator.right_slide_in,R.animator.left_slide_out);
		registerMenu();
		posts = new ArrayList<Post>();
		GoFastEngine.initialize(this);
		processNavigationLink();
		updateStatus();
	}

	private void processNavigationLink() {
		// last element of list should be forum link
		String threadLink = VozCache.instance().navigationList.get(VozCache.instance().navigationList.size() - 1);
		String[] parameters = threadLink.split("\\?")[1].split("\\&");
		String firstParam = parameters[0];
		int threadId = Integer.parseInt(firstParam.split("=")[1]);
		int threadPage = Integer.parseInt(parameters[1].split("\\=")[1]);
		int currentForumId = VozCache.instance().getCurrentThread();
		if (currentForumId != threadId) {
            getThreads(threadId, threadPage);
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
        getThreads(VozCache.instance().getCurrentThread(), VozCache.instance().getCurrentThreadPage());
    }
	private void getThreads(int threadId, int threadPage) {
		posts.clear();
		int currentThreadId = VozCache.instance().getCurrentThread();
		int currentThreadPage = VozCache.instance().getCurrentThreadPage();
        if(threadId>0 && currentThreadId != threadId) {
            VozCache.instance().setCurrentThread(threadId);
            currentThreadId = threadId;
        }
        if(threadPage > 0 && threadPage !=currentThreadPage) {
            VozCache.instance().setCurrentThreadPage(threadPage);
            currentThreadPage = threadPage;
        }
		String key = String.valueOf(currentThreadId) + "_" + currentThreadPage;
		Object cacheObject = VozCache.instance().getDataFromCache(key);
		if (cacheObject != null) {
			VozThreadDownloadTask task = new VozThreadDownloadTask(this);
            ThreadDumpObject threadDumpObject = (ThreadDumpObject) cacheObject;
			List<Post> posts = task.processResult(threadDumpObject.document);
            threadName =threadDumpObject.threadName;
            threadIsClosed = threadDumpObject.closed;
            pValue = threadDumpObject.pValue;
            replyLink = threadDumpObject.replyLink;
			int lastPage = task.getLastPage();
			processResult(posts, lastPage);
		} else {
			VozThreadDownloadTask task = new VozThreadDownloadTask(this);
			task.setContext(this);
			task.setRetries(2);
			task.setShowProcessDialog(true);
			String url = THREAD_URL_T + String.valueOf(currentThreadId)
					+ "&page="
					+ String.valueOf(currentThreadPage);
			task.execute(url);
		}
	}

	@Override
	public void doCallback(List<Post> result, Object... extra) {
		if(result == null || result.size() == 0) {			
			Toast.makeText(this, "Cannot access to VozForum. Please try again later.", Toast.LENGTH_SHORT).show();
			return;
		}
        threadName =(String)extra[1];
        threadIsClosed =(Boolean)extra[2];
        pValue = (String)extra[3];
        replyLink =(String) extra[4];
        processResult(result, (Integer) extra[0]);
	}

    private void processResult(List<Post> result,int last) {
        posts = result;
        lastPage = last;
        layout.removeAllViews();
        viewInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        webViewList = new SparseArray<WebView>();
        for (int i = 0; i < posts.size(); i++) {
            View view = viewInflater.inflate(R.layout.post_item, null);

            Post post = posts.get(i);
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
            String head = "<head><style type='text/css'>body{color: #fff; background-color: #000;}</style></head>";
            utfContent = head + "<div style='width="
                    + String.valueOf(outMetrics.widthPixels) + "'>"
                    + utfContent + "</div>";
            post.setContent(utfContent);
            webView.getSettings().setBuiltInZoomControls(false);
            webView.getSettings().setSupportZoom(false);
            webView.setBackgroundColor(Color.BLACK);
            try {
                TaskHelper.disableSSLCertCheck();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            webView.loadDataWithBaseURL(VOZ_LINK + "/", post.getContent(),"text/html", "utf-8", null);
            setListenerToWebView(webView);
//            HtmlView htmlView = (HtmlView)findViewById(R.id.content_html);
//            htmlView.loadHtml(post.getContent());
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
		SegmentedGroup navigationGroup = (SegmentedGroup)findViewById(R.id.navigation_group);
		navigationGroup.removeAllViews();
		LayoutInflater mInflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		int currentPage = VozCache.instance().getCurrentThreadPage();
		if(currentPage > 3) {
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
		if(prevStart < 1) prevStart = 1;
		int extraRight = 0;
		for (int i = prevStart; i <= currentPage; i++) {
			RadioButton prevPage = (RadioButton)mInflater.inflate(R.layout.navigation_button, null);
			prevPage.setText(String.valueOf(i));
			final int page = i;
			if(i == currentPage) prevPage.setChecked(true);
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
		if(nextEnd < 5) nextEnd = 5;
		if(nextEnd > lastPage) nextEnd = lastPage;
		for (int i = currentPage + 1; i <= nextEnd; i++) {
			RadioButton nextPage = (RadioButton)mInflater.inflate(R.layout.navigation_button, null);
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
		if(nextEnd < lastPage) {
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
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
				 handler.proceed();				 
			}
			@Override
			public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
				if(VozConfig.instance().isLoadImageByDemand()) {
					if(url.startsWith(VOZ_LINK)) {
						return super.shouldInterceptRequest(view, url);
					} else if(isImageUrl(url)) {							
							return new WebResourceResponse("image/png", "", resources.openRawResource(R.drawable.no_available_image));								
					} else {
						return null;
					}
				} else {					
					return super.shouldInterceptRequest(view, url);					
				}
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// make sure that link cannot clickable. It will be handled later
				return true;
			}
			
		});
		final WebViewClickHolder holder = new WebViewClickHolder();
		final Context thisContext = ThreadActivity.this;
		final GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
			
			@Override
			public boolean onDoubleTap(MotionEvent e) {		
				if(holder.getType() > 0) {
					// start new view which support view image
					String url = holder.getLink() == null ? "NULL": holder.getLink();
					//Toast.makeText(thisContext, String.valueOf(holder.getType()) + toast,Toast.LENGTH_LONG).show();
					if(isImageUrl(url)) { // image link ?
						Intent intent = new Intent(thisContext, ShowImageActivity.class);						
						intent.putExtra("IMAGE_URL", url);
						thisContext.startActivity(intent);						
					} /*else if(url.indexOf(VOZ_SIGN)>=0) {
						
					}*/
				}
				return true;
			}

			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				String url = holder.getLink() == null ? "NULL": holder.getLink();
				ThreadActivity.this.processLink(url);				
				return super.onSingleTapConfirmed(e);
			}
			
		});

		WebView.OnTouchListener gestureListener = new WebView.OnTouchListener() {
			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {	
				holder.setWebView((WebView)v);
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
		// TODO Auto-generated method stub
		
	}

	protected boolean isImageUrl(String url) {		
		return url.endsWith(".jpg") || url.endsWith(".gif") || url.endsWith(".png") || url.endsWith(".jpeg") || url.endsWith(".bmp");
	}

	public void goFirst() {
		int currPage = VozCache.instance().getCurrentThreadPage();
		if (currPage > 1) {
			VozCache.instance().setCurrentThreadPage(1);
			getThreads();
		}
	}

	public void goToPage(int page) {
		VozCache.instance().setCurrentThreadPage(page);
		refresh();
	}

	public void goLast() {
		int currPage = VozCache.instance().getCurrentThreadPage();
		if (currPage < lastPage) {
			currPage = lastPage;
			VozCache.instance().setCurrentThreadPage(currPage);
			getThreads();
		}
	}

    @Override
    protected void onResume() {
        super.onResume();
        refreshActionBarIcon();
        updateNavigationPanel();
    }

	@Override
	public void onBackPressed() {
        if (VozCache.instance().navigationList.size() > 0)
            VozCache.instance().navigationList.remove(VozCache.instance().navigationList.size() - 1);
        VozCache.instance().setCurrentThread(-1);
		VozCache.instance().setCurrentThreadPage(1);
		/*Intent intent = new Intent(this, ForumActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);*//*
		startActivity(intent);*/
		this.finish();
		overridePendingTransition(R.animator.left_slide_in, R.animator.right_slide_out);
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
		if(VozConfig.instance().isLoadImageByDemand()) {
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
		Post post = posts.get(selectIndex);		
		IgnoreUserTask task = new IgnoreUserTask();
		task.execute(post.getUserId(),post.getUser());
		try {
			String result= task.get();
			refresh();
		} catch (InterruptedException e) {
			
		} catch (ExecutionException e) {
			
		}
	}

	private void viewProfile() {
		// TODO Auto-generated method stub
	}

	private void sendPM() {
		// TODO Auto-generated method stub
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
