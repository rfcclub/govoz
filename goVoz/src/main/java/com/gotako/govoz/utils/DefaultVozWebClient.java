package com.gotako.govoz.utils;

import android.content.Context;
import android.content.res.Resources;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.gotako.govoz.R;
import com.gotako.govoz.VozCache;
import com.gotako.govoz.VozConfig;
import com.gotako.govoz.tasks.TaskHelper;
import com.gotako.util.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.net.ssl.HttpsURLConnection;

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
        if(error.hasError(SslError.SSL_IDMISMATCH) || error.hasError(SslError.SSL_NOTYETVALID)) {
            handler.cancel();
        } else {
            handler.proceed();
        }
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
                return getAttachmentFromVoz(url);
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


    private WebResourceResponse getAttachmentFromVoz(String url) {
        InputStream content = null;
        String encoding = "";
        try {
            TaskHelper.disableSSLCertCheck();
            URL imageUrl = new URL(url.replaceAll("&amp;", "&"));
            HttpsURLConnection conn = (HttpsURLConnection) imageUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Cookie", Utils.flatMap(VozCache.instance().getCookies()));
            conn.connect();
            if(conn.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                encoding = conn.getContentEncoding();
                content = conn.getInputStream();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        return new WebResourceResponse("image/*", encoding, content);
    }

    private boolean isAttachmentImage(String url) {
        return url.contains(VOZ_LINK + "/attachment.php?attachmentid=");
    }

    protected boolean isImageUrl(String url) {
        return url.endsWith(".jpg") || url.endsWith(".gif") || url.endsWith(".png") || url.endsWith(".jpeg") || url.endsWith(".bmp");
    }
}