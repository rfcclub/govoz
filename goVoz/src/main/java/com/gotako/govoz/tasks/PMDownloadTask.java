package com.gotako.govoz.tasks;

import com.gotako.govoz.ActivityCallback;
import com.gotako.govoz.data.Post;
import com.gotako.govoz.data.PrivateMessage;
import com.gotako.util.Utils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nam on 9/6/2015.
 */
public class PMDownloadTask extends AbstractDownloadTask<PrivateMessage> {
    public PMDownloadTask(ActivityCallback<PrivateMessage> callback) {
        super(callback);
    }

    @Override
    public List<PrivateMessage> processResult(Document document) {
        List<PrivateMessage> privateMessages = new ArrayList<PrivateMessage>();
        Element formPM = Utils.getFirstElement(document.select("form[id=pmform][action^=private.php?do=managepm][method*=post]"));
        if(formPM!=null) {
            Element tablePM = Utils.getFirstElement(formPM.select("table"));
            Elements tBodys = tablePM.select("tbody");
            for(Element tBody : tBodys) {
                if(tBody.hasAttr("id")) {
                    Elements msgs = tBody.select("tr");
                    for(Element msg: msgs) {
                        Element tdMsg = msg.select("td").get(2);
                        PrivateMessage privateMessage = new PrivateMessage();
                        // get date
                        privateMessage.pmDate = "";
                        Elements timeSpans = tdMsg.select("span[style=float:right]");
                        for(Element timeSpan:timeSpans) {
                            privateMessage.pmDate +=timeSpan.text() + " ";
                        }
                        // get message
                        Element msgLink = Utils.getFirstElement(msg.select("a[href*=private.php]"));
                        privateMessage.pmTitle = msgLink.text();
                        // get link
                        privateMessage.pmLink = msgLink.attr("href").replace("&amp;","&");
                        // get user
                        Element spanUser = Utils.getFirstElement(msg.select("span[onclick*=member]"));
                        privateMessage.pmSender = spanUser.text();
                        privateMessages.add(privateMessage);
                    }
                }
            }
        }
        return privateMessages;
    }
}
