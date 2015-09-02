/**
 * 
 */
package com.gotako.gofast.adapter;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.gotako.gofast.ReactiveObject;
import com.gotako.gofast.listener.BindingActionListener;
import com.gotako.gofast.utils.BindUtil;

/**
 * 
 * 
 * @author Nam
 * 
 */
public class SimpleObjectAdapter<T> extends ArrayAdapter<T> implements
		BindingAdapter {

	private int itemViewId;
	private List<T> dataSource;
	private AdapterBindingCore<T> adapterCore;

	public SimpleObjectAdapter(Context context, int itemViewId, List<T> data) {
		super(context, itemViewId, data);
		this.itemViewId = itemViewId;
		this.dataSource = data;
		adapterCore = new AdapterBindingCore<T>(context);
	}

	public SimpleObjectAdapter(Context context, int itemViewId, List<T> data,
			Map<T, ReactiveObject> bindingMap) {
		super(context, itemViewId, data);
		this.itemViewId = itemViewId;
		this.dataSource = data;
		adapterCore = new AdapterBindingCore<T>(context, bindingMap);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View,
	 * android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) adapterCore.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(itemViewId, null);
		}
		T instance = dataSource.get(position);
		ReactiveObject reactObject = adapterCore.getReactiveObject(instance);
		adapterCore.binding(position, v, reactObject, parent);		
		return v;
	}	

	/**
	 * @return the itemViewId
	 */
	public int getItemViewId() {
		return itemViewId;
	}

	/**
	 * @param itemViewId
	 *            the itemViewId to set
	 */
	public void setItemViewId(int itemViewId) {
		this.itemViewId = itemViewId;
	}

	/**
	 * @return the dataSource
	 */
	public List<T> getDataSource() {
		return dataSource;
	}

	/**
	 * @param dataSource
	 *            the dataSource to set
	 */
	@SuppressWarnings("unchecked")
	public void setDataSource(Object itemObject) {
		this.dataSource = (List<T>) itemObject;
		synchronizeBaseList();
	}	

	private void synchronizeBaseList() {
		clear();
		for (T item : dataSource) {
			add(item);
		}
		adapterCore.removeRedundantCache(dataSource);
	}

	

	/**
	 * @return the context
	 */
	public Context getContext() {
		return adapterCore.getContext();
	}

	/**
	 * @param context
	 *            the context to set
	 */
	public void setContext(Context context) {
		adapterCore.setContext(context);
	}

	@Override
	public T getItem(int position) {
		return dataSource.get(position);
	}

	public void setBindingActionListener(
			BindingActionListener bindingActionListener) {
		adapterCore.setBindingActionListener(bindingActionListener);
	}
}
