package com.gotako.govoz;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;

import com.bugsense.trace.BugSenseHandler;

import static com.gotako.govoz.VozConstant.FORUM_URL_F;
import static com.gotako.govoz.VozConstant.FORUM_URL_ORDER;

public class MainNeoActivity extends VozFragmentActivity
        implements MainFragment.OnFragmentInteractionListener, ForumFragment.OnFragmentInteractionListener {

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
            if(savedInstanceState != null) return;
            MainFragment mainFragment = MainFragment.newInstance();
            getSupportFragmentManager()
                                    .beginTransaction()
                                    .add(R.id.fragment_container, mainFragment)
                                    .commit();
        }
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
                .commit();
    }

    @Override
    public void onThreadClicked(Thread thread) {

    }
}
