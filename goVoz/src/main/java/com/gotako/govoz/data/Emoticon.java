package com.gotako.govoz.data;

import android.net.Uri;

public class Emoticon {
    public String url;
    public String code;

    public Emoticon() {}
    public Emoticon(String url, String code) {
        this.url = url;
        this.code = code;
    }
}
