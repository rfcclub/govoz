/**
 * 
 */
package com.gotako.govoz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
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
import com.gotako.gofast.listener.BindingActionListener;
import com.gotako.govoz.data.VozMenuItem;
import com.gotako.govoz.tasks.UserLogoutTask;
import com.gotako.util.Utils;

import java.util.ArrayList;

/**
 * @author lnguyen66
 * 
 */
public class VozFragmentActivity extends BaseFragmentActivity implements
		OnMenuItemClickListener,BindingActionListener, ExceptionCallback,LogoutCallback {
	protected boolean threadIsClosed;

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
        if(VozCache.instance().isLoggedIn()) {
            VozCache.instance().menuItemList.add(new VozMenuItem(Utils.getString(this, R.string.left_menu_logout), R.drawable.ic_input_white_18dp, 9));
        } else {
            VozCache.instance().menuItemList.add(new VozMenuItem(Utils.getString(this, R.string.left_menu_login), R.drawable.ic_account_box_white_18dp, 7));
            if(VozCache.instance().isSavedPreference(this.getBaseContext())) {
                VozCache.instance().menuItemList.add(new VozMenuItem(Utils.getString(this, R.string.left_menu_login_with_pref), R.drawable.ic_assignment_ind_white_18dp, 8));
            }
        }
        VozCache.instance().menuItemList.add(new VozMenuItem("-", -1, -1));
        VozCache.instance().menuItemList.add(new VozMenuItem(Utils.getString(this, R.string.left_menu_home), R.drawable.ic_home_white_18dp, 0));
        VozCache.instance().menuItemList.add(new VozMenuItem(Utils.getString(this, R.string.left_menu_go_thread), R.drawable.ic_open_in_new_white_18dp, 1));
        VozCache.instance().menuItemList.add(new VozMenuItem(Utils.getString(this, R.string.left_menu_go_forum), R.drawable.ic_open_in_new_white_18dp, 2));
        VozCache.instance().menuItemList.add(new VozMenuItem("-", -1, -1));
        VozCache.instance().menuItemList.add(new VozMenuItem(Utils.getString(this, R.string.left_menu_inbox), R.drawable.ic_mail_white_18dp, 3));
        VozCache.instance().menuItemList.add(new VozMenuItem(Utils.getString(this, R.string.left_menu_search), R.drawable.ic_search_white_18dp, 4));
        VozCache.instance().menuItemList.add(new VozMenuItem("-", -1, -1));
        VozCache.instance().menuItemList.add(new VozMenuItem(Utils.getString(this, R.string.left_menu_setting), R.drawable.ic_settings_white_18dp, 5));
        VozCache.instance().menuItemList.add(new VozMenuItem(Utils.getString(this, R.string.left_menu_exit), R.drawable.ic_power_settings_new_white_18dp, 6));
    }

    @Override
    protected void createRightMenu() {

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
		    default: // last option

		}
		return true;
	}

	private void doLoginWithPreset() {
		SharedPreferences prefs = this.getSharedPreferences("VOZINFO", Context.MODE_PRIVATE);
		boolean autoLogin = false;
		if(prefs.contains("USERNAME") && prefs.contains("PASSWORD")) {
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
		if(result) {
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
     * */
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
                        Intent intentInbox = new Intent(VozFragmentActivity.this,InboxActivity.class);
                        VozFragmentActivity.this.startActivity(intentInbox);
						break;
                    case 4:
						break;
                    case 5:
                        Intent intent1 = new Intent(VozFragmentActivity.this,SettingActivity.class);
                        VozFragmentActivity.this.startActivity(intent1);
                        break;
                    case 6:
                        VozFragmentActivity.this.finishAffinity();
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
}

