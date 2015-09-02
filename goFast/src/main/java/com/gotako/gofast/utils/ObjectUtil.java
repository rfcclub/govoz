package com.gotako.gofast.utils;

import android.widget.CheckBox;

public class ObjectUtil {
	public static boolean isBoolean(Object value) {
		return value instanceof Boolean;
	}

	public static boolean isString(Object value) {		
		return value instanceof String;
	}
	
	public static boolean isInteger(Object value) {		
		return value instanceof Integer;
	}

	public static Boolean parseBoolean(Object value) {
		if(value == null) {
			return null;
		}
		if (isBoolean(value)) {
			return (Boolean) value;
		} 
		if (isBooleanString(value)) {
			return Boolean.parseBoolean(((String) value).toLowerCase());
		}
		return null;
	}

	private static boolean isBooleanString(Object value) {		
		return (value instanceof String) && ("true".equals(((String)value).toLowerCase()) || "false".equals(((String)value).toLowerCase()));
	}
	
	public static String parseString(Object value) {
		if(value instanceof String) {
			return (String)value;
		} else if (value instanceof Number) {
			return String.valueOf(value);
		} else {
			return value.toString();
		}
	}
}
