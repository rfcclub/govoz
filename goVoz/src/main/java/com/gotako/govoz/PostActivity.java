package com.gotako.govoz;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;

import com.bugsense.trace.BugSenseHandler;
import com.bugsense.trace.ExceptionCallback;
import com.gotako.gofast.GoFastEngine;
import com.gotako.gofast.annotation.BindingField;
import com.gotako.govoz.tasks.PostReplyTask;

public class PostActivity extends VozFragmentActivity implements ActivityCallback<Boolean>, ExceptionCallback {

	@BindingField(id = R.id.titleText, twoWay = true)
	String titleText = "";
	@BindingField(id = R.id.answerText, twoWay = true)
	String answerText = "";
	private String replyLink;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_post);
		
		LayoutInflater mInflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		View layout = mInflater.inflate(R.layout.activity_post, null);
		
		FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_container);
		frameLayout.addView(layout);
		
		GoFastEngine.initialize(this);
		titleText ="Re: " + getIntent().getStringExtra("threadName");
		replyLink = getIntent().getStringExtra("replyLink");
		GoFastEngine.notify(this, "titleText");
		String quote = getIntent().getStringExtra("quote");		
		answerText = (quote == null ? "" : quote);
		GoFastEngine.notify(this, "answerText");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.post, menu);
		return true;
	}

	public void post(View view) {
		String tString = String.valueOf(VozCache.instance().getCurrentThread());
		PostReplyTask task = new PostReplyTask(this);
		task.execute(tString, answerText, titleText, replyLink);
	}

	@Override
	public void doCallback(List<Boolean> result, Object... extra) {
		setResult(1);
		finish();
	}
	
	@Override
	public void lastBreath(Exception ex) {
		ex.printStackTrace(); // in case you want to see the stacktrace in your log cat output
		BugSenseHandler.sendException(ex);
	}
}
