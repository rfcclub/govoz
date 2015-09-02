/**
 * 
 */
package com.gotako.govoz.tasks;

import static com.gotako.govoz.VozConstant.VOZ_LINK;

import java.io.IOException;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.gotako.govoz.GotReplyTaskCallback;
import com.gotako.govoz.R;
import com.gotako.govoz.VozCache;

/**
 * @author Nam
 *
 */
public class GotReplyQuoteTask extends AsyncTask<String, Void, String> {

	private GotReplyTaskCallback callback;
	private CustomProgressDialog progressDialog;
	private Context context;
	public GotReplyQuoteTask(Context context) {
		this.context = context;
		
	}
	/* (non-Javadoc)
	 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
	 */
	@Override
	protected String doInBackground(String... params) {
		String postReplyLink = VOZ_LINK + "/newreply.php?do=newreply&p=" + params[0];
		String quote = "";
		try {
			Connection.Response response = Jsoup.connect(postReplyLink)
		    		.timeout(30000)
		    		.cookies(VozCache.instance().getCookies())		    		
		    		.data("do","newreply")
		    		.data("p",params[0])
		    		.data("securitytoken",
							VozCache.instance().getSecurityToken())
		    		.method(Connection.Method.POST)
		    		.execute();			
			Document document = response.parse();			
			Element message = document.select("textarea[name=message]").first();
			quote = message.text();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return quote;
	}
	
	/**
	 * @param callback the callback to set
	 */
	public void setCallback(GotReplyTaskCallback callback) {
		this.callback = callback;
	}

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(String result) {
		if(progressDialog !=null) {
			progressDialog.dismiss();
		}
		if(callback != null) callback.workWithQuote(result);		
	}

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	@Override
	protected void onPreExecute() {
		progressDialog = new CustomProgressDialog(context,R.drawable.loadday1);					
		progressDialog.show();
	}
}
