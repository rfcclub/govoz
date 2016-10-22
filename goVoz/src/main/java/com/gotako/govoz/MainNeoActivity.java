package com.gotako.govoz;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;

import com.gotako.govoz.data.Thread;

import info.hoang8f.android.segment.SegmentedGroup;

import static com.gotako.govoz.VozConstant.FORUM_URL_F;
import static com.gotako.govoz.VozConstant.FORUM_URL_ORDER;
import static com.gotako.govoz.VozConstant.VOZ_LINK;

public class MainNeoActivity extends VozFragmentActivity
        implements MainFragment.OnFragmentInteractionListener,
        ForumFragment.OnFragmentInteractionListener,
        ThreadFragment.OnFragmentInteractionListener {

    protected Fragment mFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_main_neo);
        processToMainForum(savedInstanceState);
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
    public void onPostClicked(String postLink) {

    }

    @Override
    public void onThreadClicked(String postLink) {

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
        mFragment = forumFragment;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_container, forumFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onOutsideLinkClicked(String postLink) {

    }

    @Override
    public void onOutsidePictureClicked(String postLink) {

    }

    @Override
    public void onThreadClicked(Thread thread) {
        VozCache.instance().setCurrentThread(thread.getId());
        VozCache.instance().setCurrentThreadPage(1);
        String url = VOZ_LINK + "/"
                + thread.getThreadUrl()
                + "&page="
                + String.valueOf(VozCache.instance().getCurrentThreadPage());
        NavigationItem forumItem = new NavigationItem(url, NavigationItem.THREAD);
        VozCache.instance().mNeoNavigationList.add(forumItem);
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
        SegmentedGroup navigationGroup = (SegmentedGroup)findViewById(R.id.navigation_group);
        if(navigationGroup == null) return;
        navigationGroup.removeAllViews();
        if (visible) {
            NavigationItem item = VozCache.instance().currentNavigateItem();
            LayoutInflater mInflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            int currentPage = item.mCurrentPage;
            if (currentPage > 2) {
                RadioButton first = (RadioButton) mInflater.inflate(R.layout.navigation_button, null);
                first.setText("<<");
                first.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goFirst();
                    }
                });
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
    }

    private void goToPage(int page) {
        if(mFragment instanceof PageNavigationListener) {
            ((PageNavigationListener) mFragment).goToPage(page);
        }
    }

    private void goLast() {
        if(mFragment instanceof PageNavigationListener) {
            ((PageNavigationListener) mFragment).goLast();
        }
    }

    private void goFirst() {
        if(mFragment instanceof PageNavigationListener) {
            ((PageNavigationListener) mFragment).goFirst();
        }
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount() > 0) {
            VozCache.instance().removeLastNavigationLink();
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
        overridePendingTransition(R.animator.left_slide_in_half, R.animator.right_slide_out);
    }
}
