package com.gotako.gofast.adapter;

import android.content.Context;

import com.gotako.gofast.listener.BindingActionListener;

public interface BindingAdapter {
	public void notifyDataSetChanged();

	public void setDataSource(Object datasource);

	public void setContext(Context context);

	public void setBindingActionListener(BindingActionListener listener);
}
