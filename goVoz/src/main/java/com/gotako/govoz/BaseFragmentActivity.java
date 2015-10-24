/**
 * 
 */
package com.gotako.govoz;

import java.util.List;

import com.gotako.govoz.adapter.VozMenuListAdapter;
import com.gotako.govoz.data.VozMenuItem;
import com.gotako.util.Utils;

import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
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
public abstract class BaseFragmentActivity extends AppCompatActivity {
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

    protected VozMenuListAdapter leftMenuAdapter;
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
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);
        layoutSlidePanel = (LinearLayout) findViewById(R.id.layout_slidermenu);
        createLeftMenu();

        navDrawerItems = VozCache.instance().menuItemList;
        // enabling action bar app icon and behaving it as toggle button
        changeDefaultActionBar();
        // setting the nav drawer list adapter
        leftMenuAdapter = new VozMenuListAdapter(getBaseContext(), navDrawerItems);
        mDrawerList.setAdapter(leftMenuAdapter);

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
                float moveFactor = (mDrawerList.getWidth() * slideOffset);
                if(drawerView.getId() == R.id.layout_slidermenu) {
                    mainContent.setTranslationX(moveFactor);
                } else {
                    mainContent.setTranslationX(-moveFactor);
                }
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(false);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerList.setOnItemClickListener(createItemClickListener());
        mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (VozCache.instance().navigationList.size() > 0) {
                    onBackPressed();
                } else {
                    boolean drawerOpen = mDrawerLayout.isDrawerOpen(layoutSlidePanel);
                    if (!drawerOpen) {
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

    protected abstract AdapterView.OnItemClickListener createItemClickListener();
    protected abstract void createLeftMenu();
    protected abstract void createRightMenu();

    protected void changeDefaultActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        refreshActionBarIcon();
    }

    public void refreshActionBarIcon() {
        if (VozCache.instance().navigationList.size() > 0) {
            mToolbar.setNavigationIcon(Utils.getValueByTheme(R.drawable.ic_arrow_back_white_18dp, R.drawable.ic_arrow_back_black_18dp));
            mToolbar.invalidate();
        } else {
            mToolbar.setNavigationIcon(Utils.getValueByTheme(R.drawable.ic_menu_white_18dp, R.drawable.ic_menu_black_18dp));
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
        menu.findItem(R.id.action_refresh).setIcon(Utils.getValueByTheme(R.drawable.ic_refresh_white_18dp, R.drawable.ic_refresh_black_18dp));
        menu.findItem(R.id.action_reply).setIcon(Utils.getValueByTheme(R.drawable.ic_comment_white_18dp, R.drawable.ic_comment_black_18dp));
        // if nav drawer is opened, hide the action items
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(layoutSlidePanel);
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
        List<String> list = VozCache.instance().navigationList;
        // remove last element
        if(list.size() > 0) list.remove(list.size() - 1);
    }

	@Override
	protected void onResume() {
		super.onResume();
        if(mDrawerLayout.isDrawerOpen(Gravity.LEFT)){
        	mDrawerLayout.closeDrawer(Gravity.LEFT);
        }
        changeDefaultActionBar();
	}
    
    
}
