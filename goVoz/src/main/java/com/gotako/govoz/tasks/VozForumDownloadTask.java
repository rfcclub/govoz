package com.gotako.govoz.tasks;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.gotako.govoz.ActivityCallback;
import com.gotako.govoz.VozCache;
import com.gotako.govoz.data.Forum;
import com.gotako.govoz.data.Thread;

public class VozForumDownloadTask extends AbstractDownloadTask<Thread> {

	private List<Forum> subforums = new ArrayList<Forum>();
	private int lastPage;
	private String forumId;
	private String forumName;
	
	public VozForumDownloadTask(ActivityCallback<Thread> callback) {
		super(callback);
	}

	@Override
	public List<Thread> processResult(Document document) {
		List<Thread> listThreads = new ArrayList<Thread>();
		subforums = TaskHelper.parseSubForum(document);
		Element title = document.select("title").first();
		forumName = title.text().split("-")[0].trim();
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
