package com.gotako.govoz;

import android.support.v4.app.Fragment;

/**
 * Created by tunam on 10/30/16.
 */

public class VozFragment extends Fragment {
    @Override
    public void onResume() {
        super.onResume();
        doRefresh();
    }

    protected void doRefresh() {

    }

    public void forceRefresh() {

    }
}
