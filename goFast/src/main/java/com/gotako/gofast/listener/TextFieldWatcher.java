/**
 * 
 */
package com.gotako.gofast.listener;

import android.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;

import com.gotako.gofast.BindingObject;

/**
 * @author Nam
 *
 */
public class TextFieldWatcher implements TextWatcher {

	BindingObject field;
	public TextFieldWatcher(BindingObject field) {
		this.field = field;
	}
	/* (non-Javadoc)
	 * @see android.text.TextWatcher#afterTextChanged(android.text.Editable)
	 */
	@Override
	public void afterTextChanged(Editable s) {
		String setVal = s.toString();
		field.setValue(setVal);		
	}

	/* (non-Javadoc)
	 * @see android.text.TextWatcher#beforeTextChanged(java.lang.CharSequence, int, int, int)
	 */
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see android.text.TextWatcher#onTextChanged(java.lang.CharSequence, int, int, int)
	 */
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// field.setValue(s);

	}

}
