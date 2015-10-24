package com.gotako.gofast.listener;

import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Nam on 10/24/2015.
 */
public interface OnCreateViewListener {

    void onGetGroupView(int groupPosition, boolean isLastChild, View v, ViewGroup parent);
    void onGetChildView(int groupPosition, int childPosition, boolean isLastChild, View v, ViewGroup parent);
}
