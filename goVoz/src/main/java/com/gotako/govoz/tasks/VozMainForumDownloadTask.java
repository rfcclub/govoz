package com.gotako.govoz.tasks;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.ProgressDialog;

import com.gotako.govoz.ActivityCallback;
import com.gotako.govoz.VozCache;
import com.gotako.govoz.VozConstant;
import com.gotako.govoz.data.Forum;
import com.gotako.govoz.data.SearchDumpObject;
import com.gotako.govoz.data.VozDumpObject;

public class VozMainForumDownloadTask extends AbstractDownloadTask<Forum> {

	public VozMainForumDownloadTask(ActivityCallback<Forum> callback) {
		super(callback);
	}	
	
	@Override
	public List<Forum> processResult(Document document) {
		List<Forum> forums = TaskHelper.parseForum(document);
		return forums;
	}

	@Override
	public void afterDownload(Document document, String... params) {
		VozDumpObject vozDumpObject = new VozDumpObject();
		vozDumpObject.document = document;
		VozCache.instance().putDataToCache(VozConstant.VOZ_LINK, vozDumpObject);
	}
}
