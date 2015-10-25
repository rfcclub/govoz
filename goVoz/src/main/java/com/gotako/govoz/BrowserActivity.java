package com.gotako.govoz;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.gotako.util.Utils;

public class BrowserActivity extends VozFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VozCache.instance().setCanShowReplyMenu(false);
        overridePendingTransition(R.animator.right_slide_in,
                R.animator.left_slide_out);
        LayoutInflater mInflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View layout = mInflater.inflate(R.layout.activity_browser, null);
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_container);
        frameLayout.addView(layout);
        overridePendingTransition(R.animator.right_slide_in, R.animator.left_slide_out);

        String link = getIntent().getStringExtra("link");
        if(Utils.isNullOrEmpty(link)) {
            Toast.makeText(this, R.string.error_invalid_link,Toast.LENGTH_SHORT).show();
        } else {
            String realLink = link;
            if(link.startsWith("https://vozforums.com/redirect/index.php?link=")) {
                realLink = link.replace("https://vozforums.com/redirect/index.php?link=","");
                realLink = realLink.replaceAll("%3A",":").replaceAll("%2F", "/").replaceAll("%3F","?").replaceAll("%3D","=");
            }
            Toast.makeText(this, R.string.loading_link,Toast.LENGTH_SHORT).show();
            ((WebView)findViewById(R.id.webView)).loadUrl(realLink);
        }
    }
}
