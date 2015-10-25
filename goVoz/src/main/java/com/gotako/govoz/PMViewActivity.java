package com.gotako.govoz;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import com.gotako.util.Utils;

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
    private String pmReplyLink = "";
    private String pmRecipient = "";
    private String pmQuote = "";
    private String securityToken;
    private String loggedInUser;
    private String pmTitle = null;

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
        doTheming();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean ret = super.onCreateOptionsMenu(menu);
        if (VozCache.instance().isLoggedIn()) {
            menu.findItem(R.id.action_reply).setVisible(true);
        }
        return ret;
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
        pmReplyLink = (String)extra[0];
        pmRecipient = (String)extra[1];
        pmQuote = (String)extra[2];
        securityToken = (String)extra[3];
        loggedInUser = (String) extra[4];

        layout.removeAllViews();
        LayoutInflater viewInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int i = 0; i < pmContentList.size(); i++) {
            View view = viewInflater.inflate(R.layout.pm_content, null);
            view.findViewById(R.id.postInfo).setBackgroundColor(Utils.getColorByTheme(this, R.color.black, R.color.voz_back_color));
            PrivateMessageContent post = pmContentList.get(i);
            ((TextView)view.findViewById(R.id.pmSender)).setText(post.pmSender);
            ((TextView)view.findViewById(R.id.pmDate)).setText(post.pmDate);
            ((TextView)view.findViewById(R.id.pmTitle)).setText(post.pmTitle);
            if(pmTitle == null) pmTitle = "Re: " + post.pmTitle;
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
            String css = VozConfig.instance().isDarkTheme()? "body{color: #fff; background-color: #000;}" :"body{color: #000; background-color: #F5F5F5;}";
            Display display = getWindowManager().getDefaultDisplay();
            DisplayMetrics outMetrics = new DisplayMetrics();
            display.getMetrics(outMetrics);
            String head = "<head><style type='text/css'>" +
                    css + "\n" +
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

    @Override
    public void doRep() {
        String httpLink = VOZ_LINK + "/private.php?do=newpm";
        VozCache.instance().navigationList.add(httpLink);
        Intent intent = new Intent(this, CreatePMActivity.class);
        intent.putExtra("pmReplyLink",pmReplyLink);
        intent.putExtra("pmRecipient",pmRecipient);
        intent.putExtra("pmQuote",pmQuote);
        intent.putExtra("securityToken",securityToken);
        intent.putExtra("loggedInUser", loggedInUser);
        intent.putExtra("pmTitle", pmTitle);
        startActivityForResult(intent, 2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2) {
            refresh();
        }
    }

    @Override
    public void refresh() {
        loadPrivateMessageContent();
    }
}
