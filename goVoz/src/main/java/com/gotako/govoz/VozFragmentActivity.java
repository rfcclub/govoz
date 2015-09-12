/**
 * 
 */
package com.gotako.govoz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;
import com.bugsense.trace.ExceptionCallback;
import com.gotako.gofast.listener.BindingActionListener;
import com.gotako.govoz.tasks.UserLogoutTask;

/**
 * @author lnguyen66
 * 
 */
public class VozFragmentActivity extends BaseFragmentActivity implements
		OnMenuItemClickListener,BindingActionListener, ExceptionCallback,LogoutCallback {

	private MenuItem loginMenu;
	private MenuItem logoutMenu;
	private MenuItem quickRepMenu;
	protected MenuItem pinMenu;
	protected MenuItem unpinMenu;
	private MenuItem settingMenu;
	private MenuItem loginWithPresetMenu;
	protected boolean threadIsClosed;

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
		loginMenu = menu.findItem(R.id.action_login);
		loginMenu.setOnMenuItemClickListener(this);
		MenuItem refresh = menu.findItem(R.id.action_refresh);
		refresh.setOnMenuItemClickListener(this);
		logoutMenu = menu.findItem(R.id.action_logout);
		logoutMenu.setOnMenuItemClickListener(this);
		quickRepMenu = menu.findItem(R.id.action_quickrep);
		quickRepMenu.setOnMenuItemClickListener(this);
		
		pinMenu = menu.findItem(R.id.action_pin);
		pinMenu.setOnMenuItemClickListener(this);
		
		loginWithPresetMenu = menu.findItem(R.id.action_login_preset);
		loginWithPresetMenu.setOnMenuItemClickListener(this);
		
		unpinMenu = menu.findItem(R.id.action_unpin);
		unpinMenu.setOnMenuItemClickListener(this);
		
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
		case R.id.action_login:
			doLogin();
			break;
		case R.id.action_login_preset:
			doLoginWithPreset();
			break;	
		case R.id.action_refresh:
			refresh();
			break;
		case R.id.action_logout:
			doLogout();
			break;
		case R.id.action_quickrep:
			doRep();
			break;
		case R.id.action_pin:
			doUnpin();
			break;
		case R.id.action_unpin:
			doPin();
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
	
	protected void doPin() {
		
	}
	
	protected void doUnpin() {
		
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
	
	private void doRep() {
		if(threadIsClosed) {
			CharSequence text = "Sorry! This thread is closed!";
			int duration = Toast.LENGTH_SHORT;
			Toast toast = Toast.makeText(this, text, duration);
			toast.show();	
		} else { // reply thread	
			Intent intent = new Intent(this, PostActivity.class);
			startActivity(intent);		
		}		
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
		ex.printStackTrace(); // in case you want to see the stacktrace in your log cat output
		BugSenseHandler.sendException(ex);
	}

	@Override
	public void doAfterLogout(boolean result) {
		if(result) {
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