package com.gotako.gofast;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ExpandableListView;

import com.gotako.gofast.adapter.BindingAdapter;
import com.gotako.gofast.adapter.ExpandableAdapter;
import com.gotako.gofast.adapter.SimpleObjectAdapter;
import com.gotako.gofast.utils.ReflectionUtil;

public class ReactiveCollectionField<T> extends BindingObject {

	private Context context;
	private String fieldName;
	private int itemView;
	private int groupView;
	private Map<T, ReactiveObject> reactObjMap;
	private BindingAdapter adapter;

	public ReactiveCollectionField(Context source, String fieldName, int itemView) {
		this.context = source;
		setSource(source);
		this.fieldName = fieldName;
		this.itemView = itemView;
		Field resolveField = ReflectionUtil.getField(source, fieldName);
		this.field = resolveField;
		reactObjMap = new HashMap<T, ReactiveObject>();
	}

	public ReactiveCollectionField(Context source, String fieldName, int itemView, int groupView) {
		this(source, fieldName, itemView);
		this.groupView = groupView;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void bind(View view) {
		if (!(view instanceof AbsListView)) {
			throw new RuntimeException("Not support controls which are not ListView/GridView");
		}
		this.view = view;

		if (view instanceof ExpandableListView) {
			adapter = new ExpandableAdapter<T>(context, (Object[]) value, groupView, itemView);
			((ExpandableListView) view).setAdapter((ExpandableAdapter<T>) adapter);
			((ExpandableAdapter<T>) adapter).notifyDataSetChanged();
		} else {
			adapter = new SimpleObjectAdapter<T>(context, itemView, (List<T>) value, reactObjMap);
			// adapter.setDataSource((List<T>)value);
			if (view instanceof AbsListView) {
				((AbsListView) view).setAdapter((SimpleObjectAdapter<T>) adapter);
			}
			adapter.notifyDataSetChanged();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gotako.gofast.BindingObject#setValue(java.lang.Object)
	 */
	@Override
	public void setValue(Object value) {
		if (value == null) {
			throw new RuntimeException("Datasource is NULL or not a list contains require class type");
		}
		this.value = value;
	}

	public BindingAdapter getAdapter() {
		return adapter;
	}

	public String getFieldName() {
		return fieldName;
	}

	public Context getContext() {
		return context;
	}

}
