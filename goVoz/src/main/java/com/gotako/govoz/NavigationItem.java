package com.gotako.govoz;

/**
 * Created by tunam on 10/22/16.
 */
public class NavigationItem {
    public static final int FORUM = 1;
    public static final int THREAD = 2;
    public static final int EXTERNAL_PICTURE = 3;
    public static final int EXTERNAL_LINK = 4;
    public static final int INBOX = 5;
    public static final int INBOX_DETAIL = 6;
    public String mLink;
    public int mType;
    public int mLastPage;
    public int mCurrentPage;

    public NavigationItem(String mLink, int mType) {
        this.mLink = mLink;
        this.mType = mType;
        mLastPage = 1;
        mCurrentPage = 1;
    }
}
