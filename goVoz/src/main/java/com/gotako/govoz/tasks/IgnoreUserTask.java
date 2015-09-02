package com.gotako.govoz.tasks;

import static com.gotako.govoz.VozConstant.VOZ_LINK;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.os.AsyncTask;

import com.gotako.govoz.VozCache;

public class IgnoreUserTask extends AsyncTask<String, Void, String> {

	@Override
	protected String doInBackground(String... arg0) {
		String userId = arg0[0];
		String ignoreLink = VOZ_LINK + "/profile.php?do=addlist&userlist=ignore&u=" + userId;
		
		try {
			Document document = Jsoup.connect(ignoreLink)
					.timeout(30000)
					.cookies(VozCache.instance().getCookies())
					.get();
			Element form = document.select("form[action*=profile.php?do=doaddlist]").first();
			String ignoreConfirm = form.attr("action").replace("&amp;", "&");
			document = Jsoup.connect(VOZ_LINK + "/" + ignoreConfirm)
					.timeout(30000)
					.cookies(VozCache.instance().getCookies())
					.data("securitytoken",VozCache.instance().getSecurityToken())
					.data("s","")
					.data("do","doaddlist")
					.data("userlist","ignore")
					.data("userid",userId)
					.data("url","index.php")
					.data("confirm","yes")
					.post();			
			
			System.out.println(document.text());
		} catch (IOException e) {
			return null;
		}
		return "OK";
	}

}
