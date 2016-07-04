/**
 * 
 */
package com.gotako.govoz;

import static com.gotako.govoz.VozConstant.*;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.gotako.govoz.adapter.NavDrawerListAdapter;
import com.gotako.govoz.data.Forum;
import com.gotako.govoz.data.NavDrawerItem;
import com.gotako.util.Utils;

/**
 * @author lnguyen66
 * 
 */
public class BaseActivity extends Activity {
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	protected RelativeLayout mDrawerListContainer;
	
	// nav drawer title
	private CharSequence mDrawerTitle;

	// used to store app title
	private CharSequence mTitle;

	// slide menu items
	private String[] navMenuTitles;
	private TypedArray navMenuIcons;

	private List<NavDrawerItem> navDrawerItems;
	private NavDrawerListAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_with_nav);
		
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		//mDrawerListContainer = (RelativeLayout) findViewById(R.id.drawer_list_container);
		mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

		List list = null;
		try {
			list = (List)Utils.readFromFile(openFileInput(VozConstant.PIN_ITEM_FILE_NAME));
		} catch (FileNotFoundException e) {
			// ignore
		}

		if (list == null || list.size() == 0) {
			list = new ArrayList();
			buildListOfPredefinedLink(list, "f33", "33");
			buildListOfPredefinedLink(list, "f17", "17");
			buildListOfPredefinedLink(list, "f145", "145");
			buildListOfPredefinedLink(list, "Home", "1");
			VozCache.instance().pinItemForumList = list;
		}
		
		navDrawerItems = VozCache.instance().pinItemForumList;

		// setting the nav drawer list adapter
		adapter = new NavDrawerListAdapter(getApplicationContext(),
				navDrawerItems);
		mDrawerList.setAdapter(adapter);
		adapter.notifyDataSetChanged();

		// enabling action bar app icon and behaving it as toggle button
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_menu_white_18dp, // nav menu toggle icon
				R.string.app_name, // nav drawer open - description for
									// accessibility
				R.string.app_name // nav drawer close - description for
									// accessibility
		) {
			public void onDrawerClosed(View view) {
				// getActionBar().setTitle(mTitle);
				// calling onPrepareOptionsMenu() to show action bar icons
				invalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				// getActionBar().setTitle(mDrawerTitle);
				// calling onPrepareOptionsMenu() to hide action bar icons
				invalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if (savedInstanceState == null) {
			// on first time display view for first nav item
			// displayView(0);
		}
		
		mDrawerList.setOnItemClickListener(new SlideMenuClickListener());
	}

	private void buildListOfPredefinedLink(List list, String name, String id) {
		NavDrawerItem item = new NavDrawerItem(name, FORUM_URL_F + id,
				NavDrawerItem.FORUM);
		Forum f = new Forum();
		f.setId(id);
		list.add(item);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// toggle nav drawer on selecting action bar app icon/title
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle action bar actions click
		switch (item.getItemId()) {
		case R.id.action_settings:
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/***
	 * Called when invalidateOptionsMenu() is triggered
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// if nav drawer is opened, hide the action items
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		if (menu != null) {
			MenuItem item = menu.findItem(R.id.action_settings);
			if (item != null)
				item.setVisible(!drawerOpen);
			return super.onPrepareOptionsMenu(menu);
		}
		return false;
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	private class SlideMenuClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if (position < navDrawerItems.size()) {
				NavDrawerItem item = navDrawerItems.get(position);

				if (NavDrawerItem.FORUM == item.type) {
//					VozCache.instance().setCurrentForum((int)item.tag);
//					VozCache.instance().setCurrentForumPage(item.page);
					VozCache.instance().cache().clear();
					// VozCache.instance().getLookAheadPosts().clear();
					Intent intent = new Intent(BaseActivity.this,
							ForumActivity.class);
					startActivity(intent);
				} else if (NavDrawerItem.THREAD == item.type) {
//					VozCache.instance().setCurrentThread((int)item.tag);
//					VozCache.instance().setCurrentThreadPage(item.page);
					VozCache.instance().cache().clear();
					// VozCache.instance().getLookAheadPosts().clear();
					Intent intent = new Intent(BaseActivity.this,
							ThreadActivity.class);
					startActivity(intent);
				}
			}
		}
	}
	
    @Override
    public void onBackPressed() {
    	super.onBackPressed();
        if(mDrawerLayout.isDrawerOpen(Gravity.LEFT)){
        	mDrawerLayout.closeDrawer(Gravity.LEFT);
        }            
    }
    
	@Override
	protected void onResume() {
		super.onResume();
        if(mDrawerLayout.isDrawerOpen(Gravity.LEFT)){
        	mDrawerLayout.closeDrawer(Gravity.LEFT);
        } 
	}
}
