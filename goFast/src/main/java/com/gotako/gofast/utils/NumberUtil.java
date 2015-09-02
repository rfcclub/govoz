package com.gotako.gofast.utils;

public class NumberUtil {
	public static Number convertToNumber(Class<?> clazz, Object value) {
		if (clazz.isAssignableFrom(Integer.class)
				|| clazz.isAssignableFrom(int.class)) {
			return convertToInt(value);
		} else if (clazz.isAssignableFrom(Long.class)
				|| clazz.isAssignableFrom(long.class)) {
			return convertToLong(value);
		} else if (clazz.isAssignableFrom(Float.class)
				|| clazz.isAssignableFrom(float.class)) {
			return convertToFloat(value);
		} else if (clazz.isAssignableFrom(Double.class)
				|| clazz.isAssignableFrom(double.class)) {
			return convertToDouble(value);
		} else {
			return convertToInt(value);
		}

	}

	/**
	 * Convert Object to double
	 * 
	 * 
	 * @param value
	 *            value to convert
	 * @return a double
	 */
	private static Number convertToDouble(Object value) {
		if (value instanceof String) {
			return Double.parseDouble((String) value);
		} else if (value instanceof Number) {
			return (Double) value;
		} else { // use toString() and parseInt
			return Double.parseDouble(value.toString());
		}
	}

	/**
	 * @param value
	 * @return
	 */
	private static Number convertToFloat(Object value) {
		if (value instanceof String) {
			return Float.parseFloat((String) value);
		} else if (value instanceof Number) {
			return (Float) value;
		} else { // use toString() and parseInt
			return Float.parseFloat(value.toString());
		}
	}

	private static Number convertToLong(Object value) {
		if (value instanceof String) {
			return Long.parseLong((String) value);
		} else if (value instanceof Number) {
			return (Long) value;
		} else { // use toString() and parseInt
			return Long.parseLong(value.toString());
		}
	}

	public static Number convertToInt(Object value) {
		if (value instanceof String) {
			return Integer.parseInt((String) value);
		} else if (value instanceof Number) {
			return (Integer) value;
		} else { // use toString() and parseInt
			return Integer.parseInt(value.toString());
		}
	}
}
