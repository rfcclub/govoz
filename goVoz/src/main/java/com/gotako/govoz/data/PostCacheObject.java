/**
 * 
 */
package com.gotako.govoz.data;

import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Cache object for each post
 * @author Nam
 *
 */
public class PostCacheObject {
	View postView;
	WebView content;
	ImageView avatar;
	TextView postDate;
	TextView postCount;
	TextView user;
	TextView joinDate;
	TextView rank;
	TextView posted;
	TextView subTitle;
	/**
	 * @return the postView
	 */
	public View getPostView() {
		return postView;
	}
	/**
	 * @param postView the postView to set
	 */
	public void setPostView(View postView) {
		this.postView = postView;
	}
	/**
	 * @return the content
	 */
	public WebView getContent() {
		return content;
	}
	/**
	 * @param content the content to set
	 */
	public void setContent(WebView content) {
		this.content = content;
	}
	/**
	 * @return the avatar
	 */
	public ImageView getAvatar() {
		return avatar;
	}
	/**
	 * @param avatar the avatar to set
	 */
	public void setAvatar(ImageView avatar) {
		this.avatar = avatar;
	}
	/**
	 * @return the postDate
	 */
	public TextView getPostDate() {
		return postDate;
	}
	/**
	 * @param postDate the postDate to set
	 */
	public void setPostDate(TextView postDate) {
		this.postDate = postDate;
	}
	/**
	 * @return the postCount
	 */
	public TextView getPostCount() {
		return postCount;
	}
	/**
	 * @param postCount the postCount to set
	 */
	public void setPostCount(TextView postCount) {
		this.postCount = postCount;
	}
	/**
	 * @return the user
	 */
	public TextView getUser() {
		return user;
	}
	/**
	 * @param user the user to set
	 */
	public void setUser(TextView user) {
		this.user = user;
	}
	/**
	 * @return the joinDate
	 */
	public TextView getJoinDate() {
		return joinDate;
	}
	/**
	 * @param joinDate the joinDate to set
	 */
	public void setJoinDate(TextView joinDate) {
		this.joinDate = joinDate;
	}
	/**
	 * @return the rank
	 */
	public TextView getRank() {
		return rank;
	}
	/**
	 * @param rank the rank to set
	 */
	public void setRank(TextView rank) {
		this.rank = rank;
	}
	/**
	 * @return the posted
	 */
	public TextView getPosted() {
		return posted;
	}
	/**
	 * @param posted the posted to set
	 */
	public void setPosted(TextView posted) {
		this.posted = posted;
	}
	/**
	 * @return the subTitle
	 */
	public TextView getSubTitle() {
		return subTitle;
	}
	/**
	 * @param subTitle the subTitle to set
	 */
	public void setSubTitle(TextView subTitle) {
		this.subTitle = subTitle;
	}
	
}
