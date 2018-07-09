package com.gotako.govoz.tasks;

import com.gotako.govoz.ActivityCallback;
import com.gotako.govoz.VozCache;
import com.gotako.govoz.VozConfig;
import com.gotako.govoz.data.Post;
import com.gotako.govoz.data.ThreadDumpObject;
import com.gotako.govoz.service.DownloadBatch;
import com.gotako.govoz.service.ImageDownloadService;
import com.gotako.util.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class VozThreadDownloadTask extends AbstractDownloadTask<Post> {

	int lastPage = 1;
	String threadName = "";
    boolean closed;
    private String pValue = "";
    private String replyLink = "";
	private String errorMessage = null;

	public VozThreadDownloadTask(ActivityCallback<Post> callback) {
		super(callback);
	}

	@Override
	public List<Post> processResult(Document document) {

		ImageDownloadService.service().batches.clear();
		ImageDownloadService.service().set(mContext);
		List<Post> posts = new ArrayList<Post>();
		Element title = document.select("title").first();
		threadName = title.text().trim();
		if(threadName.endsWith("- vozForums")) {
			threadName = threadName.substring(0, threadName.length() - 11);
		}
        Element errorElement = Utils.getFirstElement(document.select("td[class=tcat]"));
		if(errorElement != null && errorElement.text().contains("vBulletin Message")) {
            errorMessage = Utils.getFirstElement(document.select("div[style=margin: 10px]")).text();
            return posts;
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
				post.setPostCount("#" + postCount.attr("name"));
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
			DownloadBatch batch = ImageDownloadService.service().create();
			if(!post.isDeleted()) {
				Element first = Utils.getFirstElement(tablePost.select("div[id^=post_message_]"));
				if(first!=null) {
					first.attr("style","margin: 0px;padding: 0px;border: none;width: 100%;overflow: hidden");
					// process blockquote
					cleanUp(first,"blockquote","margin: 0px;padding: 1px;border: none;width: 100%;");
					cleanUp(first,"pre","margin: 0px;padding: 1px;border: 1px solid;width: 100%;text-align: left;overflow: hidden");

                    // resize quote
                    Elements quotes = first.select("div[style^=margin:20px;]");
                    if(quotes!= null && quotes.size()>0) {
                        for(Element quote:quotes){
                            post.setComplexStructure(true);
                            quote.attr("style", "width:99%;background-color: #F2F2F2");
                            Element tableQuote = Utils.getFirstElement(quote.select("table[cellpadding=6][class*=voz-bbcode-quote]"));
                            processQuote(tableQuote, quote);
                        }
                    }

					//resize image
					Elements images = first.select("img");
                    processImages(images, post, batch);

					StringBuilder ctent = new StringBuilder(first.toString());
					post.setContent(ctent.toString());

                    // get sign
					Element possibleSign = first.nextElementSibling();
					if(!possibleSign.hasAttr("align") && !possibleSign.hasAttr("style")) { // it could be sign
						Elements floatDivs = possibleSign.select("div[style^=margin:20px;]");
						for(Element floatDiv:floatDivs){
							floatDiv.attr("style", "width:100%");
						}
						Elements allPres = possibleSign.select("pre");
						for(Element pre:allPres){
							pre.attr("style", "margin: 0px;padding: 1px;border: 1px solid;width: 100%;text-align: left;overflow: hidden");
						}
                        // resize quote
                        Elements quotesInSign = possibleSign.select("table[cellpadding=6][class*=voz-bbcode-quote]");
                        if(quotesInSign!= null && quotesInSign.size()>0) {
                            for(Element quote:quotesInSign){
                                processQuote(quote, possibleSign);
                            }
                        }
                        //resize image
                        Elements quoteImages = first.select("img");
                        processImages(quoteImages, null, batch);
						post.setUserSign("<div style='display: block;width:100%;overflow: hidden'>" + possibleSign.toString() + "</div>");
					}
                    // try to get attachment
                    Element fieldSet = Utils.getFirstElement(tablePost.select("fieldset[class=fieldset]"));
                    if(fieldSet != null) {
                        Element legend = Utils.getFirstElement(fieldSet.select("legend"));
                        if(legend != null && legend.text().contains("Attached Thumbnails")) {
                            Element divAttach = Utils.getFirstElement(fieldSet.select("div[style=padding:3px]"));
                            divAttach.attr("style","border:1px;width:100%");
                            post.setContent(post.getContent() + divAttach.html());
                        }
                    }
				}					
				else {
					post.setContent("Cannot load post");
					if(userIsIgnored) {
						post.setContent(tablePost.select("div[class=smallfont]").get(1).text());						
					}
                    post.setUserSign("");
				}
			} else {
				post.setUser(Utils.getFirstText(tablePost.select("a[href^=member.php?u=]")));
            }
			posts.add(post);
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
				Element ele = document.select("input[type=hidden][name=p]").first();
				pValue = ele.attr("value");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// get reply link
		Elements repliEles =document.select("a[href^=newreply.php?do=newreply][href*=noquote=1]");
		if (repliEles != null && repliEles.size() > 0) {
			Element replyEle = repliEles.first();
			replyLink = replyEle.attr("href").replaceAll("\\&amp\\;", "&");
		}
		// check whether this thread is closed or not
		checkThreadCloseStatus(document);
		
		return posts;
	}

    private void processImages(Elements images, Post post, DownloadBatch batch) {
	    if (images == null) return;
        for(Element image:images) {
            // if not smilies so wrap it inside an inline block and restrict the size
            if(!image.attr("src").contains("images/smilies/")) {
                image.attr("style","display: block;max-width: 100%");
                image.attr("onerror","this.src='file:///android_res/drawable/load_black_glass.gif';");
                image.wrap("<div style='display: inline-block'></div>");
                if(VozConfig.instance().isUseBackgroundService()) {
                    if (image.attr("src").startsWith("http")) {
                        batch.add(image.attr("src"));
                        String newLink = "file://" + convertToLocalLink(image.attr("src"));
                        image.attr("src", newLink);
                    }
                }
                if (post!=null && !post.isComplexStructure()) post.setComplexStructure(true);
            } else {
                String srcLink = image.attr("src").toLowerCase();
                if (srcLink.endsWith("smilies/cool.gif")) srcLink = "cool1.gif";
                else if (srcLink.endsWith("smilies/emos/shit.gif")) srcLink = "shit1.gif";
                else srcLink = srcLink.substring(srcLink.lastIndexOf("/")+ 1);
                image.attr("src", "file:///android_asset/" + srcLink);
            }
        }
    }

    private void processQuote(Element tableQuote, Element context) {
        if (tableQuote!=null) {
            tableQuote.attr("cellpadding","1");
            tableQuote.attr("width","100%");
            tableQuote.removeAttr("class");
            tableQuote.attr("style", "table-layout: fixed;box-sizing: border-box");
            Element quoteLink = Utils.getFirstElement((tableQuote.select("div > a")));
            if (quoteLink != null) {
                quoteLink.remove();
            }
            Element td = Utils.getFirstElement(tableQuote.select("td[style*=inset]"));
            String quoteContent = null;
            if(td != null) {
                quoteContent = td.html();
                List<Node> nodes = Parser.parseFragment("<div style='width: 99%;background-color: #F2F2F2'>" + quoteContent + "</div>",
                        context,
                        context.baseUri());
                tableQuote.replaceWith(nodes.get(0));
            }
        }
    }

    private void cleanUp(Element first, String ele, String stylesheet) {
		Elements elements = first.select(ele);
		for(Element element: elements) {
			element.attr("style", stylesheet);
		}
	}

	private String convertToLocalLink(String src) {
		return mContext.getCacheDir() + Utils.getPath(src);
	}


	private void checkThreadCloseStatus(Document document) {
		try {
			Element table = document
					.select("table[cellpadding=0][cellspacing=0][border=0][width=100%][style=margin-bottom:3px;padding:0px 10px 0px 0px]")
					.first();
			Element closeImage = Utils
					.getFirstElement(table
							.select("img[src=images/buttons/threadclosed.gif][alt=Closed Thread]"));
			closed = closeImage != null;
			/*
			 * String src = closeImage.attr("src"); src =
			 * closeImage.attr("alt");
			 */
			// till here, which mean closeImage is not null and thread is closed
			// VozCache.instance().getCurrentThread().setClosed(true);
		} catch (Exception ex) {
			ex.printStackTrace();
			closed = false;
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
						post.setJoinDate(childText.replace("Join Date:", "JD:"));
					}
					// <div> Posts: 20 </div>
					if (childText.contains("Posts:")) {
						post.setPosted(childText.replace("Posts:", "P:"));
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
		if (errorMessage == null && exception != null) {
			errorMessage = exception.getMessage();
		}
		if (callback != null) {
			callback.doCallback(result, errorMessage, lastPage, threadName, closed, pValue, replyLink);
		}
	}
	
	@Override
	protected List<Post> doInBackground(String... params) {
		return doInBackgroundInternal(params);
	}

	public int getLastPage() {
		return lastPage;
	}

	public void setLastPage(int lastPage) {
		this.lastPage = lastPage;
	}

	@Override
	public void afterDownload(Document document, String... params) {
		String threadId = String.valueOf(VozCache.instance().getCurrentThread());
        ThreadDumpObject threadDumpObject = new ThreadDumpObject();
        threadDumpObject.threadId = VozCache.instance().getCurrentThread();
        threadDumpObject.document = document;
        threadDumpObject.closed = closed;
        threadDumpObject.lastPage = lastPage;
        threadDumpObject.pValue = pValue;
        threadDumpObject.replyLink = replyLink;
        threadDumpObject.threadName = threadName;
        String currentPage = String.valueOf(VozCache.instance().getCurrentThreadPage());
        if (params.length > 1) {
            currentPage = params[1];
        }
        String key = threadId + "_" + currentPage;
        VozCache.instance().putDataToCache(key, threadDumpObject);
	}
}
