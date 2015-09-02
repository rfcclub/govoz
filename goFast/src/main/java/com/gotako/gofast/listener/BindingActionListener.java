/**
 * 
 */
package com.gotako.gofast.listener;

import android.view.View;
import android.view.ViewGroup;

/**
 * @author ntu
 *
 */
public interface BindingActionListener {
	void preProcess(int position, View convertView, Object... extra);
	void postProcess(int position, View convertView, Object... extra);
}
