package com.gotako.govoz;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.bugsense.trace.ExceptionCallback;
import com.gotako.gofast.GoFastEngine;
import com.gotako.gofast.annotation.BindingField;
import com.gotako.govoz.tasks.PostReplyTask;
import com.gotako.util.Utils;

import java.util.List;

public class PostActivity extends VozFragmentActivity implements ActivityCallback<Boolean> {

	private static final int SMILEY_REQUEST = 1;
	@BindingField(id = R.id.titleText, twoWay = true)
	String titleText = "";
	@BindingField(id = R.id.answerText, twoWay = true)
	String answerText = "";
	private String replyLink;
    private EditText answerTextEdit;

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
		GoFastEngine.notify(this, "titleText");
		replyLink = getIntent().getStringExtra("replyLink");
		String quote = getIntent().getStringExtra("quote");		
		answerText = (quote == null ? "" : quote + "\n");
		GoFastEngine.notify(this, "answerText");
        answerTextEdit = (EditText) layout.findViewById(R.id.answerText);
        if(!Utils.isNullOrEmpty(answerText)) {
            answerTextEdit.setSelection(answerTextEdit.getText().length());
        }
        doTheming();
        layout.findViewById(R.id.postRootLayout).setBackgroundColor(Utils.getColorByTheme(this, R.color.black, R.color.voz_back_color));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.post_menu, menu);
		MenuItem insertAvatar = menu.findItem(R.id.action_insert_avatar);
		insertAvatar.setOnMenuItemClickListener(this);
		return true;
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_insert_avatar:
				Intent intent = new Intent(this, VozSmiliesActivity.class);
				startActivityForResult(intent, SMILEY_REQUEST);
				break;
			default: // last option

		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == SMILEY_REQUEST && resultCode == VozConstant.GET_SMILEY_OK) {
			String code = data.getStringExtra("code");
			String content = answerTextEdit.getText() + code;
			answerTextEdit.setText(content);
			answerTextEdit.setSelection(answerTextEdit.getText().length());
		}
	}

	public void post(View view) {
		String tString = String.valueOf(VozCache.instance().getCurrentThread());
		PostReplyTask task = new PostReplyTask(this);
		task.execute(tString, answerText, titleText, replyLink);
	}

	@Override
	public void doCallback(CallbackResult<Boolean> callbackResult) {
		setResult(1);
		finish();
	}

    public void addSmiley(View view) {
        String smiley = (String)view.getTag();
        String content = answerTextEdit.getText() + smiley;
        answerTextEdit.setText(content);
        answerTextEdit.setSelection(answerTextEdit.getText().length());
    }
}
