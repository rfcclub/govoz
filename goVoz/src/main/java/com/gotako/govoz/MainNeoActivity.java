package com.gotako.govoz;

import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;

import com.bugsense.trace.BugSenseHandler;

public class MainNeoActivity extends VozFragmentActivity implements MainFragment.OnFragmentInteractionListener {

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
    public void onFragmentInteraction(Uri uri) {
        // do nothing for now
    }
}
