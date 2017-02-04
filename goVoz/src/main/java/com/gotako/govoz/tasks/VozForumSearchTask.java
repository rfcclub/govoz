package com.gotako.govoz.tasks;

import android.util.Log;

import com.gotako.govoz.ActivityCallback;
import com.gotako.govoz.VozCache;
import com.gotako.govoz.data.Forum;
import com.gotako.govoz.data.Thread;
import com.gotako.util.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class VozForumSearchTask extends AbstractDownloadTask<Thread> {

	private List<Forum> subforums = new ArrayList<Forum>();
	private int lastPage;
	private String forumId;
	private String forumName;

	public VozForumSearchTask(ActivityCallback<Thread> callback) {
		super(callback);
	}

	@Override
	protected List<Thread> doInBackgroundInternal(String[] params) {
		String searchString = params[0];
		String showPost = params[1];
		List<Thread> result = new ArrayList<Thread>();
		try {
			TaskHelper.disableSSLCertCheck();
			Document document =  Jsoup.connect("https://vozforums.com/search.php?do=process")
					.timeout(20000).cookies(VozCache.instance().getCookies())
					.data("s", " ")
					.data("securitytoken", VozCache.instance().getSecurityToken())
					.data("do", "process")
					.data("query", searchString)
					.data("showposts", showPost)
					.data("quicksearch", "1")
					.data("childforums", "1")
					.data("exactname", "1")
					.execute()
					.parse();

			result = processResult(document);
			afterDownload(document);
		} catch (Exception e) {
			Log.e("VozForumSearchTask", e.getMessage(), e);
			processError(e);
		}
		mRetries -= 1;
		return result;
	}

	@Override
	public List<Thread> processResult(Document document) {
		List<Thread> listThreads = new ArrayList<Thread>();
		// search result does not contains subforums
		// subforums = TaskHelper.parseSubForum(document);
		Element title = document.select("title").first();
		forumName = title.text().trim();
		Elements selected = document.select("table[id=threadslist]");
		if (selected.size() == 0)
			return listThreads;
		Element table = selected.get(0);
		Elements trs = table.select("tr");
		Thread thread = null;
		for (Element tr : trs) {
			Elements threads = tr.select("a[id^=thread_title_][href^=showthread.php?t=]");
			if (threads.size() > 0) {// this tr contains thread link
				Element href = threads.get(0);
				thread = new Thread();
				String threadUrl = href.attr("href");
				thread.setThreadUrl(threadUrl);
				thread.setId(Integer.parseInt(threadUrl.substring(threadUrl.indexOf("t=")+2)));				

				thread.setTitle(href.ownText());
				Element td = href.parent().parent();
				if(td.tagName().equals("td")) {
					thread.setSubTitle(td.attr("title"));
				}
				if("vozsticky".equals(href.attr("class"))) {
					thread.setSticky(true);					
				}
				Elements spans = tr.select("span[style^=cursor:pointer][onclick^=window.open('member.php?u=]");
				if (spans.size() > 0) {
					thread.setPoster(spans.get(0).ownText());
				}
				Elements selectTds = tr.select("td[class=alt2][title^=Replies:]");
				// get last update
				if (selectTds.size() > 0) {
					Element td2 = selectTds.get(0);
					if (td2.children().size() > 0) {
						Element div = td2.child(0);
						thread.setLastUpdate(div.text());
					}
				}
				// get prefix if in box mua ban
				Element prefix = Utils.getFirstElement(tr.select("a[href*=forumdisplay.php?f][href*=prefixid]"));
				if(prefix != null) {
					thread.prefix = prefix.text();
					thread.prefixLink = prefix.attr("href").replace("&amp;","&");
					Element prefixColor = Utils.getFirstElement(prefix.select("strong"));
					String color = prefixColor.attr("style");
					color = color.substring(color.indexOf("#"), color.length() - 1);
					thread.prefixColor = color;
				}
			} else {
                Element deleteImg = Utils.getFirstElement(tr.select("img[class=inlineimg][src*=images/misc/trashcan_small.gif][alt*=Deleted Post(s)]"));
                if(deleteImg != null) { // delete thread
                    Elements deleteDiv = tr.select("div[class=smallfont]");
                    thread = new Thread();
                    thread.setDeleted(true);
                    thread.setLastUpdate(deleteDiv.get(1).text());
                    thread.setTitle(deleteDiv.get(0).text());
                    thread.setPoster("");
                }
			}
			if (thread != null) {
				listThreads.add(thread);
				thread = null;
			}
		}
		//  get last page
		String forumId = String.valueOf(VozCache.instance().getCurrentForum());
		Elements pages = document.select("a[class=smallfont][href*=page=][href^=forumdisplay.php?f="+forumId+"][title^=Last Page]");
		lastPage = VozCache.instance().getCurrentForumPage();
		for(Element linkPage : pages) {
			String href =linkPage.attr("href");
			String extractLink = href.substring(href.indexOf("page=")+5);
			int page = Integer.parseInt(extractLink);
			if(page > lastPage) lastPage = page;
		}
		return listThreads;
	}

	@Override
	protected void onPostExecute(List<Thread> result) {
		doOnPostExecute(result);
		// do call back
		if (callback != null) {
			callback.doCallback(result, subforums, lastPage, forumName);
		}
	}

	@Override
	protected List<Thread> doInBackground(String... params) {
		return doInBackgroundInternal(params);
	}
	
	public String getForumId() {
		return forumId;
	}

	public void setForumId(String forumId) {
		this.forumId = forumId;
	}

	public List<Forum> getSubforums() {
		return subforums;
	}

	public void setSubforums(List<Forum> subforums) {
		this.subforums = subforums;
	}

	public int getLastPage() {
		return lastPage;
	}

	public void setLastPage(int lastPage) {
		this.lastPage = lastPage;
	}
}
