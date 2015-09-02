package com.gotako.gofast.utils;

public class StringUtil {

	public static String convertToString(Object value) {
		if (value instanceof String) {
			return (String) value;
		} else if (value instanceof Number) {
			return String.valueOf(value);
		} else { // use toString()
			return value.toString();
		}
	}
}
