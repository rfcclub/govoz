package com.gotako.govoz;

import android.widget.Toast;

import static com.gotako.govoz.VozConstant.THREAD_URL_T;

/**
 * Created by Nam on 9/12/2015.
 */
public class ThreadSelectDialog extends AbstractNoBorderDialog {
    @Override
    public void doOkAction(String param) {
        int threadId = 0;
        try {
            threadId = Integer.parseInt(param);
        } catch(NumberFormatException ex) {
            Toast.makeText(activity, getResources().getString(R.string.error_forum_select_id), Toast.LENGTH_SHORT).show();
            return;
        }

        String threadUrl = THREAD_URL_T + param.trim() + "&page="
                + String.valueOf(VozCache.instance().getCurrentThreadPage());

        if(activity instanceof MainNeoActivity) {
            ((MainNeoActivity)activity).goToThreadId(threadId, threadUrl);
        }

    }
}
