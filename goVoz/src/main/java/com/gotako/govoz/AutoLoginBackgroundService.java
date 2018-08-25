package com.gotako.govoz;

import android.content.Context;
import android.widget.Toast;

import com.bugsense.trace.ExceptionCallback;
import com.gotako.govoz.tasks.UserLoginTask;
import com.gotako.govoz.tasks.VozMainForumDownloadTask;
import com.gotako.util.Utils;

import java.util.List;

import static com.gotako.govoz.VozConstant.VOZ_LINK;

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
		Toast.makeText(context, Utils.getString(context, R.string.left_menu_login_with_pref), Toast.LENGTH_SHORT).show();
	}
	@Override
	public void lastBreath(Exception ex) {
		ex.printStackTrace(); // in case you want to see the stacktrace in your log cat output
		// BugSenseHandler.sendException(ex);
	}

	@Override
	public void doCallback(CallbackResult<Boolean> callbackResult) {
		List<Boolean> result = callbackResult.getResult();
		if(result != null && result.get(0) == true) {
			Toast.makeText(context, Utils.getString(context,R.string.login_success), Toast.LENGTH_SHORT).show();
			VozMainForumDownloadTask task = new VozMainForumDownloadTask(null);
			task.setContext(context);		
			task.setShowProcessDialog(false);
			task.execute(VOZ_LINK);
			((VozFragmentActivity)context).doAfterAutoLogin();
		} else {
			Toast.makeText(context, Utils.getString(context,R.string.error_incorrect_password), Toast.LENGTH_SHORT).show();
		}		
	}
	
}
