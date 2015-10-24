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
import android.widget.BaseExpandableListAdapter;

import com.gotako.gofast.ReactiveObject;
import com.gotako.gofast.listener.BindingActionListener;
import com.gotako.gofast.listener.OnCreateViewListener;

/**
 * @author lnguyen66
 * 
 */
public class ExpandableAdapter<T> extends BaseExpandableListAdapter implements
		BindingAdapter {

	private List<T> _listDataHeader; // header titles
	// child data in format of header title, child title
	private Map<Integer, List<T>> _listDataChild;
	private int childViewId;
	private int groupViewId;
	private AdapterBindingCore<T> adapterCore;
	private OnCreateViewListener onCreateViewListener;

	public ExpandableAdapter(Context context, Object[] dataSource,
			int groupViewId, int childViewId) {
		doInitialize(context, dataSource, groupViewId, childViewId, null);
	}

	public ExpandableAdapter(Context context, Object[] dataSource,
			int groupViewId, int childViewId,
			Map<T, ReactiveObject> bindingCache) {
		doInitialize(context, dataSource, groupViewId, childViewId,
				bindingCache);
	}

	@SuppressWarnings("unchecked")
	private void doInitialize(Context context, Object[] dataSource,
			int groupViewId, int childViewId,
			Map<T, ReactiveObject> bindingCache) {
		this._listDataHeader = (List<T>) dataSource[0];
		this._listDataChild = (Map<Integer, List<T>>) dataSource[1];
		this.childViewId = childViewId;
		this.groupViewId = groupViewId;
		if (bindingCache == null) {
			adapterCore = new AdapterBindingCore<T>(context);
		} else {
			adapterCore = new AdapterBindingCore<T>(context, bindingCache);
		}
	}

	@Override
	public Object getChild(int groupPosition, int childPosititon) {

		List<T> list = this._listDataChild.get(groupPosition);
		return list.get(childPosititon);
	}

	@Override
	public long getChildId(int arg0, int childPosition) {
		return childPosition;
	}

	@SuppressWarnings("unchecked")
	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		final T instance = (T) getChild(groupPosition, childPosition);

		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) adapterCore.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(childViewId, null);
		}

		ReactiveObject reactObject = adapterCore.getReactiveObject(instance);
		adapterCore.binding(groupPosition, v, reactObject, parent,
				childPosition);
		if(onCreateViewListener !=null) onCreateViewListener.onGetChildView(groupPosition, childPosition, isLastChild, v, parent);
		return v;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return this._listDataChild.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return this._listDataHeader.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return this._listDataHeader.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@SuppressWarnings("unchecked")
	@Override
	public View getGroupView(int groupPosition, boolean isLastChild,
			View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) adapterCore.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(groupViewId, null);
		}
		T instance = (T) getGroup(groupPosition);
		ReactiveObject reactObject = adapterCore.getReactiveObject(instance);
		adapterCore.binding(groupPosition, v, reactObject, parent);
		if(onCreateViewListener !=null) onCreateViewListener.onGetGroupView(groupPosition, isLastChild, v, parent);
		return v;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
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

	public BindingActionListener getBindingActionListener() {
		return adapterCore.getBindingActionListener();
	}

	public void setBindingActionListener(
			BindingActionListener bindingActionListener) {
		adapterCore.setBindingActionListener(bindingActionListener);
	}

	/*
	 * @Override public void notifyDataSetChanged() {
	 * this.notifyDataSetChanged(); }
	 */

	@SuppressWarnings("unchecked")
	@Override
	public void setDataSource(Object datasource) {
		if (!(datasource instanceof Map)) {
			return;
		}
		this._listDataHeader = (List<T>) (((Object[]) datasource)[0]);
		this._listDataChild = (Map<Integer, List<T>>) (((Object[]) datasource)[1]);
	}

	public void setOnCreateViewListener(OnCreateViewListener onCreateViewListener) {
		this.onCreateViewListener = onCreateViewListener;
	}
}
