package com.gotako.govoz.tasks;

import android.os.AsyncTask;

import com.gotako.govoz.VozCache;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

import static com.gotako.govoz.VozConstant.VOZ_LINK;

public class RatingThreadTask extends AsyncTask<String, Void, String> {

	@Override
	protected String doInBackground(String... arg0) {
		String threadId = arg0[0];
		String vote = arg0[1];
		String page = arg0[2];
		String ratingLink = VOZ_LINK + "/threadrate.php?t=" + threadId;
		
		try {
			Document document = Jsoup.connect(ratingLink)
					.timeout(30000)
					.cookies(VozCache.instance().getCookies())
					.data("securitytoken",VozCache.instance().getSecurityToken())
					.data("t",threadId)
					.data("pp","10")
					.data("vote", vote)
					.data("s", "")
					.data("submit", "Vote Now")
					.data("page", page)
					.post();			
			
			System.out.println(document.text());
		} catch (IOException e) {
			return null;
		}
		return "OK";
	}

}
