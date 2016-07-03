package com.gotako.govoz.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.Log;
import android.webkit.WebView;

import com.gotako.govoz.VozConstant;
import com.gotako.util.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

/**
 * Created by namtu on 7/3/16.
 */

public class DownloadBatch {
    private WebView webView;
    private String content;
    private Queue<String> urls;
    private Context ctx;

    public DownloadBatch(Context context) {
        urls = new ArrayBlockingQueue<String>(50);
        ctx = context;
    }

    public DownloadBatch add(String url) {
        urls.add(url);
        return this;
    }

    public DownloadBatch to(WebView webView, String content) {
        this.webView = webView;
        this.content = content;
        return this;
    }

    public void trigger() throws InterruptedException {
        if (!ready()) return;

        final CountDownLatch countDownLatch = new CountDownLatch(urls.size());
        for (int i = 0; i < 2; i++) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (urls.size() > 0) {
                        String url = urls.poll();
                        download(url);
                        countDownLatch.countDown();
                    }
                }

                private void download(String url) {
                    OkHttpClient client = new OkHttpClient();
                    Request request = null;
                    Response response = null;
                    try {
                        request = new Request.Builder().url(url)
                                .addHeader("Content-Type", Utils.getContentType(url))
                                .build();
                        response = client.newCall(request).execute();

                        String savePath = ctx.getCacheDir() + Utils.getPath(url);
                        File file = new File(savePath.substring(0, savePath.lastIndexOf("/")));
                        file.mkdirs();
                        BufferedSink sink = Okio.buffer(Okio.sink(new File(savePath)));
                        sink.writeAll(response.body().source());
                        sink.close();
                        // optimize(savePath);
                    } catch (Exception ex) {
                        Log.e("AVATAR_ERROR", ex.getMessage());
                    }
                }


            });
            thread.start();
        }
        countDownLatch.await();
        webView.loadDataWithBaseURL(VozConstant.VOZ_LINK, content, "text/html", "utf-8", null);
    }

    private void optimize(final String savePath) throws Exception {
        // if it's GIF do nothing
        if(savePath.toLowerCase().endsWith("gif")) return;
        InputStream is = new FileInputStream(savePath);
        Bitmap bm = BitmapFactory.decodeStream(is);
        is.close();
        DisplayMetrics displayMetrics = ctx.getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        FileOutputStream fos = new FileOutputStream(savePath);
        bm.compress(Bitmap.CompressFormat.WEBP, 90, fos);
        fos.close();
    }

    private boolean ready() {
        return webView != null && urls.size() > 0;
    }

}
