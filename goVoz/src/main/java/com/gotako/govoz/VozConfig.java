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
    private boolean darkTheme = true;
    /*
     * Default loading drawable
     */
    private int loadingDrawable = R.drawable.load278;

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
        prefs.edit().putInt("fontSize", fontSize);
        prefs.edit().putBoolean("loadImageByDemand", loadImageByDemand);
        prefs.edit().putBoolean("autoReloadForum", autoReloadForum);
        prefs.edit().putBoolean("supportLongAvatar", supportLongAvatar);
    }

    public void load(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("VOZCONFIG", Context.MODE_PRIVATE);
        if (prefs.contains("fontSize")) {
            fontSize = prefs.getInt("fontSize", 16);
        }
        if (prefs.contains("loadImageByDemand")) {
            loadImageByDemand = prefs.getBoolean("loadImageByDemand", false);
        }
        if (prefs.contains("autoReloadForum")) {
            autoReloadForum = prefs.getBoolean("autoReloadForum", true);
        }
        if (prefs.contains("supportLongAvatar")) {
            supportLongAvatar = prefs.getBoolean("supportLongAvatar", true);
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
}
