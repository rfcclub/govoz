/**
 * 
 */
package com.gotako.gofast;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.view.View;

import com.gotako.gofast.utils.BindUtil;
import com.gotako.gofast.utils.ReflectionUtil;

/**
 * Implementation for reactive object which provides "react" characteristics for
 * an individual objects and its field.
 * 
 * @author Nam
 * 
 */
public class ReactiveObject implements PropertyChangeListener, Serializable {
	/**
	 * serialize id
	 */
	private static final long serialVersionUID = -7061667358359524749L;
	PropertyChangeSupport support;
	Object source;
	Map<String, BindingObject> properties;

	public ReactiveObject(Object sourceBean) {
		support = new PropertyChangeSupport(sourceBean);		
		this.source = sourceBean;
		properties = new HashMap<String, BindingObject>();
	}

	/**
	 * Bind property to a view
	 * 
	 * @param propertyName
	 * @param view
	 * @param twoWay
	 */
	public void bindProperty(String propertyName, View view, boolean twoWay) {
		// return if the view cannot be binded
		if (!BindUtil.isBindable(view))
			throw new RuntimeException("View "
					+ view.getClass().getSimpleName() + " is not supported.");
		try {
			Field field = source.getClass().getDeclaredField(propertyName);
			Object value = ReflectionUtil.getValue(source, field, true);
			BindingObject irf = new POJOBindingObject(source, field, value,
					view);
			properties.put(propertyName, irf);
			GoFastEngine.instance().bindControl(irf, view, twoWay);
			if (twoWay) { // self bind since POJO does not implement bind back.
				support.addPropertyChangeListener(propertyName, this);
				// value from underlying database will be updated to control in
				// the first time
				GoFastEngine.instance().setValueOnView(view, value);
			}
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Unbind a property
	 * 
	 * @param propertyName
	 */
	public void unbindProperty(String propertyName) {
		support.removePropertyChangeListener(propertyName, this);
		properties.remove(propertyName);
	}

	/**
	 * Unbind all properties
	 */
	public void unbindAll() {
		Iterator<String> iterator = properties.keySet().iterator();
		while (iterator.hasNext()) {
			String propertyName = iterator.next();
			support.removePropertyChangeListener(propertyName, this);
		}
		properties.clear();
	}

	/**
	 * dispose all things
	 */
	public void dispose() {
		support = null;
		source = null;
		properties = null;
	}

	/**
	 * fire a property change event
	 * 
	 * @param propertyName
	 */
	public void firePropertyChange(String propertyName) {
		POJOBindingObject obj = (POJOBindingObject) properties
				.get(propertyName);
		if (obj == null)
			throw new RuntimeException("No registration for " + propertyName);
		Object oldValue = obj.getValue();
		Object newValue = ReflectionUtil.getValue(source, obj.getField(), true);
		obj.setValue(newValue);
		support.firePropertyChange(propertyName, oldValue, newValue);
	}

	/*
	 * call back from PropertyChangeListener. (non-Javadoc)
	 * 
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.
	 * PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String propertyName = event.getPropertyName();
		POJOBindingObject obj = (POJOBindingObject) properties
				.get(propertyName);
		GoFastEngine.instance().setValueOnView(obj.getView(),
				event.getNewValue());
	}

	public boolean tryBindProperty(String name, View view, boolean twoWay) {
		try {
			bindProperty(name, view, twoWay);
		} catch (Exception e) {
			// do nothing
			return false;
		}
		return true;
	}

}
