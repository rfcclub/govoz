package com.gotako.govoz;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.bugsense.trace.ExceptionCallback;
import com.gotako.gofast.GoFastEngine;
import com.gotako.gofast.annotation.BindingField;
import com.gotako.govoz.tasks.PostReplyTask;
import com.gotako.util.Utils;

import java.util.List;

public class PostActivity extends VozFragmentActivity implements ActivityCallback<Boolean>, ExceptionCallback {

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
		return true;
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
