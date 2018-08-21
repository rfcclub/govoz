/**
 *
 */
package com.gotako.govoz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;
import com.bugsense.trace.ExceptionCallback;
import com.google.gson.Gson;
import com.gotako.gofast.listener.BindingActionListener;
import com.gotako.govoz.data.NavDrawerItem;
import com.gotako.govoz.data.VozMenuItem;
import com.gotako.govoz.tasks.UserLogoutTask;
import com.gotako.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lnguyen66
 */
public class VozFragmentActivity extends BaseFragmentActivity implements
        OnMenuItemClickListener, BindingActionListener, ExceptionCallback, LogoutCallback {
    protected boolean threadIsClosed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        BugSenseHandler.initAndStartSession(this, "2330a14e");
        BugSenseHandler.setLogging(1000, "*:W");
        VozConfig.instance().load(this);
//        if (VozConfig.instance().isDarkTheme()) {
            setTheme(R.style.AppTheme);
//        } else {
//            setTheme(R.style.AppTheme_Light);
//        }
        overridePendingTransition(R.animator.right_slide_in, R.animator.left_slide_out_half);
        super.onCreate(savedInstanceState);
        mRightForumListView.setOnItemClickListener(new OnRightMenuForumItemClickListener(this));
        mRightLinkListView.setOnItemClickListener(new OnRightMenuThreadItemClickListener(this));
    }

    /*
     * This method just can be called at the end of onCreate of subclasses
     */
    protected void doTheming() {
        if (VozConfig.instance().isDarkTheme()) {
            findViewById(R.id.layout_slidermenu).setBackgroundColor(getResources().getColor(R.color.list_background));
            findViewById(R.id.right_slider_menu).setBackgroundColor(getResources().getColor(R.color.list_background));
            ((ListView) findViewById(R.id.list_slidermenu)).setSelector(getResources().getDrawable(R.drawable.list_selector));
            ((ListView) findViewById(R.id.list_right_menu_forum)).setSelector(getResources().getDrawable(R.drawable.list_selector));
            ((ListView) findViewById(R.id.list_right_menu_thread)).setSelector(getResources().getDrawable(R.drawable.list_selector));

        } else {
            findViewById(R.id.layout_slidermenu).setBackgroundColor(getResources().getColor(R.color.list_background_light));
            findViewById(R.id.right_slider_menu).setBackgroundColor(getResources().getColor(R.color.list_background_light));
            ((ListView) findViewById(R.id.list_slidermenu)).setSelector(getResources().getDrawable(R.drawable.list_selector_light));
            ((ListView) findViewById(R.id.list_right_menu_forum)).setSelector(getResources().getDrawable(R.drawable.list_selector_light));
            ((ListView) findViewById(R.id.list_right_menu_thread)).setSelector(getResources().getDrawable(R.drawable.list_selector_light));
        }
    }

    @Override
    protected AdapterView.OnItemClickListener createItemClickListener() {
        return new SlideMenuClickListener();
    }

    @Override
    protected void createMenuList() {
        createLeftMenu();
        createRightMenu();
    }

    protected void createLeftMenu() {
        if (VozCache.instance().menuItemList == null) {
            VozCache.instance().menuItemList = new ArrayList<VozMenuItem>();
        } else {
            VozCache.instance().menuItemList.clear();
        }

        VozCache.instance().menuItemList.add(new VozMenuItem("-", -1, -1));
        VozCache.instance().menuItemList.add(new VozMenuItem(Utils.getString(this, R.string.left_menu_home),R.drawable.home, 0));
        VozCache.instance().menuItemList.add(new VozMenuItem(Utils.getString(this, R.string.left_menu_go_thread), R.drawable.subscribe, 1));
        VozCache.instance().menuItemList.add(new VozMenuItem(Utils.getString(this, R.string.left_menu_go_forum), R.drawable.subscribe, 2));
        // VozCache.instance().menuItemList.add(new VozMenuItem(Utils.getString(this, R.string.left_menu_go_subcribe), R.drawable.subscribe, 11));
        VozCache.instance().menuItemList.add(new VozMenuItem("-", -1, -1));
        VozCache.instance().menuItemList.add(new VozMenuItem(Utils.getString(this, R.string.left_menu_inbox), R.drawable.message, 3));
        VozCache.instance().menuItemList.add(new VozMenuItem(Utils.getString(this, R.string.left_menu_search), R.drawable.search, 4));
        VozCache.instance().menuItemList.add(new VozMenuItem("-", -1, -1));
        VozCache.instance().menuItemList.add(new VozMenuItem(Utils.getString(this, R.string.left_menu_setting), R.drawable.settings, 5));
        VozCache.instance().menuItemList.add(new VozMenuItem(Utils.getString(this, R.string.left_menu_aboutus), R.drawable.aboutus, 10));

    }

    protected void createRightMenu() {
        Gson gson = new Gson();
        SharedPreferences prefs = getBaseContext().getSharedPreferences("VOZCONFIG", Context.MODE_PRIVATE);
        String jsonForumString = null;
        jsonForumString = prefs.getString("rightForumPins", null);
        List<NavDrawerItem> pinItemForumList = VozCache.instance().pinItemForumList;
        pinItemForumList.clear();
        if(jsonForumString != null) {
            NavDrawerItem[] items = gson.fromJson(jsonForumString, NavDrawerItem[].class);
            for(NavDrawerItem item : items) pinItemForumList.add(item);
        } else {
            pinItemForumList.add(buildForumItem("Chuyện trò linh tinh", "17"));
            pinItemForumList.add(buildForumItem("Điểm báo", "33"));
            pinItemForumList.add(buildForumItem("Review sản phẩm", "27"));
            pinItemForumList.add(buildForumItem("From F17 with love", "145"));
            pinItemForumList.add(buildForumItem("Mua và bán", "84"));
        }

        jsonForumString = prefs.getString("rightThreadPins", null);
        List<NavDrawerItem> pinItemThreadList = VozCache.instance().pinItemThreadList;
        pinItemThreadList.clear();
        if(jsonForumString != null) {
            NavDrawerItem[] items = gson.fromJson(jsonForumString, NavDrawerItem[].class);
            for(NavDrawerItem item : items) pinItemThreadList.add(item);
        } else {
            pinItemThreadList.add(buildThreadItem("Bóng đá VIỆT NAM (Tất cả vào đây)", "3925224"));
            pinItemThreadList.add(buildThreadItem("Mục lục - Danh sách các bài viết có giá trị tham khảo của box review", "123078"));
        }
        findViewById(R.id.loginLink).setOnClickListener((view)-> {doLogin();});
        findViewById(R.id.logoutLink).setOnClickListener((view) -> {doLogout();});
        findViewById(R.id.preLoginLink).setOnClickListener((view) -> { doLoginWithPreset();});
        findViewById(R.id.exitLink).setOnClickListener((view) -> {
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                VozFragmentActivity.this.finishAffinity();
            } else {
                VozFragmentActivity.this.finish();
            }
        });
    }

    @Override
    public void savePinForumsList() {
        SharedPreferences prefs = this.getBaseContext().getSharedPreferences("VOZCONFIG", Context.MODE_PRIVATE);
        NavDrawerItem[] forumsList = VozCache.instance().pinItemForumList.toArray(new NavDrawerItem[]{});
        Gson gson = new Gson();
        String jsonString = gson.toJson(forumsList);
        prefs.edit()
                .putString("rightForumPins", jsonString)
                .commit();

    }

    @Override
    public void savePinThreadsList() {
        SharedPreferences prefs = this.getBaseContext().getSharedPreferences("VOZCONFIG", Context.MODE_PRIVATE);
        NavDrawerItem[] forumsList = VozCache.instance().pinItemThreadList.toArray(new NavDrawerItem[]{});
        Gson gson = new Gson();
        String jsonString = gson.toJson(forumsList);
        prefs.edit()
                .putString("rightThreadPins", jsonString)
                .commit();

    }

    protected NavDrawerItem buildThreadItem(String title, String threadId) {
        return new NavDrawerItem(title, threadId, NavDrawerItem.THREAD);
    }

    protected NavDrawerItem buildForumItem(String forumName, String forumId) {
        return new NavDrawerItem(forumName, forumId, NavDrawerItem.FORUM);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        setListenerForMenuItems(menu);
        return true;
    }

    protected void setListenerForMenuItems(Menu menu) {
        MenuItem refresh = menu.findItem(R.id.action_refresh);
        refresh.setOnMenuItemClickListener(this);
        MenuItem rMenu = menu.findItem(R.id.action_rmenu);
        rMenu.setOnMenuItemClickListener(this);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                forceRefresh();
            break;
            case R.id.action_rmenu:
                showMenu();
                break;
            default: // last option

        }
        return true;
    }

    protected void showMenu() {
        // do nothing
    }

    protected void forceRefresh() {
        // do nothing
    }

    private void doLoginWithPreset() {
        SharedPreferences prefs = this.getSharedPreferences("VOZINFO", Context.MODE_PRIVATE);
        boolean autoLogin = false;
        if (prefs.contains("USERNAME") && prefs.contains("PASSWORD")) {
            // if not login so do login
            if (VozCache.instance().getCookies() == null) {
                String username = prefs.getString("USERNAME", "guest");
                String password = prefs.getString("PASSWORD", "guest");
                AutoLoginBackgroundService albs = new AutoLoginBackgroundService(this);
                albs.doLogin(username, password);
            }
        }
    }

    private void showSetting() {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }

    private void doLogout() {
        UserLogoutTask task = new UserLogoutTask(this);
        task.setCallback(this);
        task.execute();
    }

    private void doLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void refresh() {
        // do nothing. This method will be overriden by subclasses
    }

    public void doRep() {
        // do nothing. This method will be overriden by subclasses
    }

    @Override
    public void lastBreath(Exception ex) {
        ex.printStackTrace(); // in case you want to see the stacktrace in your log cat output
        BugSenseHandler.sendException(ex);
    }

    @Override
    public void doAfterLogout(boolean result) {

        if (result) {
            refreshLeftMenu();
            refresh();
        }
        refreshLinks();
    }

    public void doAfterAutoLogin() {
        refreshLeftMenu();
        refreshLinks();
    }

    protected void refreshLinks() {
        if(VozCache.instance().isLoggedIn()) {
            findViewById(R.id.loginLink).setVisibility(View.GONE);
            findViewById(R.id.preLoginLink).setVisibility(View.GONE);
            findViewById(R.id.yourPosts).setVisibility(View.VISIBLE);
            findViewById(R.id.yourThreads).setVisibility(View.VISIBLE);
            findViewById(R.id.logoutLink).setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.loggedInUser)).setText(VozCache.instance().getUserId());
        } else {
            findViewById(R.id.loginLink).setVisibility(View.VISIBLE);
            findViewById(R.id.preLoginLink).setVisibility(View.VISIBLE);
            findViewById(R.id.yourPosts).setVisibility(View.GONE);
            findViewById(R.id.yourThreads).setVisibility(View.GONE);
            findViewById(R.id.logoutLink).setVisibility(View.GONE);
            ((TextView)findViewById(R.id.loggedInUser)).setText(VozCache.instance().GUEST);
        }
    }

    private void refreshLeftMenu() {
        VozCache.instance().menuItemList.clear();
        createLeftMenu();
        mLeftMenuAdapter.notifyDataSetChanged();
    }

    @Override
    public void preProcess(int position, View convertView, Object... extra) {

    }

    @Override
    public void postProcess(int position, View convertView, Object... extra) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        doOnResume();
    }

    protected void doOnResume() {
        // do nothing
    }

    /**
     * Slide menu item click listener
     */
    private class SlideMenuClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            FragmentManager fm = VozFragmentActivity.this.getSupportFragmentManager();
            if (position < mNavDrawerItemsList.size()) {
                VozMenuItem item = mNavDrawerItemsList.get(position);
                switch (item.type) {
                    case 0: // home
                        VozCache.instance().mNeoNavigationList.clear();
                        if(getSupportFragmentManager().getBackStackEntryCount() > 0) {
                            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        } else {
                            refresh();
                        }
                        break;
                    case 1: // custom thread move
                        ThreadSelectDialog threadSelectDialog = new ThreadSelectDialog();
                        threadSelectDialog.setActivity(VozFragmentActivity.this);
                        threadSelectDialog.setTitle(getResources().getString(R.string.thread_select_title));
                        threadSelectDialog.show(fm, "threadSelect");
                        break;
                    case 2: // custom forum move
                        ForumSelectDialog forumSelectDialog = new ForumSelectDialog();
                        forumSelectDialog.setActivity(VozFragmentActivity.this);
                        forumSelectDialog.setTitle(getResources().getString(R.string.forum_select_title));
                        forumSelectDialog.show(fm, "forumSelect");
                        break;
                    case 3: // go to inbox
                        if(VozCache.instance().isLoggedIn()) {
                            doInbox();
                        } else {
                            Toast.makeText(VozFragmentActivity.this, getResources().getString(R.string.error_not_login_go_inbox), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 4: // search
                        SearchDialog searchDialog = new SearchDialog();
                        searchDialog.setActivity(VozFragmentActivity.this);
                        searchDialog.setTitle(getResources().getString(R.string.search_title));
                        searchDialog.show(fm, "searchDialog");
                        break;
                    case 5: // go to setting
                        Intent intent1 = new Intent(VozFragmentActivity.this, SettingActivity.class);
                        VozFragmentActivity.this.startActivity(intent1);
                        break;
                    default:
                        break;
                }
                if(mDrawerLayout.isDrawerOpen(Gravity.LEFT)){
                    mDrawerLayout.closeDrawer(Gravity.LEFT);
                }
            }
        }
    }

    protected void doInbox() {
        // do nothing
    }

    private class OnRightMenuForumItemClickListener implements AdapterView.OnItemClickListener {
        final VozFragmentActivity activity;
        public OnRightMenuForumItemClickListener(VozFragmentActivity vozFragmentActivity) {
            activity = vozFragmentActivity;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            NavDrawerItem forumItem = VozCache.instance().pinItemForumList.get(position);
            if(activity instanceof MainNeoActivity) {
                ((MainNeoActivity) activity).onForumClicked(forumItem.id);
            }
            if(mDrawerLayout.isDrawerOpen(Gravity.RIGHT)){
                mDrawerLayout.closeDrawer(Gravity.RIGHT);
            }
        }
    }

    private class OnRightMenuThreadItemClickListener implements AdapterView.OnItemClickListener {
        final VozFragmentActivity activity;
        public OnRightMenuThreadItemClickListener(VozFragmentActivity vozFragmentActivity) {
            activity = vozFragmentActivity;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            NavDrawerItem threadItem = VozCache.instance().pinItemThreadList.get(position);
            if(activity instanceof MainNeoActivity) {
                ((MainNeoActivity) activity).goToThreadId(Integer.parseInt(threadItem.id), threadItem.url);
            }
            if(mDrawerLayout.isDrawerOpen(Gravity.RIGHT)){
                mDrawerLayout.closeDrawer(Gravity.RIGHT);
            }
        }
    }
}

