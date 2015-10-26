/**
 * 
 */
package com.gotako.gofast.utils;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * @author Nam
 * 
 */
public class BindUtil {

	public static boolean isBindable(View view) {
		return (view instanceof TextView) || (view instanceof EditText) || (view instanceof ProgressBar)
				|| (view instanceof CompoundButton) || (view instanceof Spinner);
	}

	public static String getBindKey(Object object, String propertyName) {
		return object.toString() + "_" + propertyName;
	}
}
