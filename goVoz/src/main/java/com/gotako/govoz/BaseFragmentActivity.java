/**
 * 
 */
package com.gotako.govoz;

import static com.gotako.govoz.VozConstant.*;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.gotako.govoz.adapter.NavDrawerListAdapter;
import com.gotako.govoz.adapter.VozMenuListAdapter;
import com.gotako.govoz.data.Forum;
import com.gotako.govoz.data.NavDrawerItem;
import com.gotako.govoz.data.VozMenuItem;
import com.gotako.util.Utils;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
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
import android.widget.RelativeLayout;

/**
 * @author lnguyen66
 *
 */
public class BaseFragmentActivity extends AppCompatActivity {
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
    private LinearLayout layoutSlidePanel;
    protected List<VozMenuItem> navDrawerItems;

    private VozMenuListAdapter adapter;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_with_nav);
        mToolbar = (Toolbar) findViewById(R.id.voz_toolbar);
        setSupportActionBar(mToolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);
        layoutSlidePanel = (LinearLayout) findViewById(R.id.layout_slidermenu);
        initLeftMenu();

        navDrawerItems = VozCache.instance().menuItemList;
        // enabling action bar app icon and behaving it as toggle button
        changeDefaultActionBar();
        // setting the nav drawer list adapter
        adapter = new VozMenuListAdapter(getBaseContext(), navDrawerItems);
        mDrawerList.setAdapter(adapter);

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

        };
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());
        mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (VozCache.instance().navigationList.size() > 0) {
                    onBackPressed();
                } else {
                    boolean drawerOpen = mDrawerLayout.isDrawerOpen(layoutSlidePanel);
                    if(!drawerOpen) {
                        mDrawerLayout.openDrawer(GravityCompat.START);
                    }
                }
            }
        });
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

    }

    private void initLeftMenu() {
        if (VozCache.instance().menuItemList == null) {
            VozCache.instance().menuItemList = new ArrayList<VozMenuItem>();
            VozCache.instance().menuItemList.add(new VozMenuItem("Trang chủ", R.drawable.ic_home_white_18dp, 0));
            VozCache.instance().menuItemList.add(new VozMenuItem("Đi đến Thread", R.drawable.ic_open_in_new_white_18dp, 1));
            VozCache.instance().menuItemList.add(new VozMenuItem("Đi đến Forum", R.drawable.ic_open_in_new_white_18dp, 2));
            VozCache.instance().menuItemList.add(new VozMenuItem("Hộp thư", R.drawable.ic_mail_white_18dp, 3));
            VozCache.instance().menuItemList.add(new VozMenuItem("Tìm kiếm", R.drawable.ic_search_white_18dp, 4));
            VozCache.instance().menuItemList.add(new VozMenuItem("Cài đặt", R.drawable.ic_settings_white_18dp, 5));
            VozCache.instance().menuItemList.add(new VozMenuItem("Thoát", R.drawable.ic_phone_android_white_18dp, 6));
        }
    }

    protected void changeDefaultActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        refreshActionBarIcon();
    }

    public void refreshActionBarIcon() {
        if (VozCache.instance().navigationList.size() > 0) {
            mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_18dp);
            mToolbar.invalidate();
        } else {
            mToolbar.setNavigationIcon(R.drawable.ic_menu_white_18dp);
            mToolbar.invalidate();
        }
    }

    /**
     * Slide menu item click listener
     * */
    private class SlideMenuClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
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

                        break;
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    default:
                        break;
                }
            }
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
        // if nav drawer is opened, hide the action items
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(layoutSlidePanel);
        menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
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
