/**
 *
 */
package com.gotako.govoz;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;
import com.bugsense.trace.ExceptionCallback;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.gotako.gofast.listener.BindingActionListener;
import com.gotako.govoz.data.NavDrawerItem;
import com.gotako.govoz.data.VozMenuItem;
import com.gotako.govoz.listeners.OnForumItemClickListener;
import com.gotako.govoz.listeners.OnThreadItemClickListener;
import com.gotako.govoz.tasks.UserLogoutTask;
import com.gotako.util.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.gotako.govoz.VozConstant.FORUM_URL_F;
import static com.gotako.govoz.VozConstant.THREAD_URL_T;
import static com.gotako.govoz.VozConstant.VOZ_LINK;

/**
 * @author lnguyen66
 */
public class VozFragmentActivity extends BaseFragmentActivity implements
        OnMenuItemClickListener, BindingActionListener, ExceptionCallback, LogoutCallback {
    protected boolean threadIsClosed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        VozConfig.instance().load(this);
        if (VozConfig.instance().isDarkTheme()) {
            setTheme(R.style.AppTheme);
        } else {
            setTheme(R.style.AppTheme_Light);
        }
        overridePendingTransition(R.animator.right_slide_in, R.animator.left_slide_out_half);
        super.onCreate(savedInstanceState);
        mRightForumList.setOnItemClickListener(new OnForumItemClickListener(this));
        mRightLinkList.setOnItemClickListener(new OnThreadItemClickListener(this));
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
    protected void createLeftMenu() {
        if (VozCache.instance().menuItemList == null) {
            VozCache.instance().menuItemList = new ArrayList<VozMenuItem>();
        } else {
            VozCache.instance().menuItemList.clear();
        }
        if (VozCache.instance().isLoggedIn()) {
            VozCache.instance().menuItemList.add(new VozMenuItem(Utils.getString(this, R.string.left_menu_logout),
                    Utils.getValueByTheme(R.drawable.ic_input_white_18dp, R.drawable.ic_input_black_18dp), 9));
        } else {
            VozCache.instance().menuItemList.add(new VozMenuItem(Utils.getString(this, R.string.left_menu_login),
                    Utils.getValueByTheme(R.drawable.ic_account_box_white_18dp, R.drawable.ic_account_box_black_18dp), 7));
            if (VozCache.instance().isSavedPreference(this.getBaseContext())) {
                VozCache.instance().menuItemList.add(new VozMenuItem(Utils.getString(this, R.string.left_menu_login_with_pref),
                        Utils.getValueByTheme(R.drawable.ic_assignment_ind_white_18dp, R.drawable.ic_assignment_ind_black_18dp), 8));
            }
        }
        VozCache.instance().menuItemList.add(new VozMenuItem("-", -1, -1));
        VozCache.instance().menuItemList.add(new VozMenuItem(Utils.getString(this, R.string.left_menu_home),
                Utils.getValueByTheme(R.drawable.ic_home_white_18dp, R.drawable.ic_home_black_18dp), 0));
        VozCache.instance().menuItemList.add(new VozMenuItem(Utils.getString(this, R.string.left_menu_go_thread),
                Utils.getValueByTheme(R.drawable.ic_open_in_new_white_18dp, R.drawable.ic_open_in_new_black_18dp), 1));
        VozCache.instance().menuItemList.add(new VozMenuItem(Utils.getString(this, R.string.left_menu_go_forum),
                Utils.getValueByTheme(R.drawable.ic_open_in_new_white_18dp, R.drawable.ic_open_in_new_black_18dp), 2));
        VozCache.instance().menuItemList.add(new VozMenuItem("-", -1, -1));
        VozCache.instance().menuItemList.add(new VozMenuItem(Utils.getString(this, R.string.left_menu_inbox),
                Utils.getValueByTheme(R.drawable.ic_mail_white_18dp, R.drawable.ic_mail_black_18dp), 3));
        VozCache.instance().menuItemList.add(new VozMenuItem(Utils.getString(this, R.string.left_menu_search),
                Utils.getValueByTheme(R.drawable.ic_search_white_18dp, R.drawable.ic_search_black_18dp), 4));
        VozCache.instance().menuItemList.add(new VozMenuItem("-", -1, -1));
        VozCache.instance().menuItemList.add(new VozMenuItem(Utils.getString(this, R.string.left_menu_setting),
                Utils.getValueByTheme(R.drawable.ic_settings_white_18dp, R.drawable.ic_settings_black_18dp), 5));
        VozCache.instance().menuItemList.add(new VozMenuItem(Utils.getString(this, R.string.left_menu_exit),
                Utils.getValueByTheme(R.drawable.ic_power_settings_new_white_18dp, R.drawable.ic_power_settings_new_black_18dp), 6));
    }

    @Override
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
        return new NavDrawerItem(title, THREAD_URL_T + threadId, NavDrawerItem.THREAD);
    }

    protected NavDrawerItem buildForumItem(String forumName, String forumId) {
        return new NavDrawerItem(forumName, FORUM_URL_F + forumId, NavDrawerItem.FORUM);
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
        MenuItem reply = menu.findItem(R.id.action_reply);
        reply.setOnMenuItemClickListener(this);
        MenuItem bookmark = menu.findItem(R.id.action_bookmark);
        bookmark.setOnMenuItemClickListener(this);
        MenuItem star = menu.findItem(R.id.action_star);
        star.setOnMenuItemClickListener(this);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                refresh();
            break;
            case R.id.action_reply:
                doRep();
                break;
            case R.id.action_bookmark:
                doBookmark();
                break;
            case R.id.action_star:
                doRating();
                break;
            default: // last option

        }
        return true;
    }

    public void doRating() {

    }

    public void doBookmark() {

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

    }

    @Override
    public void preProcess(int position, View convertView, Object... extra) {
        // do nothing
    }

    @Override
    public void postProcess(int position, View convertView, Object... extra) {
        // do nothing

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
    }

    public void doAfterAutoLogin() {
        refreshLeftMenu();

    }

    private void refreshLeftMenu() {
        VozCache.instance().menuItemList.clear();
        createLeftMenu();
        leftMenuAdapter.notifyDataSetChanged();
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
            if (position < navDrawerItems.size()) {
                VozMenuItem item = navDrawerItems.get(position);
                switch (item.type) {
                    case 0: // home
                        VozCache.instance().navigationList.clear();
                        Intent intent = new Intent(getBaseContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        break;
                    case 1:
                        ThreadSelectDialog threadSelectDialog = new ThreadSelectDialog();
                        // dialog.setStyle(DialogFragment.STYLE_NORMAL,  R.style.ThemeWithCorners);
                        threadSelectDialog.setActivity(VozFragmentActivity.this);
                        threadSelectDialog.setTitle(getResources().getString(R.string.thread_select_title));
                        threadSelectDialog.show(fm, "threadSelect");
                        break;
                    case 2:
                        ForumSelectDialog forumSelectDialog = new ForumSelectDialog();
                        // dialog.setStyle(DialogFragment.STYLE_NORMAL,  R.style.ThemeWithCorners);
                        forumSelectDialog.setActivity(VozFragmentActivity.this);
                        forumSelectDialog.setTitle(getResources().getString(R.string.forum_select_title));
                        forumSelectDialog.show(fm, "forumSelect");
                        break;
                    case 3:
                        if(VozCache.instance().isLoggedIn()) {
                            String pmHttpsLink = VOZ_LINK + "/" + "private.php";
                            VozCache.instance().navigationList.add(pmHttpsLink);
                            Intent intentInbox = new Intent(VozFragmentActivity.this, InboxActivity.class);
                            VozFragmentActivity.this.startActivity(intentInbox);
                        } else {
                            Toast.makeText(VozFragmentActivity.this, getResources().getString(R.string.error_not_login_go_inbox), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 4:
                        break;
                    case 5:
                        Intent intent1 = new Intent(VozFragmentActivity.this, SettingActivity.class);
                        VozFragmentActivity.this.startActivity(intent1);
                        break;
                    case 6:
                        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                            VozFragmentActivity.this.finishAffinity();
                        } else {
                            VozFragmentActivity.this.finish();
                        }
                        break;
                    case 7:
                        doLogin();
                        break;
                    case 8:
                        doLoginWithPreset();
                        break;
                    case 9:
                        doLogout();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        doOnResume();
    }

    protected void doOnResume() {
        // do nothing
    }
}

