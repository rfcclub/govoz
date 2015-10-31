/**
 *
 */
package com.gotako.govoz;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.util.LruCache;

/**
 * @author Nam
 */
public class VozConfig {

    private static VozConfig vozConfig = null;

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
    private boolean autoReloadForum = true;

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
    private boolean hardwareAccelerated;


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
        editor.commit();
    }

    public void load(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("VOZCONFIG", Context.MODE_PRIVATE);
        fontSize = prefs.getInt("fontSize", 14);
        loadImageByDemand = prefs.getBoolean("loadImageByDemand", false);
        autoReloadForum = prefs.getBoolean("autoReloadForum", true);
        supportLongAvatar = prefs.getBoolean("supportLongAvatar", true);
        darkTheme = prefs.getBoolean("darkTheme", false);
        loadingDrawable = prefs.getInt("loadingDrawable", R.drawable.load278);
        showSign = prefs.getBoolean("showSign", true);
        hardwareAccelerated = prefs.getBoolean("hardwareAccelerated", false);
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
}
