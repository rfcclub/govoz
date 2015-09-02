/**
 * 
 */
package com.gotako.govoz.data;

import android.webkit.WebView;

/**
 * @author Nam
 *
 */
public class WebViewClickHolder {
	private int type;
	private String link;
	private WebView webView;
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public WebView getWebView() {
		return webView;
	}
	public void setWebView(WebView webView) {
		this.webView = webView;
	}
}
