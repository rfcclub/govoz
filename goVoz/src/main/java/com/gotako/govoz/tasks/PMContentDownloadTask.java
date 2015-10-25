package com.gotako.govoz.tasks;

import com.gotako.govoz.ActivityCallback;
import com.gotako.govoz.data.*;
import com.gotako.util.Utils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ntu on 10/20/2015.
 */
public class PMContentDownloadTask extends AbstractDownloadTask<PrivateMessageContent> {
    String pmReplyLink;
    String pmRecipient;
    String pmQuote;
    private String securityToken;
    private String loggedInUser;

    public PMContentDownloadTask(ActivityCallback<PrivateMessageContent> callback) {
        super(callback);
    }

    @Override
    public List<PrivateMessageContent> processResult(Document document) {
        List<PrivateMessageContent> privateMessages = new ArrayList<PrivateMessageContent>();
        Elements pmElements = document.select("table[class*=tborder voz-postbit][id=post]");
        for (Element pmElement : pmElements) {
            Elements trs = pmElement.children().get(0).children();
            PrivateMessageContent privateMessage = new PrivateMessageContent();
            Element tr1 = Utils.getElementAt(trs, 0);
            if (tr1 != null) {
                Element divTime = Utils.getElementAt(tr1.select("div[class=normal]"), 1);
                privateMessage.pmDate = divTime.text().trim();
            }
            Element tr2 = Utils.getElementAt(trs, 1);
            if (tr2 != null) {
                Element table = Utils.getFirstElement(tr2.select("table"));
                privateMessage.pmSender = table.select("a[class=bigusername][href*=member.php]").get(0).text();
                privateMessage.pmSenderTitle = table.select("div[class*=smallfont]").get(0).text();
            }
            Element tr3 = Utils.getElementAt(trs, 2);
            if (tr3 != null) {
                privateMessage.content = Utils.getFirstElement(tr3.select("div[class=voz-post-message]")).text();
                privateMessage.pmTitle = Utils.getFirstElement(tr3.select("div[class=smallfont]")).text();
            }
            privateMessages.add(privateMessage);
        }
        Element formElement = Utils.getFirstElement(document.select("form[action*=private.php?do=insertpm&pmid=]"));
        if (formElement != null) {
            pmReplyLink = formElement.attr("action").replaceAll("&amp;", "&");
        }
        //recipients
        Element recipientElement = Utils.getFirstElement(document.select("input[type=hidden][name=recipients]"));
        if (recipientElement != null) {
            pmRecipient = recipientElement.attr("value");
        }
        Element quoteElement = Utils.getFirstElement(document.select("textarea[name=message][id=vB_Editor_QR_textarea]"));
        if (quoteElement != null) {
            pmQuote = formElement.text();
        }

        Element secTokenElement = Utils.getFirstElement(document.select("input[type=hidden][name=securitytoken]"));
        if (secTokenElement != null) {
            securityToken = secTokenElement.attr("value");
        }

        Element loggedInUserElement = Utils.getFirstElement(document.select("input[type=hidden][name=loggedinuser]"));
        if (loggedInUserElement != null) {
            loggedInUser = loggedInUserElement.attr("value");
        }

        return privateMessages;
    }

    @Override
    protected void onPostExecute(List<PrivateMessageContent> result) {
        doOnPostExecute(result);
        // do call back
        if (callback != null) {
            callback.doCallback(result, pmReplyLink, pmRecipient, pmQuote, securityToken, loggedInUser);
        }
    }
}
