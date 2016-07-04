package com.gotako.govoz.listeners;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;

import com.gotako.govoz.ForumActivity;
import com.gotako.govoz.VozCache;
import com.gotako.govoz.data.NavDrawerItem;

/**
 * Created by tunam on 7/4/16.
 */

public class OnForumItemClickListener implements AdapterView.OnItemClickListener {

    Activity activity;
    public OnForumItemClickListener(Activity context) {
        this.activity = context;
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        NavDrawerItem forumItem = VozCache.instance().pinItemForumList.get(position);
        VozCache.instance().navigationList.add(forumItem.url);
        Intent intent = new Intent(activity, ForumActivity.class);
        activity.startActivity(intent);
    }
}
