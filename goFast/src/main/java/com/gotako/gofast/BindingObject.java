/**
 * 
 */
package com.gotako.gofast;

import java.lang.reflect.Field;

import android.view.View;

/**
 * @author Nam
 *
 */
public abstract class BindingObject {
	/**
	 * Source contain field, which is Activity normally
	 */	
	Object source;
	Field  field;
	Object value;
	View view;
	public BindingObject(Object source, Field field, Object value, View view) {
		super();
		this.source = source;
		this.field = field;
		this.value = value;
		this.view = view;
	}
	public BindingObject() {
		super();
		// TODO Auto-generated constructor stub
	}
	/**
	 * @return the field
	 */
	public Field getField() {
		return field;
	}
	/**
	 * @param field the field to set
	 */
	public void setField(Field field) {
		this.field = field;
	}
	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}
	/**
	 * @return the view
	 */
	public View getView() {
		return view;
	}
	/**
	 * @param view the view to set
	 */
	public void setView(View view) {
		this.view = view;
	}
	
	public Object getSource() {
		return source;
	}

	public void setSource(Object source) {
		this.source = source;
	}
	
	/**
	 * Abstract method for binding object
	 * @param view
	 */
	public abstract void bind(View view);
}
