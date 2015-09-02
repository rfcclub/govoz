package com.gotako.gofast.listener;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;

import com.gotako.gofast.BindingObject;
import com.gotako.gofast.GoFastEngine;

public class DefaultOnClickListener implements OnClickListener {

	BindingObject field;
	public DefaultOnClickListener(BindingObject field) {
		this.field = field;
	}
	
	@Override
	public void onClick(View v) {
		GoFastEngine.instance().setValueOnField(field,v);
	}

}
