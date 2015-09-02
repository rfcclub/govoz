package com.gotako.gofast.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class ReflectionUtil {
	/**
	 * Set value by accessible or settter
	 * 
	 * @param source
	 *            object source
	 * @param field
	 *            field to set
	 * @param value
	 *            value to set
	 * @param setAccessible
	 *            if true, try to set accessible if field.isAccessible() ==
	 *            false
	 */
	public static void setValue(Object source, Field field, Object value,
			boolean setAccessible) {
		try {
			if (!field.isAccessible() && setAccessible) {
				field.setAccessible(setAccessible);
			}
			if (field.isAccessible()) {
				doSetValue(source, field, value);
			} else { // try to set by setter
				String setter = "set"
						+ field.getName().substring(0, 1).toUpperCase()
						+ field.getName().substring(1);
				Method method = source.getClass().getMethod(setter,
						field.getDeclaringClass());
				method.invoke(source, value);
			}
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}

	}

	private static void doSetValue(Object source, Field field, Object value)
			throws IllegalArgumentException, IllegalAccessException {
		Class<?> clazz = field.getType();
		field.set(source, castToClass(clazz, value));
	}

	public static Object castToClass(Class<?> clazz, Object value) {
		
		if (clazz.isAssignableFrom(String.class)) {
			return StringUtil.convertToString(value);
		} else if (  clazz.isAssignableFrom(Number.class) 
				  || clazz.isAssignableFrom(int.class) 
				  || clazz.isAssignableFrom(long.class) 
				  || clazz.isAssignableFrom(float.class) 
				  || clazz.isAssignableFrom(double.class)) {
			return NumberUtil.convertToNumber(clazz,value);
		} else { // try to do the cast on object
			return clazz.cast(value);
		}
	}

	/**
	 * Set value by accessible or settter
	 * 
	 * @param source
	 *            source object
	 * @param field
	 *            field needs to get value
	 * @param setAccessible
	 *            if true, try to set accessible if field.isAccessible() ==
	 *            false
	 * @return value Object
	 */
	@SuppressWarnings("finally")
	public static Object getValue(Object source, Field field,
			boolean setAccessible) {
		Object ret = null;
		try {
			if (field.isAccessible()) {
				ret = field.get(source);
			} else {
				if (setAccessible) {
					field.setAccessible(setAccessible);
					ret = field.get(source);
				} else { // try to set by setter
					String setter = "get"
							+ field.getName().substring(0, 1).toUpperCase()
							+ field.getName().substring(1);
					Method method = source.getClass().getMethod(setter, null);
					ret = method.invoke(source);
				}
			}
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} finally {
			return ret;
		}
	}
	
	public static Field getField(Object source,String fieldName) {
		if (source == null) {
			throw new RuntimeException("Context {} is null when get field " + fieldName);
		}
		Field resolveField = null;
		try {
			resolveField = source.getClass().getDeclaredField(fieldName);			
		} catch (NoSuchFieldException e) {
			Field[] fields = source.getClass().getFields();
			for (int i = 0; i < fields.length; i++) {
				if (fields[i].getName().equals(fieldName)) {
					resolveField = fields[i];
					break;
				}
			}			
		}
		return resolveField;
	}
	
	public static void invokeVoidMethod(Object source, String name, Class paramClass, Object...args) {
		try {
			Method method = source.getClass().getMethod(name, paramClass);
			method.invoke(source, args);

		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}		
	}
}
