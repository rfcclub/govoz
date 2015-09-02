package com.gotako.govoz.tasks;

import static com.gotako.govoz.VozConstant.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.gotako.govoz.LogoutCallback;
import com.gotako.govoz.VozCache;

public class UserLogoutTask extends AsyncTask<String, Integer, Boolean> {

	private LogoutCallback callback;
	private ProgressDialog progressDialog;
	private Context context;	
	public UserLogoutTask(Context context) {
		this.context = context;
	}
	@Override
	protected Boolean doInBackground(String...params) {
		
		boolean result = false;
		String logoutLink = VOZ_LINK + "/login.php?do=logout&logouthash=";
		logoutLink += VozCache.instance().getSecurityToken();
		try {
			TaskHelper.disableSSLCertCheck();
			Document document = Jsoup.connect(logoutLink)
								.data("do","logout")								
								.data("logouthash",
										VozCache.instance().getSecurityToken()) 
					            .post();
			VozCache.instance().setCookies(null);
			VozCache.instance().setSecurityToken("guest");			
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			
		}
		return result;
	}
	/**
	 * @param callback the callback to set
	 */
	public void setCallback(LogoutCallback callback) {
		this.callback = callback;
	}
	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(Boolean result) {
		if(progressDialog !=null) {
			progressDialog.dismiss();
		}
		if(callback != null) callback.doAfterLogout(result);		
	}

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	@Override
	protected void onPreExecute() {
		progressDialog = new ProgressDialog(context);
		progressDialog.setMessage("Logging out...");			
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setIndeterminate(true);			
		progressDialog.show();
	}
	
	
}
