package com.gotako.govoz.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.gotako.govoz.ActivityCallback;
import com.gotako.govoz.VozConstant;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetSmiliesTask extends AbstractDownloadTask<String> {

    Context context;
    public GetSmiliesTask(Context context, ActivityCallback<String> callback) {
        super(callback);
        this.context = context;
    }
    @Override
    public List processResult(Document document) {
        Map<String, String> smiliesMap = new HashMap<>();
        Elements images = document.select("img");
        if (!images.isEmpty()) {
            for(Element image : images) {
                String source = image.attr("src");
                source = source.substring(source.lastIndexOf("/") + 1);
                String code = (String) image.attr("alt");
                smiliesMap.put(source, code);
                System.out.println("\""+ source + "\",\""+ code + "\"");
            }
        }
        if (smiliesMap.keySet().size() > 0) {
            Gson gson = new Gson();
            String smiliesMapString = gson.toJson(smiliesMap);

            Log.i("GetSmiliesTask", smiliesMapString);
            SharedPreferences prefs = context.getSharedPreferences(VozConstant.VOZINFO, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("VozSmilies", smiliesMapString);
            editor.commit();
        }
        return null;
    }
}
