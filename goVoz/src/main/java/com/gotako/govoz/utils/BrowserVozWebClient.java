package com.gotako.govoz.utils;

import android.content.Context;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import com.gotako.govoz.R;
import com.gotako.govoz.VozConfig;

import static com.gotako.govoz.VozConstant.VOZ_LINK;

/**
 * Created by Nam on 10/25/2015.
 */
public class BrowserVozWebClient  extends DefaultVozWebClient {

    public BrowserVozWebClient(Context context) {
        super(context);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return false;
    }
}
