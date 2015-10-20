package com.gotako.govoz.tasks;

import com.gotako.govoz.ActivityCallback;
import com.gotako.govoz.data.PrivateMessageContent;

import org.jsoup.nodes.Document;

import java.util.List;

/**
 * Created by ntu on 10/20/2015.
 */
public class PMContentDownloadTask extends AbstractDownloadTask<PrivateMessageContent> {

    public PMContentDownloadTask(ActivityCallback<PrivateMessageContent> callback) {
        super(callback);
    }

    @Override
    public List<PrivateMessageContent> processResult(Document document) {
        return null;
    }
}
