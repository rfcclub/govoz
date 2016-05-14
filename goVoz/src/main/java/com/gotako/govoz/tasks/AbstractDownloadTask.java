package com.gotako.govoz.tasks;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.X509Certificate;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.gotako.govoz.ActivityCallback;
import com.gotako.govoz.VozCache;
import com.gotako.govoz.VozConfig;

public abstract class AbstractDownloadTask<T> extends
        AsyncTask<String, Integer, List<T>> {

    protected ActivityCallback<T> callback;
    protected AlertDialog myAlertDialog;
    protected Context context;
    protected CustomProgressDialog progressDialog;
    protected Dialog callingDialog;
    protected Exception exception;
    protected boolean showProcessDialog;
    protected int retries = 0;
    protected boolean noInternetConnection;

    public AbstractDownloadTask(ActivityCallback<T> callback) {
        this.callback = callback;
    }

    @Override
    protected void onPreExecute() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() == null) {
            noInternetConnection = true;
            return;
        }
        if (showProcessDialog && context != null) {
            progressDialog = new CustomProgressDialog(context, VozConfig.instance().getLoadingDrawable());
            progressDialog.show();
        }
    }

    @Override
    protected List<T> doInBackground(String... params) {
        if (noInternetConnection) return null;
        return doInBackgroundInternal(params);
    }

    protected List<T> doInBackgroundInternal(String[] params) {
        String urlString = params[0];
        List<T> result = new ArrayList<T>();
        boolean completed = false;
        // while (retries >= 0 && !completed) {
        try {
            TaskHelper.disableSSLCertCheck();
            Document document = null;
            if (VozCache.instance().getCookies() == null) {
                document = Jsoup.connect(urlString)
                        .header("Accept-Encoding", "gzip")
                        .timeout(60000)
                        .post();
            } else {
                document = Jsoup.connect(urlString)
                        .timeout(60000)
                        .header("Accept-Encoding", "gzip")
                        .cookies(VozCache.instance().getCookies())
                        .data("securitytoken", VozCache.instance().getSecurityToken())
                        .post();
            }
            result = processResult(document);
            completed = true;
            afterDownload(document);
        } catch (Exception e) {
            //Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("AbstractDownloadTask", e.getMessage(), e);
            processError(e);
        }
        retries -= 1;
        // }
        return result;
    }

    public void afterDownload(Document document) {
        // do nothing. It's the last chance to change download document.
    }

    /**
     * Dummy method for process error. It does nothing
     *
     * @param e error need to process
     */
    protected void processError(Exception e) {
        try {
            if (progressDialog != null) {
                progressDialog.hide();
                progressDialog.dismiss();
                progressDialog = null;
            }
        } catch (Exception e1) {
            Log.e("AbstractDownloadTask", e1.getMessage(), e1);
        }
    }

    public abstract List<T> processResult(Document document);

    protected void suspendDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    protected void onPostExecute(List<T> result) {
        if (noInternetConnection) {
            Toast.makeText(context, "Không có kết nối đến mạng", Toast.LENGTH_SHORT).show();
            return;
        }
        doOnPostExecute(result);
        if (callback != null)
            callback.doCallback(result);

    }

    protected void doOnPostExecute(List<T> result) {
        suspendDialog();
    }

    public void setCallback(ActivityCallback<T> callback) {
        this.callback = callback;
    }

    public ActivityCallback<T> getCallback() {
        return callback;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setShowProcessDialog(boolean showDialog) {
        this.showProcessDialog = showDialog;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }


}
