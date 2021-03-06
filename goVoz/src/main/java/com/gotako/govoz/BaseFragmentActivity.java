/**
 * 
 */
package com.gotako.govoz;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.gotako.govoz.adapter.NavDrawerListAdapter;
import com.gotako.govoz.adapter.VozMenuListAdapter;
import com.gotako.govoz.data.NavDrawerItem;
import com.gotako.govoz.data.VozMenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lnguyen66
 *
 */
public abstract class BaseFragmentActivity extends AppCompatActivity {
	protected DrawerLayout mDrawerLayout;
    protected ListView mDrawerListView;
    protected ListView mRightForumListView;
    protected ListView mRightLinkListView;
    protected ActionBarDrawerToggle mDrawerToggle;
 
    // used to store app title
    protected CharSequence mTitle;
 
    // slide menu items
    protected LinearLayout mLayoutSlidePanel;
    protected List<VozMenuItem> mNavDrawerItemsList;
    protected List<NavDrawerItem> mForumPinItemsList;
    protected List<NavDrawerItem> mThreadPinItemsList;
    protected VozMenuListAdapter mLeftMenuAdapter;
    protected NavDrawerListAdapter mForumPinAdapter;
    protected NavDrawerListAdapter mThreadPinAdapter;
    protected Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_with_nav);
        mToolbar = (Toolbar) findViewById(R.id.voz_toolbar);
        final LinearLayout mainContent = (LinearLayout) findViewById(R.id.main_content);
        setSupportActionBar(mToolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerListView = (ListView) findViewById(R.id.list_slidermenu);
        mRightForumListView = (ListView) findViewById(R.id.list_right_menu_forum);
        mRightLinkListView = (ListView) findViewById(R.id.list_right_menu_thread);
        mLayoutSlidePanel = (LinearLayout) findViewById(R.id.layout_slidermenu);
        createMenuList();
        mNavDrawerItemsList = VozCache.instance().menuItemList;
        mForumPinItemsList = VozCache.instance().pinItemForumList;
        mThreadPinItemsList = VozCache.instance().pinItemThreadList;
        // enabling action bar app icon and behaving it as toggle button
        changeDefaultActionBar();
        // setting the nav drawer list adapter
        mLeftMenuAdapter = new VozMenuListAdapter(getBaseContext(), mNavDrawerItemsList);
        mDrawerListView.setAdapter(mLeftMenuAdapter);
        mForumPinAdapter = new NavDrawerListAdapter(this, mForumPinItemsList);
        mRightForumListView.setAdapter(mForumPinAdapter);
        mThreadPinAdapter = new NavDrawerListAdapter(this, mThreadPinItemsList);
        mRightLinkListView.setAdapter(mThreadPinAdapter);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                mToolbar, //nav menu toggle icon
                R.string.app_name, // nav drawer open - description for accessibility
                R.string.app_name // nav drawer close - description for accessibility
        ){
            public void onDrawerClosed(View view) {
                invalidateOptionsMenu();
            }
 
            public void onDrawerOpened(View drawerView) {
                //getActionBar().setTitle(mDrawerTitle);
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                float moveFactor = (mDrawerListView.getWidth() * slideOffset);
                if(drawerView.getId() == R.id.layout_slidermenu) {
                    mainContent.setTranslationX(moveFactor);
                } else {
                    mainContent.setTranslationX(-moveFactor);
                }
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(false);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerListView.setOnItemClickListener(createItemClickListener());
        mDrawerToggle.setToolbarNavigationClickListener(v -> {
            if (VozCache.instance().mNeoNavigationList.size() > 0) {
                onBackPressed();
            } else {
                if (!(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)) {
                    boolean drawerOpen = mDrawerLayout.isDrawerOpen(mLayoutSlidePanel);
                    if (!drawerOpen) {
                        mDrawerLayout.openDrawer(GravityCompat.START);
                    }
                }
            }
        });
        mDrawerLayout.post(() -> mDrawerToggle.syncState());

    }

    protected abstract void createMenuList();


    protected abstract AdapterView.OnItemClickListener createItemClickListener();

    protected void changeDefaultActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        refreshActionBarIcon();
    }

    public void refreshActionBarIcon() {
        if (VozCache.instance().mNeoNavigationList.size() > 0) {
            mToolbar.setNavigationIcon(R.drawable.back);
            mToolbar.invalidate();
        } else {
            mToolbar.setNavigationIcon(R.drawable.menu);
            mToolbar.invalidate();
        }
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
//        menu.findItem(R.id.action_refresh).setIcon(R.drawable.reload);
//        menu.findItem(R.id.action_rmenu).setIcon(R.drawable.rmenu);
        // if nav drawer is opened, hide the action items
        // boolean drawerOpen = mDrawerLayout.isDrawerOpen(mLayoutSlidePanel);
        return super.onPrepareOptionsMenu(menu);
    }
 
    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
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
    
    @Override
    public void onBackPressed() {
    	super.onBackPressed();
        if(mDrawerLayout.isDrawerOpen(Gravity.LEFT)){
        	mDrawerLayout.closeDrawer(Gravity.LEFT);
        } else if(mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            mDrawerLayout.closeDrawer(Gravity.RIGHT);
        }
        List<String> list = VozCache.instance().navigationList;
        // remove last element
        if(list.size() > 0) list.remove(list.size() - 1);
        finish();
        overridePendingTransition(R.animator.left_slide_in_half, R.animator.right_slide_out);
    }

	@Override
	protected void onResume() {
		super.onResume();
        if(mDrawerLayout.isDrawerOpen(Gravity.LEFT)){
        	mDrawerLayout.closeDrawer(Gravity.LEFT);
        } else if(mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            mDrawerLayout.closeDrawer(Gravity.RIGHT);
        }
        changeDefaultActionBar();
	}


    public abstract void savePinForumsList();
    public abstract void savePinThreadsList();
}
