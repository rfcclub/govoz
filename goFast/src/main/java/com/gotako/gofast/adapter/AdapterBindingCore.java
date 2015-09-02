package com.gotako.gofast.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.gotako.gofast.ReactiveObject;
import com.gotako.gofast.listener.BindingActionListener;
import com.gotako.gofast.utils.BindUtil;

public class AdapterBindingCore<T> {
	protected Context context;
	protected Map<T, ReactiveObject> bindingCache;
	protected Map<Integer, String> nameCache;
	private BindingActionListener bindingActionListener = null;

	public AdapterBindingCore(Context context) {
		this.context = context;
		bindingCache = null;
		nameCache = new HashMap<Integer,String>();
	}

	public AdapterBindingCore(Context context,
			Map<T, ReactiveObject> bindingCache) {
		this(context);
		this.bindingCache = bindingCache;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public ReactiveObject getReactiveObject(T instance) {
		ReactiveObject reactObject = null;
		if (bindingCache != null) {
			reactObject = bindingCache.get(instance);
			if (reactObject == null) {
				reactObject = new ReactiveObject(instance);
				bindingCache.put(instance, reactObject);
			}
		}

		if (reactObject == null) { // does not use bindingCache
			// create new binding object for each time
			reactObject = new ReactiveObject(instance);
		}
		return reactObject;
	}

	public void binding(int position, View v, ReactiveObject reactObject,
			Object... extra) {
		preBindingAction(position, v, extra);
		bindingInternal(position, v, reactObject);
		postBindingAction(position, v, extra);
	}

	public void bindingInternal(int position, View v, ReactiveObject reactObject) {
		// update data from underlying layer
		if (v instanceof ViewGroup) {
			// bind all child views in v have same names with properties of
			// reactive object.
			ViewGroup viewGroup = (ViewGroup) v;
			bindViewGroup(reactObject, viewGroup);
		} else {
			// bind view by name of view
			String name = v.getResources().getResourceEntryName(v.getId());
			boolean isBinded = reactObject.tryBindProperty(name, v, true);
			// update viewmodel value to view first.
			if (isBinded)
				reactObject.firePropertyChange(name);
		}
	}

	private void bindViewGroup(ReactiveObject reactObject, ViewGroup viewGroup) {
		int childViewCount = viewGroup.getChildCount();
		for (int i = 0; i < childViewCount; i++) {
			View childView = viewGroup.getChildAt(i);
			if (childView instanceof ViewGroup) {
				ViewGroup subGroup = (ViewGroup) childView;
				bindViewGroup(reactObject, subGroup);
			} else if (BindUtil.isBindable(childView)) {
				bindViewChild(childView, reactObject, viewGroup);
			}
		}
	}

	private void bindViewChild(View childView, ReactiveObject reactObject,
			ViewGroup viewGroup) {
		String name = nameCache.get(childView.getId());
		if(name == null) {
			name = viewGroup.getResources().getResourceEntryName(
				childView.getId());
			nameCache.put(childView.getId(), name);
		} 
		boolean isBinded = reactObject.tryBindProperty(name, childView, true);
		// update viewmodel value to view first
		if (isBinded) {
			reactObject.firePropertyChange(name);
		}
	}

	protected void preBindingAction(int position, View v, Object... extra) {
		if (bindingActionListener != null)
			bindingActionListener.preProcess(position, v, extra);
	}

	protected void postBindingAction(int position, View v, Object... extra) {
		if (bindingActionListener != null)
			bindingActionListener.postProcess(position, v, extra);
	}

	@SuppressWarnings("unchecked")
	public void removeRedundantCache(List<T> list) {
		// TODO: Optimize this method to reuse ReactiveObject
		Object[] keySet = bindingCache.keySet().toArray();
		for (int i = keySet.length - 1; i >= 0; i--) {
			T item = (T) bindingCache.get(keySet[i]);
			if (!existsInDataSource(list, item))
				bindingCache.remove(item);
		}
	}

	private boolean existsInDataSource(List<T> dataSource, T item) {
		for (T check : dataSource) {
			if (check.equals(item))
				return true;
		}
		return false;
	}

	public Map<T, ReactiveObject> getBindingCache() {
		return bindingCache;
	}

	public void setBindingCache(Map<T, ReactiveObject> bindingCache) {
		this.bindingCache = bindingCache;
	}

	public BindingActionListener getBindingActionListener() {
		return bindingActionListener;
	}

	public void setBindingActionListener(
			BindingActionListener bindingActionListener) {
		this.bindingActionListener = bindingActionListener;
	}

}
