package com.gotako.govoz.listeners;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;

import com.gotako.govoz.ThreadActivity;
import com.gotako.govoz.VozCache;
import com.gotako.govoz.data.NavDrawerItem;

/**
 * Created by tunam on 7/4/16.
 */

public class OnThreadItemClickListener implements AdapterView.OnItemClickListener {

    Activity activity;
    public OnThreadItemClickListener(Activity context) {
        this.activity = context;
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        NavDrawerItem threadItem = VozCache.instance().pinItemThreadList.get(position);
        VozCache.instance().navigationList.add(threadItem.url);
        Intent intent = new Intent(activity, ThreadActivity.class);
        activity.startActivity(intent);
    }
}
