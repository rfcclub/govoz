package com.gotako.gofast;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;

import com.gotako.gofast.event.FieldChangeEvent;
import com.gotako.gofast.event.FieldChangeListener;
import com.gotako.gofast.utils.ReflectionUtil;

public class ReactiveField extends BindingObject implements FieldChangeListener {	
	/**
	 * field name for react
	 */
	private String fieldName;
	/**
	 * list contains listener for field on the source object
	 */
	private List<FieldChangeListener> listenerList;

	/**
	 * Construct a ReactiveField with Context and field name.
	 * 
	 * @param source
	 *            source which contains field
	 * @param fieldName
	 *            name of the field
	 */
	public ReactiveField(Object source, String fieldName) {
		this(source, ReflectionUtil.getField(source, fieldName));
	}

	public ReactiveField(Object source, Field field) {
		this.source = source;
		this.field = field;
		listenerList = new ArrayList<FieldChangeListener>();
	}

	/**
	 * Raise a FieldChangeEvent on this reflect field. It will call method
	 * onFieldChange(ev) on all listeners.
	 * 
	 * @param ev
	 *            event needs to raise
	 */
	@Override
	public void setValue(Object value) {
		setValue(value, false);
	}

	public void setValue(Object value, boolean notified) {
		this.value = value;
		updateUnderlyingValue(value);
		if (notified) {
			raiseFieldChangeEvent();
		}
	}

	public void raiseFieldChangeEvent() {
		try {
			// value = field.get(source);
			value = ReflectionUtil.getValue(source, field, true);
			FieldChangeEvent ev = new FieldChangeEvent(source, field.getName(),
					value);
			doRaiseFieldChangeEvent(ev);
		} catch (IllegalArgumentException e) {
			System.out.println(field.getName() + ":" + value.toString());
			throw new RuntimeException(e);
		} catch (RuntimeException e) {
			System.out.println(field.getName() + ":" + value.toString());
			throw new RuntimeException(e);
		}
	}

	private void doRaiseFieldChangeEvent(FieldChangeEvent ev) {
		for (int i = 0; i < listenerList.size(); i++) {
			FieldChangeListener listener = listenerList.get(i);
			listener.onFieldChange(ev);
		}
	}

	/**
	 * Update underlying value of ViewModel by using Reflection.
	 */
	private void updateUnderlyingValue(Object value) {
		if (source == null) {
			dispose();
			return;
		}
		try {
			/*
			 * if (!field.isAccessible()) { field.setAccessible(true); }
			 */
			// field.set(source, value);
			ReflectionUtil.setValue(source, field, value, true);
		} catch (IllegalArgumentException e) {
			System.out.println(field.getName() + ":" + value.toString());
			throw new RuntimeException(e);
		} catch (RuntimeException e) {
			System.out.println(field.getName() + ":" + value.toString());
			throw e;
		}
	}

	/**
	 * Dispose all things so VM can collect.
	 */
	private void dispose() {
		value = null;
		source = null;
		field = null;
		listenerList = null;

	}

	/**
	 * Add listener to listen for the change in underlying ViewModel
	 * 
	 * @param listener
	 *            listener to add
	 */
	public void addFieldChangeListener(FieldChangeListener listener) {
		this.listenerList.add(listener);
	}

	/*
	 * Set value in event back to view.
	 * 
	 * @see
	 * com.gotako.gofast.event.FieldChangeListener#onFieldChange(com.gotako.
	 * gofast.event.FieldChangeEvent)
	 */
	@Override
	public void onFieldChange(FieldChangeEvent event) {
		GoFastEngine.instance().setValueOnView(view, event.getValue());
	}

	/**
	 * Get view is used to link in View-ViewModel.
	 * 
	 * @return the view
	 */
	public View getView() {
		return view;
	}

	/**
	 * Set view is used to link in View-ViewModel.
	 * 
	 * @param view
	 *            the view to set
	 */
	public void setView(View view) {
		this.view = view;
	}

	/**
	 * Get value is being hold by ViewModel
	 * 
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @return the fieldName
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * @param fieldName
	 *            the fieldName to set
	 */
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gotako.gofast.BindingObject#bind(android.view.View)
	 */
	@Override
	public void bind(View view) {
		addFieldChangeListener(this);
	}	

	public List<FieldChangeListener> getListenerList() {
		return listenerList;
	}

	public void setListenerList(List<FieldChangeListener> listenerList) {
		this.listenerList = listenerList;
	}
}
