package com.gotako.gofast;

import java.lang.reflect.Field;

import android.view.View;

public class POJOBindingObject extends BindingObject {

	
	public POJOBindingObject() {
		super();		
	}

	public POJOBindingObject(Object source,Field field, Object value, View view) {
		super(source, field, value, view);		
	}

	@Override
	public void bind(View view) {
		// DO NOTHING BECAUSE IT IS A POJO.
	}

}
