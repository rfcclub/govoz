/**
 *
 */
package com.gotako.govoz;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.util.LruCache;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gotako.govoz.data.EmoticonSetObject;
import com.gotako.govoz.tasks.TaskHelper;
import com.gotako.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nam
 */
public class VozConfig {

    private static VozConfig vozConfig = null;

    private boolean imageOptimizer = true;

    /**
     * load image by demand
     */
    private boolean loadImageByDemand;
    /**
     * font size
     */
    private int fontSize = 16;

    /**
     * Auto reload forum when pressing Back
     */
    private boolean autoReloadForum = false;

    /**
     * Support long avatar
     */
    private boolean supportLongAvatar = true;
    /*
     * Using dark theme or not
     */
    private boolean darkTheme = false;
    /*
     * Default loading drawable
     */
    private int loadingDrawable = R.drawable.load278;

    /*
     * Default show sign
     */
    private boolean showSign = true;
    /*
     * should use hardware accelerated in webview or not
     */
    private boolean hardwareAccelerated = true;


    private boolean useBackgroundService = false;

    private boolean preloadForumsAndThreads = true;



    private boolean usingDnsOverVpn;

    private int activeEmoticonSet;
    private List<EmoticonSetObject> emoticonSet;
    /**
     * Default constructor
     */
    private VozConfig() {
        // do nothing
    }

    public static VozConfig instance() {
        if (vozConfig == null) {
            vozConfig = new VozConfig();
        }
        return vozConfig;
    }

    public static List<EmoticonSetObject> getEmoticonSet() {
        return instance().emoticonSet;
    }

    public boolean isLoadImageByDemand() {
        return loadImageByDemand;
    }

    public void setLoadImageByDemand(boolean loadImageByDemand) {
        this.loadImageByDemand = loadImageByDemand;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public boolean isAutoReloadForum() {
        return autoReloadForum;
    }

    public void setAutoReloadForum(boolean autoReloadForum) {
        this.autoReloadForum = autoReloadForum;
    }

    public boolean isSupportLongAvatar() {
        return supportLongAvatar;
    }

    public void setSupportLongAvatar(boolean supportLongAvatar) {
        this.supportLongAvatar = supportLongAvatar;
    }

    public void setPreloadForumsAndThreads(boolean preloadForumsAndThreads) {
        this.preloadForumsAndThreads = preloadForumsAndThreads;
    }

    public void setUsingDnsOverVpn(boolean usingDnsOverVpn) {
        this.usingDnsOverVpn = usingDnsOverVpn;
    }
    public void save(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("VOZCONFIG", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("fontSize", fontSize);
        editor.putBoolean("loadImageByDemand", loadImageByDemand);
        editor.putBoolean("autoReloadForum", autoReloadForum);
        editor.putBoolean("supportLongAvatar", supportLongAvatar);
        editor.putBoolean("darkTheme", darkTheme);
        editor.putInt("loadingDrawable", loadingDrawable);
        editor.putBoolean("showSign", showSign);
        editor.putBoolean("hardwareAccelerated", hardwareAccelerated);
        editor.putBoolean("useBackgroundService",useBackgroundService);
        editor.putBoolean("preloadForumsAndThreads", preloadForumsAndThreads);
        editor.putBoolean("usingDnsOverVpn", usingDnsOverVpn);
        Gson gson = new Gson();
        String jsonString = gson.toJson(emoticonSet);
        editor.putString("emoticonSets", jsonString);
        editor.putInt("activeEmoticonSet", activeEmoticonSet);
        editor.commit();
    }

    public void load(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("VOZCONFIG", Context.MODE_PRIVATE);
        fontSize = prefs.getInt("fontSize", 14);
        loadImageByDemand = prefs.getBoolean("loadImageByDemand", false);
        autoReloadForum = prefs.getBoolean("autoReloadForum", false);
        supportLongAvatar = prefs.getBoolean("supportLongAvatar", true);
        darkTheme = prefs.getBoolean("darkTheme", false);
        loadingDrawable = prefs.getInt("loadingDrawable", R.drawable.load278);
        showSign = prefs.getBoolean("showSign", true);
        hardwareAccelerated = prefs.getBoolean("hardwareAccelerated", true);
        useBackgroundService = prefs.getBoolean("useBackgroundService", false);
        preloadForumsAndThreads = prefs.getBoolean("preloadForumsAndThreads", true);
        usingDnsOverVpn = prefs.getBoolean("usingDnsOverVpn", true);
        activeEmoticonSet = prefs.getInt("activeEmoticonSet", 0);
        String jsonString = prefs.getString("emoticonSets", null);
        Gson gson = new Gson();
        if (Utils.isEmpty(jsonString)) {
            emoticonSet = TaskHelper.createDefaultEmoticonSetList();
            save(context);
        } else {
            emoticonSet = new ArrayList<EmoticonSetObject>();
            emoticonSet = gson.fromJson(jsonString, new TypeToken<List<EmoticonSetObject>>(){}.getType());
            if (emoticonSet.size() > 0 && emoticonSet.get(0) == null) {
                emoticonSet = TaskHelper.createDefaultEmoticonSetList();
                save(context);
            }
        }
    }

    public int getLoadingDrawable() {
        return loadingDrawable;
    }

    public void setLoadingDrawable(int loadingDrawable) {
        this.loadingDrawable = loadingDrawable;
    }

    public boolean isDarkTheme() {
        return darkTheme;
    }

    public void setDarkTheme(boolean darkTheme) {
        this.darkTheme = darkTheme;
    }

    public boolean isShowSign() {
        return showSign;
    }

    public void setShowSign(boolean showSign) {
        this.showSign = showSign;
    }

    public boolean isHardwareAccelerated() {
        return hardwareAccelerated;
    }
    public void setHardwareAccelerated(boolean val) {
        hardwareAccelerated = val;
    }

    public boolean imageOptimizer() {
        return imageOptimizer;
    }

    public void setImageOptimizer(boolean boo) {
        imageOptimizer = boo;
    }

    public boolean isUseBackgroundService() {
        return useBackgroundService;
    }

    public void setUseBackgroundService(boolean useBackgroundService) {
        this.useBackgroundService = useBackgroundService;
    }
    public boolean isPreloadForumsAndThreads() {
        return preloadForumsAndThreads;
    }

    public boolean isUsingDnsOverVpn() {
        return usingDnsOverVpn;
    }


    public int getActiveEmoticonSet() {
        return activeEmoticonSet;
    }

    public void setActiveEmoticonSet(int activeEmoticonSet) {
        this.activeEmoticonSet = activeEmoticonSet;
    }

    public void setEmoticonSet(List<EmoticonSetObject> emoticonSet) {
        this.emoticonSet = emoticonSet;
    }
}
