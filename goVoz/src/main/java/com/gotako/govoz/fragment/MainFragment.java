package com.gotako.govoz.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gotako.govoz.R;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * 
 */
public class MainFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.activity_main_with_nav, container,false);
	}

}
