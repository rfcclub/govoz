package com.gotako.govoz.rss;

import org.xmlpull.v1.XmlPullParser;

import com.gotako.rss.RSS2ReaderImpl;
import com.gotako.rss.RSSObject;
import com.gotako.rss.RSSXMLTag;

public class VozRSSReaderImpl extends RSS2ReaderImpl {

	private int filterForumId;

	@Override
	protected void doParseStartTag(XmlPullParser xpp, RSSObject rssObject) {
		if (xpp.getName().equals("category")) {
			setCurrentTag(RSSXMLTag.CATEGORY);
			String category = xpp.getAttributeValue(0);
			int forumId = Integer.parseInt(category.substring(category
					.indexOf("f=") + 2));
			rssObject.setForumId(forumId);
			if (forumId != filterForumId) {
				setIgnoreThisItem(true);
			}
		}
	}

	public int getFilterForumId() {
		return filterForumId;
	}

	public void setFilterForumId(int filterForumId) {
		this.filterForumId = filterForumId;
	}

}
