package com.gotako.govoz;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;

import com.bugsense.trace.BugSenseHandler;
import com.gotako.govoz.data.Thread;

import static com.gotako.govoz.VozConstant.FORUM_URL_F;
import static com.gotako.govoz.VozConstant.FORUM_URL_ORDER;

public class MainNeoActivity extends VozFragmentActivity
        implements MainFragment.OnFragmentInteractionListener,
        ForumFragment.OnFragmentInteractionListener,
        ThreadFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        BugSenseHandler.initAndStartSession(this, "2330a14e");
        BugSenseHandler.setLogging(1000, "*:W");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_neo);

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

        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) return;
            MainFragment mainFragment = MainFragment.newInstance();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, mainFragment)
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
        VozCache.instance().navigationList.add(forumUrl);
        ForumFragment forumFragment = ForumFragment.newInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, forumFragment)
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

    }

    @Override
    public void updateNavigationPanel() {

    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount() > 0) {
            VozCache.instance().removeLastNavigationLink();
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}
