package com.gotako.govoz.tasks;
import static com.gotako.util.Utils.*;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.ProgressDialog;

import com.gotako.govoz.ActivityCallback;
import com.gotako.govoz.VozCache;
import com.gotako.govoz.data.Post;
import com.gotako.govoz.data.Thread;
import com.gotako.util.Utils;

public class VozThreadDownloadTask extends AbstractDownloadTask<Post> {

	int lastPage = 1;
	
	public VozThreadDownloadTask(ActivityCallback<Post> callback) {
		super(callback);
	}

	@Override
	public List<Post> processResult(Document document) {
		List<Post> posts = new ArrayList<Post>();
		Element divPosts = document.select("div[id=posts]").get(0);
		Elements tablePosts = divPosts
				.select("table[class^=tborder][id^=post][cellpadding=6][cellspacing=1][border=0][width=100%][align=center]");
		for (Element tablePost : tablePosts) {
			Post post = new Post();
			Elements eleUserId = tablePost.select("a[class=bigusername][href^=member.php?u=]");
			boolean userIsIgnored = false;
			if (eleUserId!=null && eleUserId.size() > 0) {
				String userIdHref = tablePost
						.select("a[class=bigusername][href^=member.php?u=]")
						.first().attr("href");
				post.setUserId(userIdHref.split("=")[1]);
				post.setUser(Utils.getFirstText(tablePost
						.select("a[class=bigusername][href^=member.php?u=]")));
			} else { // user is ignored ?
				if(tablePost.text().indexOf("This message is hidden because")>=0) {
					Element userEle = tablePost.select("a[href^=member.php?u=]").first();
					post.setUserId(userEle.attr("href").split("=")[1]);
					post.setUser(userEle.text());
					userIsIgnored = true;
				}
			}
			// post id
			Element postReport = Utils.getFirstElement(tablePost.select("a[href^=report.php?p=]"));
			if (postReport != null) {
				String id = postReport.attr("href").split("=")[1];
				post.setPostId(id);				
			}
			// post count 
			Element thead = Utils.getFirstElement(tablePost.select("td[class=thead]"));
			Element postCount = Utils.getFirstElement(thead.select("a[href^=showpost.php?p=]"));
			if(postCount !=null) {
				post.setPostCount("#" +postCount.attr("name"));
			}
			// get post time
			Element firstDiv = Utils.getFirstElement(thead.select("div[class=normal]"));			
			if(firstDiv!=null) {
				Element secondDiv = firstDiv.nextElementSibling();
				if(secondDiv!=null) {
					post.setPostDate(secondDiv.text());					
				}
			}
			// get avatar
			Element avatar = Utils.getFirstElement(tablePost.select("img[src^=customavatars/"));
			if(avatar !=null) {
				post.setAvatar(avatar.attr("src"));
			}
			// parse detail
			Elements divInfos = tablePost.select("div[class=smallfont]");
			parseDetailsTo(post, divInfos);	
			if(!post.isDeleted()) {
				Element first = Utils.getFirstElement(tablePost.select("div[id^=post_message_]"));
				if(first!=null) {
					//resize image
					Elements images = first.select("img");
					for(Element image:images) {
						// if not smilies so wrap it inside an inline block and restrict the size						
						if(!image.attr("src").contains("images/smilies/")) {
							if(image.attr("src").endsWith("gif")) { // transparent background
								image.attr("style","display: block;max-width: 60%");
							} else {
								image.attr("style","display: block;max-width: 60%;background:url(file:///android_res/drawable/loader64x64.gif) no-repeat center center");
							}
							image.wrap("<div style='display: inline-block'></div>");
						}						
					}				
					
					// resize quote
					Elements quotes = first.select("div[style^=margin:20px;]");
					if(quotes!= null && quotes.size()>0) {
						for(Element quote:quotes){
							quote.attr("style", "margin:0px; margin-top:1px;width:100%;color:white");
							Element tableQuote = Utils.getFirstElement(quote.select("table[cellpadding=6][class*=voz-bbcode-quote]"));
							if (tableQuote!=null) {
								tableQuote.attr("cellpadding","2");
								tableQuote.removeAttr("class");
								Element td = Utils.getFirstElement(tableQuote.select("td[style*=inset]"));
								if(td != null) {
									td.attr("style","border:1px solid");
								}
							}
						}
					}
					// get sign
					StringBuilder ctent = new StringBuilder(first.toString());
					Element possibleSign = first.nextElementSibling();
					if(!possibleSign.hasAttr("align") && !possibleSign.hasAttr("style")) { // it could be sign
						ctent.append("<div style='display: inline-block;width:100%;color:white'>" + possibleSign.toString() + "</div>");
					}
					
					post.setContent(ctent.toString());					
				}					
				else {
					post.setContent("Cannot load post");
					if(userIsIgnored) {
						post.setContent(tablePost.select("div[class=smallfont]").get(1).text());						
					}
				}
			} else {
				post.setUser(Utils.getFirstText(tablePost.select("a[href^=member.php?u=]")));
			}
			posts.add(post);
		}
		
		Element divNav = Utils.getFirstElement(document.select("div[class=pagenav]"));
		lastPage = VozCache.instance().getCurrentThreadPage();
		if (divNav != null) {
			Elements pageLinks = divNav.select("a");
			if (pageLinks != null && pageLinks.size() > 0) {
				for (Element pageLink : pageLinks) {
					String href = pageLink.attr("href");
					String pageSign = "page=";
					if (href.indexOf(pageSign) > 0) { // if we have link href="...page=2"
						String extractLink = href.split(pageSign)[1].trim();
						int page = Integer.parseInt(extractLink);
						if (page > lastPage)
							lastPage = page;
					}
				}
			}
		}
		VozCache.instance().setLastPage(lastPage);
		
		// if logged in so get userid
		if(!VozCache.instance().getSecurityToken().equals("guest")) {
			if (Utils.isNullOrEmpty(VozCache.instance().getUserId())) {
				Element firstSmallFontDiv = Utils.getFirstElement(document.select("div[class=smallfont]"));
				try {
					Element firstA = firstSmallFontDiv.select("a").first();
					String userId = firstA.attr("href").split("=")[1];
					VozCache.instance().setUserId(userId);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			try {
				if (Utils.isNullOrEmpty(VozCache.instance().getCurrentThread().getP())) {
					Element ele = document.select("input[type=hidden][name=p]").first();
					VozCache.instance().getCurrentThread().setP(ele.attr("value"));
				}
			} catch (Exception e) {			
				
				e.printStackTrace();
			}
		}
		// get reply link
		Elements repliEles =document.select("a[href^=newreply.php?do=newreply][href*=noquote=1]");
		if (repliEles != null && repliEles.size() > 0) {
			Element replyEle = repliEles.first();
			VozCache.instance().getCurrentThread().setReplyLink(replyEle.attr("href").replaceAll("&amp;", "&"));
		}
		// check whether this thread is closed or not
		checkThreadCloseStatus(document);
		
		return posts;
	}

	private void processQuotes(Document document) {
				
	}

	private void checkThreadCloseStatus(Document document) {
		try {
			Element table = document
					.select("table[cellpadding=0][cellspacing=0][border=0][width=100%][style=margin-bottom:3px;padding:0px 10px 0px 0px]")
					.first();
			// Element closeImage =
			// table.select("img[src=images/buttons/threadclosed.gif][alt=Closed Thread]").first();
			Element closeImage = Utils
					.getFirstElement(table
							.select("img[src=images/buttons/threadclosed.gif][alt=Closed Thread]"));
			if (closeImage != null)
				VozCache.instance().getCurrentThread().setClosed(true);
			else
				VozCache.instance().getCurrentThread().setClosed(false);
			/*
			 * String src = closeImage.attr("src"); src =
			 * closeImage.attr("alt");
			 */
			// till here, which mean closeImage is not null and thread is closed
			// VozCache.instance().getCurrentThread().setClosed(true);
		} catch (Exception ex) {
			ex.printStackTrace();
			VozCache.instance().getCurrentThread().setClosed(false);
		}
	}

	private void parseDetailsTo(Post post, Elements divInfos) {
		post.setRank("");
		post.setJoinDate("");
		post.setPosted("");
		post.setSubTitle("");
		if (divInfos!= null && divInfos.size() > 0) {
			String text = divInfos.get(0).text();
			if(isDeletedText(text)) {
				post.setContent(divInfos.get(0).html());
				post.setDeleted(true);
				return;
			}
			post.setRank(text);
			// <div class="smallfont">
			// <div>Join Date: 06-2013</div>
			if (divInfos.size() > 1) {
				for (Element child : divInfos.get(1).children()) {
					String childText = child.text();
					if (childText.contains("Join Date:")) {
						post.setJoinDate(childText);
					}
					// <div> Posts: 20 </div>
					if (childText.contains("Posts:")) {
						post.setPosted(childText);
					}
				}
				if (divInfos.size() > 2) {
					post.setSubTitle(divInfos.get(2).text());
				}
			}
		}
	}

	private boolean isDeletedText(String text) {
		return text.contains("This message has been deleted by");
	}

	@Override
	protected void onPostExecute(List<Post> result) {		
		doOnPostExecute(result);
		
		// do call back
		if (callback != null) {
			callback.doCallback(result, lastPage);
		}
	}
	
	@Override
	protected List<Post> doInBackground(String... params) {
		return doInBackgroundInternal(params);
	}

	
	/*private List<Post> doInBackgroundInternal(String[] params) {
		String urlString = params[0];
		List<Post> result = new ArrayList<Post>();
		//if (!runForCache) {
			boolean completed = false;
			try {
				Document document = null;
				if (VozCache.instance().getCookies() == null) {
					document = Jsoup.connect(urlString).timeout(60000).post();
				} else {
					document = Jsoup
							.connect(urlString)
							.timeout(60000)
							.cookies(VozCache.instance().getCookies())
							.data("securitytoken",
									VozCache.instance().getSecurityToken())
							.post();
				}
				result = processResult(document);
				completed = true;
				com.gotako.govoz.data.Thread currentThread = VozCache.instance()
						.getCurrentThread();
				String threadId = currentThread.getId() + "";
				VozCache.instance().putDataToCache(threadId + "_" + VozCache.instance().getCurrentThreadPage(), document);
			} catch (Exception e) {				
				processError(e);				
			}
			retries-=1;
		return result;
	}*/
	
	/*@Override
	protected void onPreExecute() {
		if (showProcessDialog && context!=null) {
			progressDialog = ProgressDialog
					.show(context, "", "Loading data...");
			progressDialog = new ProgressDialog(context);
			progressDialog.setMessage("Loading data...");			
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setIndeterminate(true);			
			progressDialog.show();
		}
	}*/

	public int getLastPage() {
		return lastPage;
	}

	public void setLastPage(int lastPage) {
		this.lastPage = lastPage;
	}

	@Override
	public void afterDownload(Document document) {
		com.gotako.govoz.data.Thread currentThread = VozCache.instance()
				.getCurrentThread();
		String threadId = currentThread.getId() + "";
		VozCache.instance().putDataToCache(threadId + "_" + VozCache.instance().getCurrentThreadPage(), document);
	}
}
