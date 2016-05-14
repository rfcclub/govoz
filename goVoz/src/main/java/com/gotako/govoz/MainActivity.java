package com.gotako.govoz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;
import com.bugsense.trace.ExceptionCallback;
import com.gotako.animation.ExpandAnimation;
import com.gotako.gofast.GoFastEngine;
import com.gotako.gofast.adapter.ExpandableAdapter;
import com.gotako.gofast.listener.OnCreateViewListener;
import com.gotako.gofast.annotation.BindingCollection;
import com.gotako.govoz.data.Forum;
import com.gotako.govoz.tasks.VozMainForumDownloadTask;
import com.gotako.util.Utils;

import static com.gotako.govoz.VozConstant.FORUM_URL_F;
import static com.gotako.govoz.VozConstant.FORUM_URL_ORDER;

public class MainActivity extends VozFragmentActivity implements
        ActivityCallback<Forum>, OnChildClickListener, ExceptionCallback, OnCreateViewListener {
    @BindingCollection(id = R.id.forumList, layout = R.layout.forum_item, groupLayout = R.layout.forum_item_group, twoWay = true)
    private Object[] forums;
    // private Map<Forum, List<Forum>> forums;
    private ExpandableListView list;

    /**
     * The serialization (saved instance state) Bundle key representing the
     * current dropdown position.
     */
    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        BugSenseHandler.initAndStartSession(this, "2330a14e");
        BugSenseHandler.setLogging(1000, "*:W");
        super.onCreate(savedInstanceState);
        LayoutInflater mInflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View layout = mInflater.inflate(R.layout.activity_main, null);
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_container);
        frameLayout.addView(layout);
        // if login then do login
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        VozCache.instance().setHeight(outMetrics.heightPixels);
        VozCache.instance().setWidth(outMetrics.widthPixels);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        VozCache.instance().reset();
        VozCache.instance().setCanShowReplyMenu(false);
        VozConfig config = VozConfig.instance();
        config.load(this);
        VozCache.instance().navigationList.clear();
        forums = new Object[2];
        forums[0] = new ArrayList<Forum>();
        forums[1] = new HashMap<Forum, List<Forum>>();
        list = (ExpandableListView) layout.findViewById(R.id.forumList);
        list.setOnChildClickListener(this);
        GoFastEngine.initialize(this);
        doTheming();

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() != null) {
            doLoginAndGetVozForums();
        } else {
            Toast.makeText(this, "Không có kết nối đến mạng", Toast.LENGTH_SHORT).show();
        }
    }
    private void doLoginAndGetVozForums() {
        SharedPreferences prefs = this.getSharedPreferences(VozConstant.VOZINFO, Context.MODE_PRIVATE);
        boolean autoLogin = false;
        if (prefs.contains(VozConstant.USERNAME) && prefs.contains(VozConstant.PASSWORD)) {
            // if not login so do login
            if (VozCache.instance().getCookies() == null) {
                String username = prefs.getString(VozConstant.USERNAME, "guest");
                String password = prefs.getString(VozConstant.PASSWORD, "guest");
                AutoLoginBackgroundService albs = new AutoLoginBackgroundService(this);
                albs.doLogin(username, password);
            }
        }

        if (!autoLogin) {
            getVozForums();
        }
    }
    private void getVozForums() {
        VozMainForumDownloadTask task = new VozMainForumDownloadTask(this);
        task.setContext(this);
        task.setShowProcessDialog(true);
        task.execute(VozConstant.VOZ_LINK);
    }

    @SuppressWarnings("unchecked")
    @SuppressLint("UseSparseArrays")
    @Override
    public void doCallback(List<Forum> result, Object... extra) {
        if (result == null || result.size() == 0) {
            Toast.makeText(this, "Cannot access to VozForum. Please try again later.", Toast.LENGTH_SHORT).show();
        }
        Forum parentForum = null;
        List<Forum> headers = new ArrayList<Forum>();
        forums[0] = headers;
        forums[1] = new HashMap<Integer, List<Forum>>();
        Integer pos = -1;
        for (Forum forum : result) {
            if (parentForum == null || forum.getForumGroupName() != null) {
                parentForum = forum;
                headers.add(parentForum);
                pos++;
            }
            if (forum.getForumGroupName() == null) {
                List<Forum> list = ((Map<Integer, List<Forum>>) forums[1])
                        .get(pos);
                if (list == null) {
                    list = new ArrayList<Forum>();
                    ((Map<Integer, List<Forum>>) forums[1]).put(pos, list);
                }
                list.add(forum);
            }
        }

        GoFastEngine.notify(this, "forums");
        ExpandableListView elv = (ExpandableListView) findViewById(R.id.forumList);
        ((ExpandableAdapter<?>) elv.getExpandableListAdapter()).setOnCreateViewListener(this);
        for (int i = 0; i < headers.size(); i++) elv.expandGroup(i);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean onChildClick(ExpandableListView parent, View v,
                                int groupPosition, int childPosition, long id) {
        v.setSelected(true);
        v.setBackgroundResource(android.R.color.holo_blue_dark);
        List<Forum> list = ((Map<Integer, List<Forum>>) forums[1])
                .get(groupPosition);
        Forum selectedForum = list.get(childPosition);
        VozCache.instance().setCurrentForum(Integer.parseInt(selectedForum.getId()));
        VozCache.instance().setCurrentForumPage(1);
        VozCache.instance().cache().clear();
        String forumUrl = FORUM_URL_F + selectedForum.getId() + FORUM_URL_ORDER + "1";
        VozCache.instance().navigationList.add(forumUrl);
        Intent intent = new Intent(this, ForumActivity.class);
        startActivity(intent);
        return true;
    }

    @Override
    public void refresh() {
        getVozForums();
    }

    @Override
    public void postProcess(int position, View convertView, Object... extra) {
        ExpandAnimation expandAni = new ExpandAnimation(convertView, 500);
        convertView.startAnimation(expandAni);
    }

    @Override
    protected void doOnResume() {
        refreshActionBarIcon();
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }

    @Override
    public void onGetGroupView(int groupPosition, boolean isLastChild, View v, ViewGroup parent) {
        if(Build.VERSION.SDK_INT >= 16) {
            v.findViewById(R.id.forumGroupName).setBackground(
                    getResources().getDrawable(Utils.getValueByTheme(R.drawable.gradient, R.drawable.gradient_light)));
        } else {
            v.findViewById(R.id.forumGroupName).setBackgroundColor(
                    getResources().getColor(Utils.getValueByTheme(R.color.background_material_dark, R.color.background_material_light)));
        }
//        ((TextView) v.findViewById(R.id.forumGroupName)).setTextColor(R.color.white);
    }

    @Override
    public void onGetChildView(int groupPosition, int childPosition, boolean isLastChild, View v, ViewGroup parent) {
        v.findViewById(R.id.forumSection).setBackgroundColor(getResources().getColor(Utils.getValueByTheme(R.color.black, R.color.white)));
        ((TextView) v.findViewById(R.id.forumName)).setTextColor(getResources().getColor(Utils.getValueByTheme(R.color.light_gray, R.color.voz_front_color)));
        ((TextView) v.findViewById(R.id.viewing)).setTextColor(getResources().getColor(Utils.getValueByTheme(R.color.light_gray, R.color.black)));
    }
}
