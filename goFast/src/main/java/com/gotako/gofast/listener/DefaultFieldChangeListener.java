/**
 * 
 */
package com.gotako.gofast.listener;

import android.view.View;

import com.gotako.gofast.GoFastEngine;
import com.gotako.gofast.event.FieldChangeEvent;
import com.gotako.gofast.event.FieldChangeListener;

/**
 * @author Nam
 *
 */
public class DefaultFieldChangeListener implements FieldChangeListener {

	private View view;
	public DefaultFieldChangeListener(View view) {
		this.view = view;
	}
	/* (non-Javadoc)
	 * @see com.gotako.gofast.event.FieldChangeListener#onFieldChange(com.gotako.gofast.event.FieldChangeEvent)
	 */
	@Override
	public void onFieldChange(FieldChangeEvent event) {
		GoFastEngine.instance().setValueOnView(view,event.getValue());
	}

}
