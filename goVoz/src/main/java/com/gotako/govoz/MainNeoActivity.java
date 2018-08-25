package com.gotako.govoz;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.RadioButton;

import com.gotako.govoz.data.Thread;
import com.gotako.govoz.service.VozVpnService;
import com.gotako.util.Utils;

import info.hoang8f.android.segment.SegmentedGroup;

import static com.gotako.govoz.VozConstant.FORUM_URL_F;
import static com.gotako.govoz.VozConstant.FORUM_URL_ORDER;
import static com.gotako.govoz.VozConstant.VOZ_LINK;

public class MainNeoActivity extends VozFragmentActivity
        implements MainFragment.OnFragmentInteractionListener,
        ForumFragment.OnFragmentInteractionListener,
        ThreadFragment.OnFragmentInteractionListener,
        SearchFragment.OnFragmentInteractionListener,
        InboxFragment.OnFragmentInteractionListener,
        InboxDetailFragment.OnFragmentInteractionListener {

    protected VozFragment mFragment;
    private static final int VPN_REQUEST_CODE = 0x0F;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (VozConfig.instance().isUsingDnsOverVpn()) {
            startVpn();
        }
        // setContentView(R.layout.activity_main_neo);
        processToMainForum(savedInstanceState);
    }

    private void startVpn() {
        Intent intent = VpnService.prepare(this);
        if (intent != null)
        {
            startActivityForResult(intent, VPN_REQUEST_CODE);
        }
        else
        {
            onActivityResult(VPN_REQUEST_CODE, RESULT_OK, null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == VPN_REQUEST_CODE) {
            startService(new Intent(this, VozVpnService.class).setAction(VozVpnService.ACTION_CONNECT));
        }
    }

    @Override
    protected void forceRefresh() {
        if (mFragment != null) mFragment.forceRefresh();
    }

    private void processToMainForum(Bundle savedInstanceState) {
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        VozCache.instance().setHeight(outMetrics.heightPixels);
        VozCache.instance().setWidth(outMetrics.widthPixels);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        VozCache.instance().reset();
        VozCache.instance().setCanShowReplyMenu(false);
        VozConfig config = VozConfig.instance();
        config.load(this);

        SharedPreferences prefs = getSharedPreferences(VozConstant.VOZINFO, Context.MODE_PRIVATE);
        if (prefs.contains(VozConstant.USERNAME) && prefs.contains(VozConstant.PASSWORD)) {
            // if not login so do login
            if (VozCache.instance().getCookies() == null) {
                String username = prefs.getString(VozConstant.USERNAME, "guest");
                String password = prefs.getString(VozConstant.PASSWORD, "guest");
                AutoLoginBackgroundService albs = new AutoLoginBackgroundService(this);
                albs.doLogin(username, password);
            }
        }

        if (findViewById(R.id.frame_container) != null) {
            if (savedInstanceState != null) return;
            MainFragment mainFragment = MainFragment.newInstance();
            mFragment = mainFragment;
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.frame_container, mainFragment)
                    .commit();
        }

    }

    @Override
    protected void showMenu() {
        if (mFragment instanceof PageNavigationListener) {
            PopupMenu popupMenu = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                popupMenu = new PopupMenu(this, findViewById(R.id.voz_toolbar), Gravity.RIGHT);
            } else {
                popupMenu = new PopupMenu(this, findViewById(R.id.voz_toolbar));
            }
            if (mFragment instanceof ThreadFragment) {
                popupMenu.getMenuInflater().inflate(R.menu.post, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener((ThreadFragment) mFragment);
            } else if (mFragment instanceof ForumFragment) {
                popupMenu.getMenuInflater().inflate(R.menu.thread_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener((ForumFragment) mFragment);
            }
            popupMenu.show();
        }
    }

    @Override
    public void onForumClicked(String forumIndex) {
        VozCache.instance().setCurrentForum(Integer.parseInt(forumIndex));
        VozCache.instance().setCurrentForumPage(1);
        VozCache.instance().cache().clear();
        String forumUrl = FORUM_URL_F + forumIndex + FORUM_URL_ORDER + "1";
        NavigationItem forumItem = new NavigationItem(forumUrl, NavigationItem.FORUM);
        VozCache.instance().mNeoNavigationList.add(forumItem);
        ForumFragment forumFragment = ForumFragment.newInstance();
        VozCache.instance().setCanShowReplyMenu(false);
        mFragment = forumFragment;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_container, forumFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    protected void doInbox() {
        String pmHttpsLink = VOZ_LINK + "/" + "private.php";
        NavigationItem item = new NavigationItem(pmHttpsLink, NavigationItem.INBOX);
        VozCache.instance().mNeoNavigationList.add(item);
        InboxFragment inboxFragment = InboxFragment.newInstance();
        mFragment = inboxFragment;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_container, inboxFragment)
                .addToBackStack(null)
                .commit();
    }



    @Override
    public void onThreadClicked(Thread thread) {
        String url = VOZ_LINK + "/"
                + thread.getThreadUrl()
                + "&page=1";
        goToThreadId(thread.getId(), url);
    }

    public void goToThreadId(int threadId, String threadUrl) {
        VozCache.instance().setCurrentThread(threadId);
        VozCache.instance().setCurrentThreadPage(1);

        NavigationItem threadItem = new NavigationItem(threadUrl, NavigationItem.THREAD);
        VozCache.instance().addNavigationItem(threadItem);
        VozCache.instance().setCanShowReplyMenu(true);
        threadItem.mCurrentPage = 1;
        ThreadFragment threadFragment = ThreadFragment.newInstance();
        mFragment = threadFragment;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_container, threadFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void updateNavigationPanel(boolean visible) {
        SegmentedGroup navigationGroup = (SegmentedGroup) findViewById(R.id.navigation_group);
        if (navigationGroup == null) return;
        navigationGroup.removeAllViews();
        if (visible) {
            NavigationItem item = VozCache.instance().currentNavigateItem();
            LayoutInflater mInflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            int currentPage = item.mCurrentPage;
            if (currentPage > 2) {
                RadioButton first = (RadioButton) mInflater.inflate(R.layout.navigation_button, null);
                first.setText("<<");
                first.setOnClickListener(v -> goFirst());
                navigationGroup.addView(first);
            }
            int prevStart = currentPage - 1;
            if (prevStart < 1) prevStart = 1;
            int extraRight = 0;
            for (int i = prevStart; i <= currentPage; i++) {
                RadioButton prevPage = (RadioButton) mInflater.inflate(R.layout.navigation_button, null);
                prevPage.setText(String.valueOf(i));
                prevPage.setTag(i);
                final int page = i;
                if (i == currentPage) prevPage.setChecked(true);
                prevPage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goToPage(page);
                    }
                });
                navigationGroup.addView(prevPage);
                extraRight++;
            }
            int nextEnd = currentPage + 1;
            if (nextEnd < 4) nextEnd = 4;
            if (nextEnd > item.mLastPage) nextEnd = item.mLastPage;
            for (int i = currentPage + 1; i <= nextEnd; i++) {
                RadioButton nextPage = (RadioButton) mInflater.inflate(R.layout.navigation_button, null);
                nextPage.setText(String.valueOf(i));
                nextPage.setTag(i);
                final int page = i;
                nextPage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goToPage(page);
                    }
                });
                navigationGroup.addView(nextPage);
            }
            if (nextEnd < item.mLastPage) {
                RadioButton last = (RadioButton) mInflater.inflate(R.layout.navigation_button, null);
                last.setText(">>");
                last.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goLast();
                    }
                });
                navigationGroup.addView(last);
            }
            navigationGroup.updateBackground();
            navigationGroup.requestLayout();
            navigationGroup.invalidate();
        }
        refreshActionBarIcon();
    }



    @Override
    public void reload() {
        reloadCurrentFragment();
    }

    @Override
    public void onSessionExpired() {
        Intent intent = new Intent(getApplicationContext(), MainNeoActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToPage(int page) {
        if (mFragment instanceof PageNavigationListener) {
            ((PageNavigationListener) mFragment).goToPage(page);
        }
    }

    private void goLast() {
        if (mFragment instanceof PageNavigationListener) {
            ((PageNavigationListener) mFragment).goLast();
        }
    }

    private void goFirst() {
        if (mFragment instanceof PageNavigationListener) {
            ((PageNavigationListener) mFragment).goFirst();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /*
     * This method has error, need review
     */
    private void doResumeChecking() {
        long currentMillis = System.currentTimeMillis();
        boolean needReload = false;
        if ((Utils.convertToMinutes(currentMillis - VozCache.instance().milliSeconds) >= 20)
            || (VozCache.instance().mNeoNavigationList.size() == 0 && mFragment != null && !(mFragment instanceof MainFragment))) {
            VozCache.instance().setCookies(null);
            VozCache.instance().setUserId("guest");
            refreshLinks();
            reloadActivity();
        }
    }

    private void reloadActivity() {
        if(getSupportFragmentManager().getBackStackEntryCount() > 0) {
            VozCache.instance().mNeoNavigationList.clear();
            VozCache.instance().setCanShowReplyMenu(false);
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else {
            if(mFragment instanceof InboxFragment || mFragment instanceof InboxDetailFragment) {
                MainFragment mainFragment = MainFragment.newInstance();
                mFragment = mainFragment;
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame_container, mainFragment)
                        .commit();
            }
        }

    }

    @Override
    public void refresh() {
        mFragment.onResume();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            VozCache.instance().removeLastNavigationLink();
            VozCache.instance().setCanShowReplyMenu(false);
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
        overridePendingTransition(R.animator.left_slide_in_half, R.animator.right_slide_out);
    }

    @Override
    public void onPMClicked(String pmLink) {
        NavigationItem item = new NavigationItem(VozConstant.VOZ_LINK + "/" + pmLink, NavigationItem.INBOX_DETAIL);
        VozCache.instance().mNeoNavigationList.add(item);
        InboxDetailFragment inboxFragment = InboxDetailFragment.newInstance();
        mFragment = inboxFragment;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_container, inboxFragment)
                .addToBackStack(null)
                .commit();
    }


    public void quickSearch(String searchString, String showPosts) {
        NavigationItem item = new NavigationItem(null, NavigationItem.SEARCH);
        VozCache.instance().mNeoNavigationList.add(item);
        SearchFragment searchFragment = SearchFragment.newInstance(searchString, showPosts);
        mFragment = searchFragment;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_container, searchFragment)
                .addToBackStack(null)
                .commit();
    }

    public void reloadCurrentFragment() {
        // TODO: Seems it has error, try to go to main page
        if(getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else {
            Intent intent = new Intent(this, MainNeoActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onOutsideLinkClicked(String postLink) {

    }

    @Override
    public void onOutsidePictureClicked(String postLink) {

    }

    @Override
    public void onPostClicked(String postLink) {

    }

    @Override
    public void onThreadClicked(String postLink) {

    }

    @Override
    public void onLinkClick(String link) {

    }

    @Override
    public void rateThread() {
        FragmentManager fm = getSupportFragmentManager();
        RatingDialog ratingDialog = new RatingDialog();
        ratingDialog.setActivity(this);
        ratingDialog.show(fm, "rating");
    }

    @Override
    public void showPageSelectDialog() {
        PageSelectDialog pageSelectDialog = new PageSelectDialog();
        pageSelectDialog.setActivity(this);
        pageSelectDialog.setTitle("Go to page:");
        pageSelectDialog.show(getSupportFragmentManager(), "selectPage");
    }

    @Override
    public void lastBreath(Exception ex) {
        super.lastBreath(ex);
        VozCache.instance().clearCache();
    }
}
