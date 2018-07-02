package com.gotako.govoz.utils;

import android.content.Context;

import com.gotako.govoz.NavigationItem;
import com.gotako.govoz.VozCache;
import com.gotako.govoz.tasks.VozForumDownloadTask;
import com.gotako.govoz.tasks.VozThreadDownloadTask;

import static com.gotako.govoz.VozConstant.FORUM_URL_F;
import static com.gotako.govoz.VozConstant.FORUM_URL_ORDER;
import static com.gotako.govoz.VozConstant.THREAD_URL_T;

public class CacheUtils {
    public static void preload(Context mContext, NavigationItem item) {
        if (item.mType == NavigationItem.THREAD) {
            String currentThreadId = String.valueOf(VozCache.instance().getCurrentThread());
            int nextPage = item.mCurrentPage + 1;
            int prevPage = item.mCurrentPage - 1;
            if (nextPage < item.mLastPage && !hasDownload(currentThreadId, nextPage)) {
                downloadThread(mContext, currentThreadId, nextPage);
            }
            if (prevPage > 1 && !hasDownload(currentThreadId, prevPage)) {
                downloadThread(mContext, currentThreadId, prevPage);
            }
            // get last page to get latest
            downloadThread(mContext, currentThreadId, VozCache.instance().currentNavigateItem().mLastPage);
        } else if (item.mType == NavigationItem.FORUM) {
            String currentForumId = String.valueOf(VozCache.instance().getCurrentForum());
            int nextPage = item.mCurrentPage + 1;
            int prevPage = item.mCurrentPage - 1;
            if (nextPage < item.mLastPage) {
                downloadForum(mContext, currentForumId, nextPage);
            }
            if (prevPage > 1) {
                downloadForum(mContext, currentForumId, prevPage);
            }
        }
    }

    private static void downloadForum(Context mContext, String currentForumId, int page) {
        VozForumDownloadTask task = new VozForumDownloadTask(null);
        String forumUrl = FORUM_URL_F + currentForumId + FORUM_URL_ORDER
                + String.valueOf(page);
        task.setForumId(String.valueOf(currentForumId));
        task.setContext(mContext);
        task.execute(forumUrl, String.valueOf(page));
    }

    private static void downloadThread(Context mContext, String currentThreadId, int page) {
        String threadUrl = THREAD_URL_T + currentThreadId
                + "&page="
                + String.valueOf(page);
        VozThreadDownloadTask task = new VozThreadDownloadTask(null);
        task.setContext(mContext);
        task.execute(threadUrl, String.valueOf(page));
    }

    private static boolean hasDownload(String currentThreadId, int prevPage) {
        String key = currentThreadId + "_" + prevPage;
        return VozCache.instance().hasDataInCache(key);
    }
}
