package com.gotako.govoz.tasks;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.ProgressDialog;

import com.gotako.govoz.ActivityCallback;
import com.gotako.govoz.data.Forum;

public class VozMainForumDownloadTask extends AbstractDownloadTask<Forum> {

	public VozMainForumDownloadTask(ActivityCallback<Forum> callback) {
		super(callback);
	}	
	
	@Override
	public List<Forum> processResult(Document document) {
		List<Forum> forums = TaskHelper.parseForum(document);
		return forums;
	}
}
