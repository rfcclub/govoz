package com.gotako.govoz;

import android.widget.Toast;

import static com.gotako.govoz.VozConstant.THREAD_URL_T;

/**
 * Created by Nam on 9/12/2015.
 */
public class PageSelectDialog extends AbstractNoBorderDialog {

    public PageSelectDialog() {
        super();
    }

    @Override
    public void doOkAction(String... param) {
        int page = 0;
        try {
            page = Integer.parseInt(param[0]);
        } catch(NumberFormatException ex) {
            Toast.makeText(activity,getResources().getString(R.string.error_forum_select_id),Toast.LENGTH_SHORT).show();
            return;
        }

        if (page > VozCache.instance().currentNavigateItem().mLastPage) {
            page = VozCache.instance().currentNavigateItem().mLastPage;
        }
        if (page > 0 && page != VozCache.instance().currentNavigateItem().mCurrentPage) {
            ((MainNeoActivity)activity).mFragment.goToPage(page);
        }
    }
}
