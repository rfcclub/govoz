/**
 * 
 */
package com.gotako.gofast.event;

import java.util.EventObject;

import android.content.Context;

/**
 * @author Nam
 *
 */
public class FieldChangeEvent extends EventObject {	
	/**
	 * Generate serial version id.
	 */
	private static final long serialVersionUID = 1946937207862449352L;
	private String fieldName;
	private Object value;
	public FieldChangeEvent(Object source) {
		super(source);		
	}

	public FieldChangeEvent(Object source,String fieldName) {
		this(source);
		this.fieldName = fieldName;
	}	
	
	
	public FieldChangeEvent(Object source, String fieldName, Object value) {
		this(source,fieldName);		
		this.value = value;
	}

	/**
	 * @return the fieldName
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * @param fieldName the fieldName to set
	 */
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
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
}
