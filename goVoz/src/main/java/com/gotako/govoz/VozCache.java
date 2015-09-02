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
	private Forum currentForum;
	private int currentForumPage;
	private Thread currentThread;
	private int lastPage = 1;
	private int currentThreadPage;
	private Map<String,String> cookies;
	private String securityToken ="guest";
	private String userId ="";
	private boolean canShowReplyMenu;
	private Forum currentParentForum = null;
	private int width;
	private int height;	
	private List<NavDrawerItem> pinItemList;	
	private Map<String, Object> forumCache = null;
	
	private VozCache() {
		cookies = null;
		final int cacheSize = 1024 * 1024 * 20;
        memoryCache = new LruCache(cacheSize);
        forumCache = new HashMap<String, Object>();
        pinItemList = new ArrayList<NavDrawerItem>();
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

	public Forum getCurrentForum() {
		return currentForum;
	}

	public void setCurrentForum(Forum currentForum) {
		this.currentForum = currentForum;
	}

	public int getCurrentForumPage() {
		return currentForumPage;
	}

	public void setCurrentForumPage(int currentForumPage) {
		this.currentForumPage = currentForumPage;
	}

	public Thread getCurrentThread() {
		return currentThread;
	}

	public void setCurrentThread(Thread currentThread) {
		this.currentThread = currentThread;
	}

	public int getCurrentThreadPage() {
		return currentThreadPage;
	}

	public void setCurrentThreadPage(int currentThreadPage) {
		this.currentThreadPage = currentThreadPage;
	}

	public void reset() {
		currentThread = null;
		currentThreadPage = 0;
		currentForumPage = 0;
		currentForum = null;
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

	public Forum getCurrentParentForum() {
		return currentParentForum;
	}

	public void setCurrentParentForum(Forum currentParentForum) {
		this.currentParentForum = currentParentForum;
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
