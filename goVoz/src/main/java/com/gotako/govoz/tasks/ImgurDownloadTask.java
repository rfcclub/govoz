package com.gotako.govoz.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.gotako.govoz.ActivityCallback;
import com.gotako.govoz.CallbackResult;
import com.gotako.govoz.data.ImgurImage;
import com.gotako.govoz.data.ImgurImageAlbum;
import com.gotako.govoz.service.ImgurModule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImgurDownloadTask extends AsyncTask<String, Integer, List<ImgurImage>> {
    protected ActivityCallback<ImgurImage> callback;
    public ImgurDownloadTask(ActivityCallback<ImgurImage> activityCallback) {
        callback = activityCallback;
    }
    @Override
    protected List<ImgurImage> doInBackground(String... strings) {
        List<ImgurImage> list = new ArrayList<>();
        try {
            String link = strings[0];
            if (link.startsWith("http")) link = link.substring(link.lastIndexOf('/') + 1);
             ImgurImageAlbum album = ImgurModule.getImgurService().getAlbumImages(link).execute().body();
            list = album.data;
        } catch(IOException ex) {
            Log.e("VozSmiliesActivity", ex.getMessage());
        } finally {
            // do nothing
        }
        return list;
    }

    @Override
    protected void onPostExecute(List<ImgurImage> imgurImages) {
        CallbackResult<ImgurImage> cb = new CallbackResult.Builder<ImgurImage>()
                .setResult(imgurImages)
                .build();

        if (callback != null) callback.doCallback(cb);
    }
}
