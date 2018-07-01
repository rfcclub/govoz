package com.gotako.govoz.utils;

import com.gotako.govoz.NavigationItem;
import com.gotako.govoz.VozCache;
import com.gotako.govoz.data.Post;
import com.gotako.govoz.tasks.HttpDownloadTask;
import com.gotako.govoz.tasks.VozThreadDownloadTask;

import org.jsoup.nodes.Document;

import java.util.List;

import static com.gotako.govoz.VozConstant.THREAD_URL_T;

public class CacheUtils {
    public static void preloadThreads(NavigationItem item) {
        if (item.mType == NavigationItem.THREAD) {
            String currentThreadId = String.valueOf(VozCache.instance().getCurrentThread());
            int nextPage = item.mCurrentPage + 1;
            if (nextPage <= item.mLastPage) {
                String threadUrl = THREAD_URL_T + "/" + currentThreadId
                        + "&page="
                        + String.valueOf(nextPage);
                VozThreadDownloadTask task = new VozThreadDownloadTask(null);
                task.execute(threadUrl);
            }
            // get last page to get latest
            String threadUrl = THREAD_URL_T + "/" + currentThreadId
                    + "&page="
                    + String.valueOf(VozCache.instance().currentNavigateItem().mLastPage);
            VozThreadDownloadTask task = new VozThreadDownloadTask(null);
            task.execute(threadUrl);
        }
    }
}
