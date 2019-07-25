package com.gotako.govoz;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by tunam on 10/30/16.
 */

public class VozFragment extends Fragment {

    SwipeRefreshLayout layout;

    @Override
    public void onResume() {
        super.onResume();
        doRefresh();
    }

    protected void initializeSwipeToRefresh(View view) {
        if (layout == null) {
            layout = view.findViewById(R.id.swipe_refresh_layout);
            layout.setOnRefreshListener(()-> {
                new Handler().postDelayed(()-> {
                    layout.setRefreshing(false);
                }, 1000);
                forceRefresh();
            });
        }
    }
    protected View createViewWithSwipeToRefresh(LayoutInflater inflater, int fragment_layout, ViewGroup container,
                                                Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_layout, container, false);
        initializeSwipeToRefresh(view);
        return view;
    }
    protected void doRefresh() {
        // empty method
    }

    public void forceRefresh() {
        // empty method
    }

    public void goToPage(int page) {
        // empty method
    }
}
