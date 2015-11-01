package com.gotako.govoz.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.gotako.govoz.VozConfig;
import com.gotako.govoz.data.UrlDrawable;
import com.gotako.govoz.utils.AsyncCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import static com.gotako.govoz.VozConstant.HTTPS_PROTOCOL;
import static com.gotako.govoz.VozConstant.HTTP_PROTOCOL;
import static com.gotako.govoz.VozConstant.VOZ_LINK;

/**
 * Created by Nam on 10/31/2015.
 */
public class DownloadThreadImageTask extends AsyncTask<String, Void, Drawable> {
    String vozUrl = VOZ_LINK + "/";
    UrlDrawable urlDrawable;
    Context context;
    AsyncCallback<UrlDrawable> callback;

    public DownloadThreadImageTask(UrlDrawable d, Context context, AsyncCallback<UrlDrawable> callback) {
        this.urlDrawable = d;
        this.context = context;
        this.callback = callback;
    }

    @Override
    protected Drawable doInBackground(String... params) {
        String url = params[0];
        try {
            InputStream is = fetch(url);
            Bitmap bm = BitmapFactory.decodeStream(is);
            Drawable drawable = new BitmapDrawable(context.getResources(), bm);
            drawable.setBounds(0, 0, 0 + drawable.getIntrinsicWidth(), 0
                    + drawable.getIntrinsicHeight());
            return drawable;
        } catch (Exception e) {
            Log.d("ERROR", e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(Drawable result) {
        if (result != null) {
            Bitmap bm = ((BitmapDrawable) result).getBitmap();
            if (bm != null) {
                Drawable d = new BitmapDrawable(context.getResources(),
                        Bitmap.createScaledBitmap(bm, result.getIntrinsicWidth(),
                                result.getIntrinsicHeight(), true));
                urlDrawable.setDrawable(result);
            }
        }
        if (callback != null) callback.callback(urlDrawable);
    }

    private InputStream fetch(String urlString) throws MalformedURLException,
            IOException {
        try {
            TaskHelper.disableSSLCertCheck();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        if (!existInCache(urlString)) {
            saveFileToCache(urlString, vozUrl);
        }
        return lookupFileInCache(urlString);
    }

    private void saveFileToCache(String urlString, String baseUrl) {
        Reader reader = null;
        Writer writer = null;
        try {
            String realUrl = urlString;
            if (!realUrl.startsWith(HTTPS_PROTOCOL) && !realUrl.startsWith(HTTP_PROTOCOL)) {
                realUrl = baseUrl + urlString;
            }
            URL aURL = new URL(realUrl);
            URLConnection conn = aURL.openConnection();
            HttpURLConnection httpConn = (HttpURLConnection) conn;
            try {
                //conn.connect();
                httpConn.setRequestMethod("GET");
                httpConn.connect();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream stream = httpConn.getInputStream();
                String savePath = context.getCacheDir() + "/" + urlString;
                Bitmap bm = BitmapFactory.decodeStream(stream);
                File file = new File(savePath.substring(0, savePath.lastIndexOf("/")));
                file.mkdirs();
                FileOutputStream fos = new FileOutputStream(savePath);
                bm.compress(Bitmap.CompressFormat.WEBP, 90, fos);
                fos.close();
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            try {
                if (writer != null)
                    writer.close();
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

    private InputStream lookupFileInCache(String urlString)
            throws FileNotFoundException {
        String savePath = context.getCacheDir() + "/" + urlString;
        File file = new File(savePath);
        return new FileInputStream(file);
    }

    private boolean existInCache(String urlString) {
        String realUrl = context.getCacheDir() + "/" + urlString;
        File file = new File(realUrl);
        return file.isFile() && file.exists();
    }
}
