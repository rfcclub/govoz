package com.gotako.govoz;

import android.widget.Toast;

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

        if (activity instanceof MainNeoActivity) {
            ((MainNeoActivity)activity).onForumClicked(param);
        }
    }
}
