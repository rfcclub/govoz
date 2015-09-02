package com.gotako.gofast.bind;

import android.view.View;
import android.widget.TextView;

import com.gotako.gofast.BindingObject;

/**
 * Static class for binding output. It is built as static class temporarily to reduce size.
 * @author Nam
 *
 */
public class OutputBindingHelper {
	/**
	 * Bind value from ViewModel output to View
	 * 
	 * @param view
	 *            View need to bind
	 * @param field
	 *            ViewModel wrapped in BindingObject.
	 */
	public static void bindOutput(View view, BindingObject field) {
		if (view instanceof TextView) {
			field.bind(view);
		}
	}
}
