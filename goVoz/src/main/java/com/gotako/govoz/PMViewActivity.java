package com.gotako.govoz;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.gotako.gofast.GoFastEngine;
import com.gotako.gofast.ReactiveCollectionField;
import com.gotako.gofast.annotation.BindingCollection;
import com.gotako.govoz.data.Post;
import com.gotako.govoz.data.PrivateMessage;
import com.gotako.govoz.data.PrivateMessageContent;
import com.gotako.govoz.tasks.PMContentDownloadTask;
import com.gotako.govoz.tasks.PMDownloadTask;
import com.gotako.govoz.tasks.TaskHelper;
import com.gotako.govoz.utils.DefaultVozWebClient;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static com.gotako.govoz.VozConstant.VOZ_LINK;

public class PMViewActivity extends VozFragmentActivity implements
        ActivityCallback<PrivateMessageContent> {

    @BindingCollection(id = R.id.pmContentList, layout = R.layout.pm_content, twoWay = false)
    private List<PrivateMessageContent> pmContentList = new ArrayList<PrivateMessageContent>();
    private int lastPage = 1;
    private LinearLayout layout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VozCache.instance().setCanShowReplyMenu(true);
        overridePendingTransition(R.animator.right_slide_in,
                R.animator.left_slide_out);
        LayoutInflater mInflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View rootLayout = mInflater.inflate(R.layout.activity_pm_view, null);
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_container);
        frameLayout.addView(rootLayout);
        layout = (LinearLayout)rootLayout.findViewById(R.id.postListLayout);

        GoFastEngine.initialize(this);
        ReactiveCollectionField field = GoFastEngine.instance().getBindingObject(this, "pmList", ReactiveCollectionField.class);

        loadPrivateMessageContent();
    }

    private void loadPrivateMessageContent() {
        PMContentDownloadTask task = new PMContentDownloadTask(this);
        String pmContentLink = VozCache.instance().navigationList.get(VozCache.instance().navigationList.size() - 1);
        // load threads for forum
        task.setShowProcessDialog(true);
        task.setContext(this);
        task.execute(pmContentLink);
    }

    @Override
    public void doCallback(List<PrivateMessageContent> result, Object... extra) {
        pmContentList = result;
        layout.removeAllViews();
        LayoutInflater viewInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int i = 0; i < pmContentList.size(); i++) {
            View view = viewInflater.inflate(R.layout.pm_content, null);
            PrivateMessageContent post = pmContentList.get(i);
            ((TextView)view.findViewById(R.id.pmSender)).setText(post.pmSender);
            ((TextView)view.findViewById(R.id.pmDate)).setText(post.pmDate);
            ((TextView)view.findViewById(R.id.pmTitle)).setText(post.pmTitle);

            final WebView webView = (WebView) view.findViewById(R.id.content);
            webView.getSettings().setJavaScriptEnabled(false);
            // disable all click listener in webview
            webView.setClickable(false);
            webView.setLongClickable(true);
            webView.setFocusable(false);
            webView.setFocusableInTouchMode(false);
            webView.getSettings().setDefaultFontSize(VozConfig.instance().getFontSize());
            webView.setTag(i);
            String utfContent = null;
            try {
                utfContent = new String(post.content.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            Display display = getWindowManager().getDefaultDisplay();
            DisplayMetrics outMetrics = new DisplayMetrics();
            display.getMetrics(outMetrics);
            String head = "<head><style type='text/css'>" +
                    "body{color: #fff; background-color: #000;}\n" +
                    "div#permalink_section\n" +
                    "{\n" +
                    "    white-space: pre-wrap; \n" +
                    "    white-space: -moz-pre-wrap;\n" +
                    "    white-space: -pre-wrap; \n" +
                    "    white-space: -o-pre-wrap;\n" +
                    "    word-wrap: break-word;\n" +
                    "}\n" +
                    "</style></head>";
            utfContent = head + "<div style='width="
                    + String.valueOf(outMetrics.widthPixels) + "'>"
                    + utfContent + "</div>";
            post.content = utfContent;
            webView.getSettings().setBuiltInZoomControls(false);
            webView.getSettings().setSupportZoom(false);
            webView.setBackgroundColor(Color.BLACK);
            try {
                TaskHelper.disableSSLCertCheck();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            webView.loadDataWithBaseURL(VOZ_LINK + "/", post.content,"text/html", "utf-8", null);
            webView.setWebViewClient(new DefaultVozWebClient(this));
            layout.addView(view);
        }
    }
}
