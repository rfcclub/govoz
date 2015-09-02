/**
 * 
 */
package com.gotako.gofast.listener;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

import com.gotako.gofast.BindingObject;
import com.gotako.gofast.ReactiveField;

/**
 * @author Nam
 *
 */
public class DefaultOnItemSelectedListener implements OnItemSelectedListener {
	
	BindingObject field;
	public DefaultOnItemSelectedListener(BindingObject obj) {
		this.field =obj;
	}

	/* (non-Javadoc)
	 * @see android.widget.AdapterView.OnItemSelectedListener#onItemSelected(android.widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		if(field instanceof ReactiveField) {
			ReactiveField reactField = (ReactiveField)field;
			reactField.setValue(parent.getItemAtPosition(position));
		}
	}

	/* (non-Javadoc)
	 * @see android.widget.AdapterView.OnItemSelectedListener#onNothingSelected(android.widget.AdapterView)
	 */
	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub

	}

}
