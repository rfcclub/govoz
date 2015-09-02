package com.gotako.rss;

public class RSSObject {
	private String postThumbUrl;
	private String postTitle;
	private String postDate;
	private String postLink;
	private String category;
	private int forumId;

	public String getPostThumbUrl() {
		return postThumbUrl;
	}

	public void setPostThumbUrl(String postThumbUrl) {
		this.postThumbUrl = postThumbUrl;
	}

	public String getPostTitle() {
		return postTitle;
	}

	public void setPostTitle(String postTitle) {
		this.postTitle = postTitle;
	}

	public String getPostDate() {
		return postDate;
	}

	public void setPostDate(String postDate) {
		this.postDate = postDate;
	}

	public String getPostLink() {
		return postLink;
	}

	public void setPostLink(String postLink) {
		this.postLink = postLink;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public int getForumId() {
		return forumId;
	}

	public void setForumId(int forumId) {
		this.forumId = forumId;
	}

}
