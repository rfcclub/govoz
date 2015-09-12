package com.gotako.govoz;

import android.content.Intent;
import android.widget.Toast;

import static com.gotako.govoz.VozConstant.FORUM_URL_F;
import static com.gotako.govoz.VozConstant.FORUM_URL_ORDER;
import static com.gotako.govoz.VozConstant.THREAD_URL_T;

/**
 * Created by Nam on 9/12/2015.
 */
public class ThreadSelectDialog extends AbstractNoBorderDialog {
    @Override
    public void doOkAction(String param) {
        try {
            Integer.parseInt(param);
        } catch(NumberFormatException ex) {
            Toast.makeText(activity, getResources().getString(R.string.error_forum_select_id), Toast.LENGTH_SHORT).show();
            return;
        }

        String threadUrl = THREAD_URL_T + param.trim() + "&page="
                + String.valueOf(VozCache.instance().getCurrentThreadPage());
        VozCache.instance().navigationList.add(threadUrl);
        Intent intent = new Intent(activity, ThreadActivity.class);
        startActivity(intent);
    }
}
