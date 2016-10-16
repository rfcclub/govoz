package com.gotako.govoz.tasks;

import com.gotako.govoz.VozCache;
import com.gotako.govoz.data.Forum;
import com.gotako.govoz.utils.DocumentUtil;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class TaskHelper {

	/**
	 * Helper method which parse forum and return a list contain parsed forums
	 * 
	 * @param document
	 *            XML document
	 * @return list of forum
	 */
	public static List<Forum> parseForum(Document document) {
		List<Forum> forums = new ArrayList<Forum>();
		Elements tables = document
				.select("table[class=tborder][cellpadding=6][cellspacing=1][border=0][width=100%][align=center]");
		if (tables.size() == 0)
			return null;
		Element tableElement = tables.get(2); // this table is for non-logged
												// user
		if (VozCache.instance().getCookies() != null) {
			tableElement = tables.get(1); // this table is correct for logged
											// user.
		}
		Elements forumEles = tableElement
		// .select("tr[align=center][style^=height: 50px;]");
				.select("tr");
		Forum forum = null;
		for (int i = 0; i < forumEles.size(); i++) {
			Element trElement = forumEles.get(i);
			// get forum id
			Elements aEles = trElement.select("a[href^=forumdisplay.php?f=]");
			if (aEles.size() > 0) {
				forum = new Forum();
				Element hrefEle = aEles.first();
				if (trElement.hasAttr("align") && trElement.hasAttr("style")) { // forum
					forum.setForumName(hrefEle.text());
					String href = hrefEle.attr("href");
					forum.setId(href.substring(href.lastIndexOf("=") + 1));
					Element spanEle = hrefEle.nextElementSibling();
					if (spanEle != null && spanEle.nodeName().equals("span")) {
						forum.setViewing(spanEle.ownText().replace("Viewing", "đang xem"));
					} else {
						forum.setViewing("(0 đang xem)");
					}

					Elements tdEles = trElement.select("td");
					if (tdEles.size() == 5) {
						forum.setThreadCount("Threads:" + tdEles.get(3).text());
						forum.setThreadReplies("Replies:"
								+ tdEles.get(4).text());
					}
					forum.setForumGroupName(null);
				} else { // forum header
					forum.setForumGroupName(hrefEle.ownText());
					forum.setForumName("");
					forum.setViewing("");
				}
			}
			if (forum != null) {
				forums.add(forum);
				forum = null;
			}
		}
		// get security token
		Elements secuTokens = document
				.select("input[type=hidden][name=securitytoken]");
		if (secuTokens.size() > 0) {
			VozCache.instance().setSecurityToken(
					secuTokens.get(0).attr("value"));
		}
		return forums;
	}

	public static List<Forum> parseSubForum(Document document) {
		List<Forum> forums = new ArrayList<Forum>();
		Elements tables = document
				.select("table[class=tborder][cellpadding=6][cellspacing=1][border=0][width=100%][align=center]");
		int pos = 0;
		boolean hasSubForum = false;
		for (Element table : tables) {
			Elements tds = table.select("td[class=tcat][width=100%]");
			if (DocumentUtil.containsText("Sub-Forums", tds)) {
				hasSubForum = true;
				break;
			}
			pos += 1;
		}
		if (hasSubForum) {
			Element subForumTable = tables.get(pos + 1); // next table contains
															// forums
			Elements subEles = subForumTable
					.select("a[href^=forumdisplay.php?f=]");

			for (Element ele : subEles) {
				Forum forum = new Forum();
				forum.setForumName(ele.text());
				String hrefValue = ele.attr("href");
				forum.setId(hrefValue.substring(hrefValue.lastIndexOf("=") + 1));
				forums.add(forum);
			}
		}
		return forums;
	}

	public static void disableSSLCertCheck() throws NoSuchAlgorithmException,
			KeyManagementException {
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkClientTrusted(
					java.security.cert.X509Certificate[] chain, String authType)
					throws CertificateException {
				// TODO Auto-generated method stub

			}

			@Override
			public void checkServerTrusted(
					java.security.cert.X509Certificate[] chain, String authType)
					throws CertificateException {
				// TODO Auto-generated method stub

			}
		} };

		// Install the all-trusting trust manager
		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

		// Create all-trusting host name verifier
		HostnameVerifier allHostsValid = new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};

		// Install the all-trusting host verifier
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	}
}
