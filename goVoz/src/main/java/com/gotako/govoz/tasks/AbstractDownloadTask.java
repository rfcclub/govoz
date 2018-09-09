package com.gotako.govoz.tasks;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.gotako.govoz.ActivityCallback;
import com.gotako.govoz.AutoLoginBackgroundService;
import com.gotako.govoz.CallbackResult;
import com.gotako.govoz.VozCache;
import com.gotako.govoz.VozConfig;
import com.gotako.util.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDownloadTask<T> extends
        AsyncTask<String, Integer, List<T>> {

    private static final String TOKEN_EXPIRED = "Your submission could not be processed because the token has expired.";
    protected ActivityCallback<T> callback;
    protected AlertDialog myAlertDialog;
    protected Context mContext;
    protected CustomProgressDialog progressDialog;
    protected Dialog callingDialog;
    protected Exception mException;
    protected boolean showProcessDialog;
    protected int mRetries = 0;
    protected boolean mNoInternetConnection;
    protected boolean hasError = false;
    protected Exception exception = null;
    protected boolean sessionExpired;

    public AbstractDownloadTask(ActivityCallback<T> callback) {
        this.callback = callback;
    }

    @Override
    protected void onPreExecute() {
        if (mContext != null) { // if it has context so check connectivity
            ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm.getActiveNetworkInfo() == null) {
                mNoInternetConnection = true;
                return;
            }
        }
        if (showProcessDialog && mContext != null) {
            progressDialog = new CustomProgressDialog(mContext, VozConfig.instance().getLoadingDrawable());
            progressDialog.show();
        }
    }

    @Override
    protected List<T> doInBackground(String... params) {
        if (mNoInternetConnection) return null;
        return doInBackgroundInternal(params);
    }

    protected List<T> doInBackgroundInternal(String[] params) {
        String urlString = params[0];
        List<T> result = new ArrayList<T>();
        boolean completed = false;
        // while (mRetries >= 0 && !completed) {
        try {
            TaskHelper.disableSSLCertCheck();
            Document document = null;
            long startMillis = System.currentTimeMillis();
            if (VozCache.instance().getCookies() == null) {
                document = Jsoup.connect(urlString)
                        .header("Accept-Encoding", "gzip, deflate")
                        .maxBodySize(0)
                        .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                        .timeout(60000)
                        .post();
            } else {
                document = Jsoup.connect(urlString)
                        .timeout(60000)
                        .header("Accept-Encoding", "gzip, deflate")
                        .maxBodySize(0)
                        .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                        .cookies(VozCache.instance().getCookies())
                        .data("securitytoken", VozCache.instance().getSecurityToken())
                        .post();
            }
            Log.d("AbstractDownloadTask","Page load in: " + (System.currentTimeMillis() - startMillis) / 1000);
            startMillis = System.currentTimeMillis();
            checkError(document);
            result = processResult(document);
            Log.d("AbstractDownloadTask","Processed in: " + (System.currentTimeMillis() - startMillis) / 1000);
            completed = true;
            afterDownload(document, params);
        } catch (Exception e) {
            //Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("AbstractDownloadTask", e.getMessage(), e);
            processError(e);
        }
        mRetries -= 1;
        // }

        return result;
    }

    private void checkError(Document document) throws Exception {
        Elements elements = document.select("table[class=tborder][cellpadding=6][cellspacing=1][border=0][align=center]");
        if (elements != null && elements.size() > 0) {
            Element errorElement = null;
            for (Element element : elements) {
                if ("vBulletin Message".equals(Utils.getFirstText(element.select("td")))) {
                    errorElement = element;
                    break;
                }
            }
            if (errorElement != null) {
                String errorMsg = Utils.getFirstText(errorElement.select("p"));
                throw new Exception(errorMsg);
            }
        }
    }

    public void afterDownload(Document document, String... params) {
        // do nothing. It's the last chance to change download document.
    }

    /**
     * Dummy method for process error. It does nothing
     *
     * @param e error need to process
     */
    protected void processError(Exception e) {
        try {
            if (sessionExpired(e)) {
                sessionExpired = true;
            }
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        } finally {
            hasError = true;
            exception = e;
        }
    }

    private boolean sessionExpired(Exception e) {
        if (e.getMessage() != null && (e.getMessage().startsWith(TOKEN_EXPIRED))) {
            return true;
        } else {
            return false;
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
        if (mNoInternetConnection) {
            Toast.makeText(mContext, "Không có kết nối đến mạng", Toast.LENGTH_SHORT).show();
            return;
        }
        doOnPostExecute(result);
        if (callback != null) {
            String errorMessage = null;
            if (hasError && exception != null) {
                errorMessage = exception.getMessage();
            }
            CallbackResult<T> callbackResult = new CallbackResult.Builder<T>()
            .setResult(result)
            .setSessionExpire(sessionExpired)
            .setError(errorMessage != null).build();
            callback.doCallback(callbackResult);
        }

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
        this.mContext = context;
    }

    public void setShowProcessDialog(boolean showDialog) {
        this.showProcessDialog = showDialog;
    }

    public void setRetries(int retries) {
        this.mRetries = retries;
    }


}
