package com.gotako.govoz;

import java.util.List;

import com.bugsense.trace.BugSenseHandler;
import com.bugsense.trace.ExceptionCallback;
import com.gotako.govoz.tasks.UserLoginTask;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.Menu;
import android.widget.Toast;

public class AutoLoginActivity extends Activity implements
		ActivityCallback<Boolean>, ExceptionCallback {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_auto_login);
		SharedPreferences prefs = this.getSharedPreferences("VOZINFO",
				Context.MODE_PRIVATE);
		if (prefs.contains("USERNAME") && prefs.contains("PASSWORD")) {
			if (VozCache.instance().getCookies() == null) {
				String username = prefs.getString("USERNAME", "guest");
				String password = prefs.getString("PASSWORD", "guest");
				UserLoginTask mAuthTask = new UserLoginTask(this);
				mAuthTask.execute(username, password);
			}
		}
		setTitle("");		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.auto_login, menu);
		return true;
	}

	@Override
	public void doCallback(List<Boolean> result, Object... extra) {
		if(result != null && result.get(0) == true) {
			Toast.makeText(this, "Login successfully !!!", Toast.LENGTH_SHORT).show();			
		} else {
			Toast.makeText(this, "Cannot login. Wrong username/password", Toast.LENGTH_SHORT).show();
		}
		finish();
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}

	@Override
	public void lastBreath(Exception ex) {
		ex.printStackTrace(); // in case you want to see the stacktrace in your log cat output
		BugSenseHandler.sendException(ex);
	}
}
