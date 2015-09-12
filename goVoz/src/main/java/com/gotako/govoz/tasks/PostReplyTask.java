/**
 * 
 */
package com.gotako.govoz.tasks;
import static com.gotako.govoz.VozConstant.*;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.gotako.govoz.ActivityCallback;
import com.gotako.govoz.R;
import com.gotako.govoz.VozCache;

/**
 * @author Nam
 * 
 */
public class PostReplyTask extends AsyncTask<String, Void, Boolean> {

	protected ActivityCallback<Boolean> callback;
	private CustomProgressDialog progressDialog;
	private Context context;
	public PostReplyTask(ActivityCallback<Boolean> callback) {
		this.callback = callback;
		this.context = (Context)callback;
	}

	@Override
	protected void onPreExecute() {
		progressDialog = new CustomProgressDialog(context,R.drawable.loadday1);					
		progressDialog.show();
	}
	
	@Override
	protected Boolean doInBackground(String... params) {
		String getReplyAttributeLink = VOZ_LINK + "/" + params[3];
		try {			
			Document document = Jsoup.connect(getReplyAttributeLink)
					                 .cookies(VozCache.instance().getCookies())
					                 .timeout(20000)
					                 .get(); 
		    Element secToken = document.select("input[name*=securitytoken]").first();
		    Element pHash = document.select("input[name*=posthash]").first();
		    Element pStime = document.select("input[name*=poststarttime]").first();
		    Element title = document.select("input[name=title]").first();
		    Element p = document.select("input[name=p]").first();
		    Element specialpost = document.select("input[name=specifiedpost]").first();
		    Element loggedinuser = document.select("input[name=loggedinuser]").first();
		    Element localElement1 = document.select("form[action*=newreply.php?do=postreply]").first();
		    String link = localElement1.attr("action").replace("&amp;", "&");
		    String postReplyLink = VOZ_LINK + "/" + link;
		    String threadId = postReplyLink.split("=")[2];
		    Connection.Response response = Jsoup.connect(postReplyLink)
		    		.timeout(30000)
		    		.cookies(VozCache.instance().getCookies())
		    		.data("title", title.attr("value"))
		    		.data("message", params[1])
		    		.data("wysiwyg", "0")
		    		.data("s", " ")
		    		.data("securitytoken", getValue(secToken))
		    		.data("do", "postreply")
		    		.data("t", threadId)
		    		.data("p", getValue(p))
		    		.data("specifiedpost", getValue(specialpost))
		    		.data("posthash", getValue(pHash))
		    		.data("poststarttime", getValue(pStime))
		    		.data("loggedinuser", getValue(loggedinuser))
		    		.data("multiquoteempty:", " ")
		    		.data("sbutton", "Submit Reply")
		    		.data("signature", "1")
		    		.data("parseurl", "1")
		    		.method(Connection.Method.POST)
		    		.execute();			
			document = response.parse();
			String abc = document.text();			

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private String getValue(Element secToken) {		
		return secToken.attr("value");		
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if(progressDialog !=null) {
			progressDialog.dismiss();
		}
		List<Boolean> boo = new ArrayList<Boolean>();
		boo.add(result);
		callback.doCallback(boo, null);
	}

}
