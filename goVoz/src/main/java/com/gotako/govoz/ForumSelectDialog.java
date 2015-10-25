package com.gotako.govoz;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import static com.gotako.govoz.VozConstant.FORUM_URL_F;
import static com.gotako.govoz.VozConstant.FORUM_URL_ORDER;

/**
 * Created by Nam on 9/12/2015.
 */
public class ForumSelectDialog extends AbstractNoBorderDialog {

    public ForumSelectDialog() {
        super();
    }

    @Override
    public void doOkAction(String param) {
        try {
            Integer.parseInt(param);
        } catch(NumberFormatException ex) {
            Toast.makeText(activity,getResources().getString(R.string.error_forum_select_id),Toast.LENGTH_SHORT).show();
            return;
        }
        if(param.trim().equals("33")) {
            if (VozCache.instance().getCookies() == null) {
                Toast.makeText(activity,getResources().getString(R.string.error_not_login_go_forum),Toast.LENGTH_SHORT).show();
                return;
            }
        }
        String forumUrl = FORUM_URL_F + param.trim() + FORUM_URL_ORDER + "1";
        VozCache.instance().navigationList.add(forumUrl);
        Intent intent = new Intent(activity, ForumActivity.class);
        startActivity(intent);
    }
}
