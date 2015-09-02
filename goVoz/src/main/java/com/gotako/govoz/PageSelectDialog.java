/**
 * 
 */
package com.gotako.govoz;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.gotako.util.Utils;

/**
 * @author HA LINH
 *
 */
public class PageSelectDialog extends android.support.v4.app.DialogFragment {

	private Activity activity;
	
	private EditText editText;
	
	public PageSelectDialog() {}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

		return dialog;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		final View view = inflater.inflate(R.layout.select_page_layout, container);

	    DisplayMetrics displaymetrics = new DisplayMetrics();
	    activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
	    int width = displaymetrics.widthPixels;
	    
		getDialog().getWindow().setLayout((int) (width * 0.65), 440);
		editText = (EditText) view.findViewById(R.id.txtPageNumber);
		
		((TextView) view.findViewById(R.id.txtViewPage1)).setText("Số trang tối đa: " + ((ForumActivity) activity).getLastPage());
		
		((Button) view.findViewById(R.id.btnGo)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				goToPage();
			}
		});
		
		((Button) view.findViewById(R.id.btnCancel)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				cancel();
			}
		});
		
		return view;
	}

	public void goToPage() {
		int page =  Utils.convertInt(editText.getText().toString(), VozCache.instance().getCurrentForumPage());
		VozCache.instance().setCurrentForumPage(page);
		((ForumActivity) activity).loadThreads();
		getDialog().dismiss();
	}
	
	public void cancel() {
		getDialog().dismiss();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.copyFrom(getDialog().getWindow().getAttributes());
		
	    DisplayMetrics displaymetrics = new DisplayMetrics();
	    activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
	    int width = displaymetrics.widthPixels;
		
		lp.width = (int) (width * 0.65);
		getDialog().getWindow().setAttributes(lp);
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

}
