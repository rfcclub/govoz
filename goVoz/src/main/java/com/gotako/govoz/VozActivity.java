package com.gotako.govoz;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;
import com.bugsense.trace.ExceptionCallback;
import com.gotako.gofast.listener.BindingActionListener;
import com.gotako.govoz.tasks.UserLogoutTask;

public class VozActivity extends BaseActivity implements OnMenuItemClickListener,
		BindingActionListener, ExceptionCallback, LogoutCallback {

	private MenuItem loginMenu;
	private MenuItem logoutMenu;
	private MenuItem quickRepMenu;
	private MenuItem pinMenu;
	private MenuItem unpinMenu;
	private MenuItem settingMenu;
	private MenuItem loginWithPresetMenu;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		setListenerForMenuItems(menu);
		return true;
	}

	protected void setListenerForMenuItems(Menu menu) {
		settingMenu = menu.findItem(R.id.action_settings);
		settingMenu.setOnMenuItemClickListener(this);
		MenuItem refresh = menu.findItem(R.id.action_refresh);
		refresh.setOnMenuItemClickListener(this);
		checkMenuItemStatus();
	}

	private void checkMenuItemStatus() {
		// set menu items base on login status
		if (VozCache.instance().getCookies() == null) { // not logged in yet
			logoutMenu.setVisible(false);			
			loginMenu.setVisible(true);
			loginWithPresetMenu.setVisible(true);
		} else { // logged in
			logoutMenu.setVisible(true);			
			loginMenu.setVisible(false);
			loginWithPresetMenu.setVisible(false);
		}		
		if (VozCache.instance().canShowReplyMenu()) {
			if(VozCache.instance().getCookies() != null) {// logged in
				quickRepMenu.setVisible(true);
			} else {
				quickRepMenu.setVisible(false);
			}
		} else {
			quickRepMenu.setVisible(false);
		}
		
		if (canShowPinnedMenu()) {
			pinMenu.setVisible(true);
		} else {
			pinMenu.setVisible(false);
		}
		
		if (canShowUnpinnedMenu()) {
			unpinMenu.setVisible(true);
		} else {
			unpinMenu.setVisible(false);
		}
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_refresh:
			refresh();
			break;
		case R.id.action_settings:
			showSetting();
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

	private void doRep() {
		// do nothing
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
		// do nothing
	}

	protected void doPin() {
		
	}
	
	protected void doUnpin() {
		
	}
	
	@Override
	public void preProcess(int position, View convertView, Object... extra) {
		// TODO Auto-generated method stub

	}

	@Override
	public void postProcess(int position, View convertView, Object... extra) {
		// TODO Auto-generated method stub

	}

	@Override
	public void lastBreath(Exception ex) {
		ex.printStackTrace(); // in case you want to see the stacktrace in your
								// log cat output
		BugSenseHandler.sendException(ex);
	}

	@Override
	public void doAfterLogout(boolean result) {
		if (result) {
			checkMenuItemStatus();
			refresh();
		}
	}
	
	protected boolean canShowPinnedMenu() {
		return false;
	}
	
	protected boolean canShowUnpinnedMenu() {
		return false;
	}
}
