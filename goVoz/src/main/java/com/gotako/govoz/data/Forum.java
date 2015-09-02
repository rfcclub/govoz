package com.gotako.govoz.data;

import java.io.Serializable;

public class Forum implements Comparable<Forum>, Serializable{
	String forumName;
	String id;
	String threadCount;
	String threadReplies;
	String viewing;
	String forumGroupName = null;
	Forum parent;
	

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(String threadCount) {
		this.threadCount = threadCount;
	}

	public String getThreadReplies() {
		return threadReplies;
	}

	public void setThreadReplies(String threadReplies) {
		this.threadReplies = threadReplies;
	}

	public String getViewing() {
		return viewing;
	}

	public void setViewing(String viewing) {
		this.viewing = viewing;
	}

	public String getForumName() {
		return forumName;
	}

	public void setForumName(String name) {
		this.forumName = name;
	}

	public String getForumGroupName() {
		return forumGroupName;
	}

	public void setForumGroupName(String forumGroupName) {
		this.forumGroupName = forumGroupName;
	}

	@Override
	public int compareTo(Forum another) {
		if (another == null || another.id == null || another.id.length() == 0) {
			return 1;
		}
		if (id == null || id.length() == 0) {
			return -1;
		}
		
		return Integer.parseInt(id) - Integer.parseInt(another.id);
	}

	public Forum getParent() {
		return parent;
	}

	public void setParent(Forum parent) {
		this.parent = parent;
	}

//	@Override
//	public boolean equals(Object another) {
//		if (!(another instanceof Forum)) {
//			return false;
//		}
//		
//		return compareTo((Forum)another) == 0;
//	}
//
//	@Override
//	public int hashCode() {
//		return 1;
//	}
}
