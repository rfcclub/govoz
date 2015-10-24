package com.gotako.govoz.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.gotako.govoz.R;
import com.gotako.govoz.VozConfig;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import static com.gotako.govoz.VozConstant.VOZ_LINK;

/**
 * Created by Nam on 10/24/2015.
 */
public class DefaultVozWebClient extends WebViewClient {
    private Resources resources;
    Context context;
    public DefaultVozWebClient(Context context) {
        this.context = context;
        this.resources = context.getResources();
    }
    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        handler.proceed();
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        if (VozConfig.instance().isLoadImageByDemand()) {
            if (url.startsWith(VOZ_LINK)) {
                return super.shouldInterceptRequest(view, url);
            } else if (isImageUrl(url)) {
                return new WebResourceResponse("image/png", "", resources.openRawResource(R.drawable.no_available_image));
            } else {
                return null;
            }
        } else {
            if (isAttachmentImage(url)) {
                return getContentFromWeb(url);
            } else {
                return super.shouldInterceptRequest(view, url);
            }
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        // make sure that link cannot clickable. It will be handled later
        return true;
    }


    private WebResourceResponse getContentFromWeb(String url) {
        InputStream content = null;
        try {
            URL imageUrl = new URL(url.replaceAll("&amp;", "&"));
            content = imageUrl.openStream();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new WebResourceResponse("image/jpg", "", content);
    }

    private boolean isAttachmentImage(String url) {
        return url.contains(VOZ_LINK + "/attachment.php?attachmentid=") && url.contains("stc=1&amp;thumb=1");
    }

    protected boolean isImageUrl(String url) {
        return url.endsWith(".jpg") || url.endsWith(".gif") || url.endsWith(".png") || url.endsWith(".jpeg") || url.endsWith(".bmp");
    }
}