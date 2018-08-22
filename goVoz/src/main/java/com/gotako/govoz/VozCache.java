/**
 * 
 */
package com.gotako.govoz;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.util.LruCache;

import com.google.gson.Gson;
import com.gotako.govoz.data.CookieObject;
import com.gotako.govoz.data.NavDrawerItem;
import com.gotako.govoz.data.VozMenuItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cache contains current state of application
 * 
 * @author Nam
 * 
 */
public class VozCache {
	
	public static final int LOOK_AHEAD_NUMBER = 1;
    public static final String GUEST = "guest";
	public static final String ANONYMOUS = "Anonymous";

    private LruCache mMemoryCache;
	
	private static VozCache vozCache = null;
	private int mCurrentForumId;
	private int mCurrentForumPage;
	private int mCurrentThreadId;
	private int mLastPage = 1;
	private int mCurrentThreadPage;
	private Map<String,String> mCookies;
	private String mSecurityToken = GUEST;
	private String mUserId ="";
	private boolean mCanShowReplyMenu;
	private int mCurrentParentForumId = -1;
	private int mWidth;
	private int mHeight;
	public List<NavDrawerItem> pinItemThreadList;
	public List<NavDrawerItem> pinItemForumList;
	public List<VozMenuItem> menuItemList;
	private Map<String, Object> mForumCache = null;
	public Map<String, CookieObject> mUserCookies;
	public List<String> navigationList;
	public List<NavigationItem> mNeoNavigationList;
	public long milliSeconds;

	private VozCache() {
		mCookies = null;
		final int cacheSize = 1024 * 1024 * 20;
        mMemoryCache = new LruCache(cacheSize);
        mForumCache = new HashMap<>();
		pinItemThreadList = new ArrayList<>();
		pinItemForumList = new ArrayList<>();
        navigationList = new ArrayList<>();
		mNeoNavigationList = new ArrayList<>();
		mUserCookies = new HashMap<>();
	}

	public static VozCache instance() {
		if (vozCache == null) {
			vozCache = new VozCache();
		}
		return vozCache;
	}

	public LruCache getDumpCache() {
		return mMemoryCache;
	}
	public boolean isLoggedIn() {
		return mCookies != null;
	}
    public boolean isSavedPreference(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(VozConstant.VOZINFO, Context.MODE_PRIVATE);
        return prefs.contains(VozConstant.USERNAME) && prefs.contains(VozConstant.PASSWORD);
    }

	public Object getDataFromCache(String key) {
		return mMemoryCache.get(key);
	}
	
	public void putDataToCache(String key, Object value) {
		mMemoryCache.put(key, value);
	}
	
	public VozCache getVozCache() {
		return vozCache;
	}

	public static void setVozCache(VozCache vozCache) {
		VozCache.vozCache = vozCache;
	}

	public int getCurrentForum() {
		return mCurrentForumId;
	}

	public void setCurrentForum(int currentForum) {
		this.mCurrentForumId = currentForum;
	}

	public int getCurrentForumPage() {
		return mCurrentForumPage;
	}

	public void setCurrentForumPage(int currentForumPage) {
		this.mCurrentForumPage = currentForumPage;
	}

	public int getCurrentThread() {
		return mCurrentThreadId;
	}

	public void setCurrentThread(int currentThread) {
		this.mCurrentThreadId = currentThread;
	}

	public int getCurrentThreadPage() {
		return mCurrentThreadPage;
	}

	public void setCurrentThreadPage(int currentThreadPage) {
		this.mCurrentThreadPage = currentThreadPage;
	}

	public void reset() {
		mCurrentThreadId = -1;
		mCurrentThreadPage = 0;
		mCurrentForumPage = 0;
		mCurrentForumId = -1;
	}

	public Map<String, String> getCookies() {
		return mCookies;
	}

	public void setCookies(Map<String, String> cookies) {
		this.mCookies = cookies;
	}

	public String getSecurityToken() {
		return mSecurityToken;
	}

	public void setSecurityToken(String securityToken) {
		this.mSecurityToken = securityToken;
	}

	public String getUserId() {		
		return mUserId;
	}

	public void setUserId(String userId) {
		this.mUserId = userId;
	}

	public boolean canShowReplyMenu() {
		return mCanShowReplyMenu;
	}

	public void setCanShowReplyMenu(boolean canShowReplyMenu) {
		this.mCanShowReplyMenu = canShowReplyMenu;
	}

	public int getCurrentParentForum() {
		return mCurrentParentForumId;
	}

	public void setCurrentParentForum(int currentParentForum) {
		this.mCurrentParentForumId = currentParentForum;
	}

	public int getWidth() {
		return mWidth;
	}

	public void setWidth(int width) {
		this.mWidth = width;
	}

	public int getHeight() {
		return mHeight;
	}

	public void setHeight(int height) {
		this.mHeight = height;
	}

	public int getLastPage() {
		return mLastPage;
	}

	public void setLastPage(int lastPage) {
		this.mLastPage = lastPage;
	}


	public Map<String, Object> cache() {
		return mForumCache;
	}


	public void removeLastNavigationLink() {
		if(mNeoNavigationList.size() > 0) {
			mNeoNavigationList.remove(mNeoNavigationList.size() - 1);
		}
	}

	public NavigationItem currentNavigateItem() {
		if (mNeoNavigationList.size() > 0) {
			return mNeoNavigationList.get(mNeoNavigationList.size() - 1);
		} else {
			return null;
		}
	}

	public void addNavigationItem(NavigationItem navItem) {
		mNeoNavigationList.add(navItem);
	}

	public void clearDumpCache(String key) {
		mMemoryCache.remove(key);
	}

	public boolean hasDataInCache(String key) {
		return mMemoryCache.get(key) != null;
	}

	public void savePreferecences(Context context) {
		Gson gson = new Gson();
		String rightThreadPinsString = gson.toJson(pinItemThreadList.toArray(new NavDrawerItem[]{}));
		String rightForumPinsString = gson.toJson(pinItemForumList.toArray(new NavDrawerItem[]{}));
		SharedPreferences prefs = context.getSharedPreferences(VozConstant.VOZINFO, Context.MODE_PRIVATE);
		prefs.edit()
				.putString("rightThreadPins", rightThreadPinsString)
				.putString("rightForumPins", rightForumPinsString)
				.apply();

	}

	public void addThreadItem(NavDrawerItem pinThread) {
		boolean found = false;
		for(NavDrawerItem item : pinItemThreadList) {
			if (item.id.equals(pinThread.id)) {
				found = true;
				break;
			}
		}
		if (!found) {
			pinItemThreadList.add(pinThread);
		}
	}

	public void addForumItem(NavDrawerItem pinForum) {
		boolean found = false;
		for(NavDrawerItem item : pinItemForumList) {
			if (item.id.equals(pinForum.id)) {
				found = true;
				break;
			}
		}
		if (!found) {
			pinItemForumList.add(pinForum);
		}
	}

	public void clearCache() {
		mMemoryCache.evictAll();
	}
}
