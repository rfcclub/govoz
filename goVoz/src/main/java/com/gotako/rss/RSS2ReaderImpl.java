/**
 * 
 */
package com.gotako.rss;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;

/**
 * @author Nam
 * 
 */
public class RSS2ReaderImpl implements RSSReader {

	private boolean ignoreThisItem;
	private RSSXMLTag currentTag;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gotako.rss.RSSReader#parse(java.io.InputStream)
	 */
	@Override
	public List<RSSObject> parse(InputStream stream) {
		List<RSSObject> result = new ArrayList<RSSObject>();
		try {
			// parse xml after getting the data
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser xpp = factory.newPullParser();
			xpp.setInput(stream, null);
			int eventType = xpp.getEventType();			
			RSSObject rssObject = null;
			currentTag = RSSXMLTag.IGNORETAG;
			ignoreThisItem = false;
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"EEE, DD MMM yyyy HH:mm:ss");
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_DOCUMENT) {

				} else if (eventType == XmlPullParser.START_TAG) {
					if (xpp.getName().equals("item")) {
						rssObject = new RSSObject();
						currentTag = RSSXMLTag.IGNORETAG;
					} else if (xpp.getName().equals("title")) {
						currentTag = RSSXMLTag.TITLE;
					} else if (xpp.getName().equals("link")) {
						currentTag = RSSXMLTag.LINK;
					} else if (xpp.getName().equals("pubDate")) {
						currentTag = RSSXMLTag.DATE;
					} 
					doParseStartTag(xpp,rssObject);					
				} else if (eventType == XmlPullParser.END_TAG) {
					if (xpp.getName().equals("item")) {
						if (!ignoreThisItem) {
							Date postDate = dateFormat.parse(rssObject
									.getPostDate());
							rssObject.setPostDate(dateFormat.format(postDate));
							result.add(rssObject);
							
						} else {
							//reset ignore flag
							ignoreThisItem = false;
						}
					} else {
						currentTag = RSSXMLTag.IGNORETAG;
					}
					doParseEndTag(xpp,rssObject);
				} else if (eventType == XmlPullParser.TEXT) {
					String content = xpp.getText();
					content = content.trim();
					Log.d("debug", content);
					if (rssObject != null) {
						switch (currentTag) {
						case TITLE:
							if (content.length() != 0) {
								String postTitle = rssObject.getPostTitle();
								if (postTitle != null) {
									postTitle += content;
								} else {
									postTitle = content;
								}
								rssObject.setPostTitle(postTitle);
							}
							break;
						case LINK:
							if (content.length() != 0) {
								String postLink = rssObject.getPostLink();
								if (postLink != null) {
									postLink += content;
								} else {
									postLink = content;
								}
								rssObject.setPostLink(postLink);
							}
							break;
						case DATE:
							if (content.length() != 0) {
								String postDate = rssObject.getPostDate();
								if (postDate != null) {
									postDate += content;
								} else {
									postDate = content;
								}
								rssObject.setPostDate(postDate);
							}
							break;
						case CATEGORY:
							if (content.length() != 0) {
								rssObject.setCategory(content);								
							}
						default:
							break;
						}
					}
					doParseText(xpp,rssObject);
				}
				eventType = xpp.next();
			}
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	protected void doParseStartTag(XmlPullParser xpp,RSSObject rssObject) {
		// TODO Auto-generated method stub
		
	}

	protected void doParseEndTag(XmlPullParser xpp,RSSObject rssObject) {
		// TODO Auto-generated method stub
		
	}

	protected void doParseText(XmlPullParser xpp,RSSObject rssObject) {
		// TODO Auto-generated method stub
		
	}

	public boolean isIgnoreThisItem() {
		return ignoreThisItem;
	}

	public void setIgnoreThisItem(boolean ignoreThisItem) {
		this.ignoreThisItem = ignoreThisItem;
	}

	public RSSXMLTag getCurrentTag() {
		return currentTag;
	}

	public void setCurrentTag(RSSXMLTag currentTag) {
		this.currentTag = currentTag;
	}

}
