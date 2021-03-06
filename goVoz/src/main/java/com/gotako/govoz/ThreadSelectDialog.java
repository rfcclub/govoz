package com.gotako.govoz;

import android.widget.Toast;

import static com.gotako.govoz.VozConstant.THREAD_URL_T;

/**
 * Created by Nam on 9/12/2015.
 */
public class ThreadSelectDialog extends AbstractNoBorderDialog {
    @Override
    public void doOkAction(String... param) {
        int threadId = 0;
        try {
            threadId = Integer.parseInt(param[0]);
        } catch(NumberFormatException ex) {
            Toast.makeText(activity, getResources().getString(R.string.error_thread_select_id), Toast.LENGTH_SHORT).show();
            return;
        }

        String threadUrl = THREAD_URL_T + param[0].trim() + "&page=1";

        if(activity instanceof MainNeoActivity) {
            ((MainNeoActivity)activity).goToThreadId(threadId, threadUrl);
        }

    }
}
