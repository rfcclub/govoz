package com.gotako.govoz.tasks;

import static com.gotako.govoz.VozConstant.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.os.AsyncTask;

import com.gotako.govoz.ActivityCallback;
import com.gotako.govoz.VozCache;
import com.gotako.util.Utils;

/**
 * Represents an asynchronous login/registration task used to authenticate the
 * user.
 */

public class UserLoginTask extends AsyncTask<String, String, Boolean> {	
	ActivityCallback<Boolean> callback;

	public UserLoginTask(ActivityCallback<Boolean> loginActivity) {
		this.callback = loginActivity;
	}

	@Override
	protected Boolean doInBackground(String... params) {		
		boolean hasLogged = false;		
		try {
			TaskHelper.disableSSLCertCheck();
			// String url = VOZ_LINK +"/vbdev/login_api.php";
			String url = VOZ_LINK + "/login.php?do=login";
			Connection conn = Jsoup.connect(url);
			conn = buildRequest(conn, params, "", "");
			Response response = conn.method(Method.POST).execute();
//			Document document = response.parse();
//			org.json.JSONObject first = new JSONObject(document.text());
//			if (!first.getJSONObject("userinfo").getString("username")
//					.equals(params[0])) {
//				String captcha = first.getString("captcha");
//				conn = buildRequest(conn, params, captcha, "");
//				response = conn.method(Method.POST).execute();
//				document = response.parse();
//				JSONObject second = new JSONObject(document.text());
//				String salt = second.getString("salt");
//				conn = buildRequest(conn, params, captcha, salt);
//				response = conn.method(Method.POST).execute();
//				document = response.parse();
//				JSONObject third = new JSONObject(document.text());
//				if (third.getJSONObject("userinfo").getString("username")
//						.equals(params[0])) {
//					hasLogged = true;
//				}
//			} else { // logged in and do not need to fill captcha
//				hasLogged = true;
//			}
            if (response.statusCode() == 200) {
                hasLogged = true;
            }
			if (hasLogged) {
				VozCache.instance().setUserId(params[0]);
				VozCache.instance().setCookies(conn.response().cookies());
				VozCache.instance().milliSeconds = System.currentTimeMillis();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return hasLogged;
	}

	private Connection buildRequest(Connection conn, String[] params,
			String captcha, String salt) {
//		return conn.data("api_cookieuser", "1").data("securitytoken", "guest")
//				.data("do", "login").data("api_vb_login_username", params[0])
//				.data("api_vb_login_password", params[1])
//				.data("api_vb_login_md5password", Utils.md5(params[1]))
//				.data("api_vb_login_md5password_utf", Utils.md5(params[1]))
//				.data("api_2factor", "").data("api_captcha", captcha)
//				.data("api_salt", salt);
		return conn.data("cookieuser", "1")
				.data("securitytoken", "guest")
				.data("do", "login")
				.data("vb_login_username", params[0])
				.data("vb_login_password", params[1])
				.data("vb_login_md5password", Utils.md5(params[1]))
				.data("vb_login_md5password_utf", Utils.md5(params[1]))
				.data("s", "")
				//.data("api_2factor", "").data("api_captcha", captcha)
				.data("salt", salt);
	}
	
	@Override
	protected void onPostExecute(final Boolean success) {
		/*mAuthTask = null;
		showProgress(false);*/
		List<Boolean> list = new ArrayList<Boolean>();
		if (success) {
			list.add(true);
		} else {
			list.add(false);			
		}
		callback.doCallback(list);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void onCancelled() {
		List list = new ArrayList<Boolean>();
		list.add(false);		
		if (callback != null) {
			list.add(true);
			callback.doCallback(list, true);
		}
		// mAuthTask = null;
		// showProgress(false);
	}
}
