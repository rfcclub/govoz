/**
 * 
 */
package com.gotako.govoz.data;

import java.io.Serializable;

/**
 * @author lnguyen66
 *
 */
public class NavDrawerItem implements Serializable {
	public static int FORUM = 1;
	public static int THREAD = 2;
	public String title;
	public String url;
	public int type;
	
	public NavDrawerItem() {}
	
	public NavDrawerItem(String title, String url, int type) {
		this.title = title;
		this.url = url;
		this.type = type;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof NavDrawerItem) {
			if (o != null) {
				NavDrawerItem item = (NavDrawerItem) o;
				if (title == null && item.title == null && url == null && item.url == null) {
					return true;
				}
				if ((title == null && item.title != null) || (title != null && item.title == null)) {
					return false;
				}
				if ((url == null && item.url != null) || (url != null && item.url == null)) {
					return false;
				}
				return title.equals(item.title) && url.equals(item.url);
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (title != null ? title.hashCode() : 10) + (url != null ? url.hashCode() : 20);
	}
}
