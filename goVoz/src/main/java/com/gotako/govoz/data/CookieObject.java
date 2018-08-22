package com.gotako.govoz.data;

import java.util.Map;

public class CookieObject {
    public Map<String, String> mCookies;
    public CookieObject(Map<String, String> mCookies) {
        this.mCookies = mCookies;
    }

    public CookieObject() {
    }

    public Map<String, String> getCookies() {
        return mCookies;
    }

    public void setCookies(Map<String, String> mCookies) {
        this.mCookies = mCookies;
    }
}
