package com.gotako.govoz.tasks;

import com.gotako.govoz.ActivityCallback;

import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;

public class HttpDownloadTask extends AbstractDownloadTask<Document> {
    public HttpDownloadTask(ActivityCallback callback) {
        super(callback);
    }

    @Override
    public List<Document> processResult(Document document) {
        List<Document> documents = new ArrayList<>();
        documents.add(document);
        return documents;
    }
}
