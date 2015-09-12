/**
 * 
 */
package com.gotako.govoz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Document;

import android.support.v4.util.LruCache;

import com.gotako.govoz.data.Forum;
import com.gotako.govoz.data.NavDrawerItem;
import com.gotako.govoz.data.Thread;
import com.gotako.govoz.data.VozMenuItem;

/**
 * Cache contains current state of application
 * 
 * @author Nam
 * 
 */
public class VozCache {
	
	public static final int LOOK_AHEAD_NUMBER = 2;
	
	private LruCache memoryCache;
	
	private static VozCache vozCache = null;
	private int currentForumId;
	private int currentForumPage;
	private int currentThreadId;
	private int lastPage = 1;
	private int currentThreadPage;
	private Map<String,String> cookies;
	private String securityToken ="guest";
	private String userId ="";
	private boolean canShowReplyMenu;
	private int currentParentForumId = -1;
	private int width;
	private int height;
	public List<NavDrawerItem> pinItemList;
	public List<VozMenuItem> menuItemList;
	private Map<String, Object> forumCache = null;
	public List<String> navigationList;
	
	private VozCache() {
		cookies = null;
		final int cacheSize = 1024 * 1024 * 20;
        memoryCache = new LruCache(cacheSize);
        forumCache = new HashMap<String, Object>();
        pinItemList = new ArrayList<NavDrawerItem>();
        navigationList = new ArrayList<String>();
	}

	public static VozCache instance() {
		if (vozCache == null) {
			vozCache = new VozCache();
		}
		return vozCache;
	}

	public Object getDataFromCache(String key) {
		return memoryCache.get(key);
	}
	
	public void putDataToCache(String key, Object value) {
		memoryCache.put(key, value);
	}
	
	public VozCache getVozCache() {
		return vozCache;
	}

	public static void setVozCache(VozCache vozCache) {
		VozCache.vozCache = vozCache;
	}

	public int getCurrentForum() {
		return currentForumId;
	}

	public void setCurrentForum(int currentForum) {
		this.currentForumId = currentForum;
	}

	public int getCurrentForumPage() {
		return currentForumPage;
	}

	public void setCurrentForumPage(int currentForumPage) {
		this.currentForumPage = currentForumPage;
	}

	public int getCurrentThread() {
		return currentThreadId;
	}

	public void setCurrentThread(int currentThread) {
		this.currentThreadId = currentThread;
	}

	public int getCurrentThreadPage() {
		return currentThreadPage;
	}

	public void setCurrentThreadPage(int currentThreadPage) {
		this.currentThreadPage = currentThreadPage;
	}

	public void reset() {
		currentThreadId = -1;
		currentThreadPage = 0;
		currentForumPage = 0;
		currentForumId = -1;
	}

	public Map<String, String> getCookies() {
		return cookies;
	}

	public void setCookies(Map<String, String> cookies) {
		this.cookies = cookies;
	}

	public String getSecurityToken() {
		return securityToken;
	}

	public void setSecurityToken(String securityToken) {
		this.securityToken = securityToken;
	}

	public String getUserId() {		
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public boolean canShowReplyMenu() {
		return canShowReplyMenu;
	}

	public void setCanShowReplyMenu(boolean canShowReplyMenu) {
		this.canShowReplyMenu = canShowReplyMenu;
	}

	public int getCurrentParentForum() {
		return currentParentForumId;
	}

	public void setCurrentParentForum(int currentParentForum) {
		this.currentParentForumId = currentParentForum;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getLastPage() {
		return lastPage;
	}

	public void setLastPage(int lastPage) {
		this.lastPage = lastPage;
	}

	public List<NavDrawerItem> getPinItemList() {
		return pinItemList;
	}

	public void setPinItemList(List<NavDrawerItem> pinItemList) {
		this.pinItemList = pinItemList;
	}

	public Map<String, Object> cache() {
		return forumCache;
	}
}
