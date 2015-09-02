package com.gotako.govoz;

import static com.gotako.govoz.VozConstant.*;
import java.util.List;

import android.content.Context;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;
import com.bugsense.trace.ExceptionCallback;
import com.gotako.govoz.tasks.UserLoginTask;
import com.gotako.govoz.tasks.VozMainForumDownloadTask;

public class AutoLoginBackgroundService implements
ActivityCallback<Boolean>, ExceptionCallback {

	private Context context;

	public AutoLoginBackgroundService(Context context) {
		super();
		this.context = context;
	}

	public void doLogin(String username, String password) {
		UserLoginTask mAuthTask = new UserLoginTask(this);
		mAuthTask.execute(username, password);
		Toast.makeText(context, "Try to login with preset username/password", Toast.LENGTH_SHORT).show();
	}
	@Override
	public void lastBreath(Exception ex) {
		ex.printStackTrace(); // in case you want to see the stacktrace in your log cat output
		BugSenseHandler.sendException(ex);		
	}

	@Override
	public void doCallback(List<Boolean> result, Object... extra) {
		if(result != null && result.get(0) == true) {
			Toast.makeText(context, "Login successfully !!", Toast.LENGTH_SHORT).show();	
			VozMainForumDownloadTask task = new VozMainForumDownloadTask(null);
			task.setContext(context);		
			task.setShowProcessDialog(false);
			task.execute(VOZ_LINK);
		} else {
			Toast.makeText(context, "Cannot login. Maybe wrong username/password or network is not good", Toast.LENGTH_SHORT).show();
		}		
	}
	
}
