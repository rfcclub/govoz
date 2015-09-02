package com.gotako.gofast.bind;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.gotako.gofast.BindingObject;
import com.gotako.gofast.listener.DefaultOnClickListener;
import com.gotako.gofast.listener.DefaultOnItemSelectedListener;
import com.gotako.gofast.listener.TextFieldWatcher;

/**
 * Helper class for binding actions. It is built under static class to save cost of initialize.
 * @author Nam
 *
 */
public class InputBindingHelper {
	/**
	 * Bind input value from View to ViewModel
	 * 
	 * @param view
	 *            View need to bind
	 * @param field
	 *            ViewModel wrapped in BindingObject.
	 */
	public static void bindInput(View view, BindingObject field) {
		if (view instanceof TextView) {
			TextView text = (TextView) view;
			bindInputTextView(text, field);
		} else if (view instanceof EditText) {
			EditText edit = (EditText) view;
			bindInputEditView(edit, field);
		} else if (view instanceof ProgressBar) {
			bindInputEditView((ProgressBar) view, field);
		} else if (view instanceof CompoundButton) {
			bindInputEditView((CompoundButton) view, field);
		} /*
		 * else if (view instanceof RadioButton) {
		 * bindInputEditView((RadioButton) view, field); }
		 */else if (view instanceof Spinner) {
			bindInputEditView((Spinner) view, field);
		}
	}

	/*
	 * private void bindInputEditView(RadioButton view, BindingObject field) {
	 * view.setOnClickListener(new DefaultOnClickListener(field)); }
	 */

	private static void bindInputEditView(Spinner view, BindingObject field) {
		view.setOnItemSelectedListener(new DefaultOnItemSelectedListener(field));
	}

	private static void bindInputEditView(CompoundButton view, BindingObject field) {
		view.setOnClickListener(new DefaultOnClickListener(field));
	}

	private static void bindInputEditView(ProgressBar view, BindingObject field) {
		// TODO
	}

	/**
	 * Bind input specially for EditText
	 * 
	 * @param edit
	 *            EditText
	 * @param field
	 *            BindingObject
	 */
	private static void bindInputEditView(EditText edit, BindingObject field) {
		edit.addTextChangedListener(new TextFieldWatcher(field));
	}

	/**
	 * Bind input specially for TextView
	 * 
	 * @param text
	 *            TextView
	 * @param field
	 *            BindingObject
	 */
	private static void bindInputTextView(TextView text, final BindingObject field) {
		text.addTextChangedListener(new TextFieldWatcher(field));
	}
}
