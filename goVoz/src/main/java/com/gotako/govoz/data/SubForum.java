package com.gotako.govoz.data;

import java.io.Serializable;

public class SubForum extends Thread implements Serializable {
	private Forum subForum;	

	public SubForum(Forum subForum) {
		this.subForum = subForum;
		setTitle(subForum.getForumName());
		setPoster("");
		setLastUpdate("");		
	}
	@Override
	public String getThreadUrl() {
		return "forumdisplay.php?f=" + subForum.getId();
	}	
}
