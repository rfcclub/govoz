package com.gotako.govoz.tasks;

import com.gotako.govoz.ActivityCallback;
import com.gotako.govoz.CallbackResult;
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
    private String pmReplyLink;
    private String pmRecipient;
    private String pmQuote;
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
//                privateMessage.content = Utils.getFirstElement(tr3.select("div[class=voz-post-message]")).text();
                privateMessage.content = cleanUpContent(Utils.getFirstElement(tr3.select("div[class=voz-post-message]")));
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

    private String cleanUpContent(Element first) {
        if (first == null) return "";
        cleanUp(first, "blockquote", "margin: 0px;padding: 1px;border: none;width: 100%;");
        cleanUp(first, "pre", "margin: 0px;padding: 1px;border: 1px solid;width: 100%;text-align: left;overflow: hidden");
        // resize quote
        Elements quotes = first.select("div[style^=margin:20px;]");
        if (quotes != null && quotes.size() > 0) {
            for (Element quote : quotes) {
                quote.attr("style", "width:100%");
                Element tableQuote = Utils.getFirstElement(quote.select("table[cellpadding=6][class*=voz-bbcode-quote]"));
                if (tableQuote != null) {
                    tableQuote.attr("cellpadding", "1");
                    tableQuote.attr("width", "100%");
                    tableQuote.removeAttr("class");
                    Element td = Utils.getFirstElement(tableQuote.select("td[style*=inset]"));
                    if (td != null) {
                        td.attr("style", "border:none;background-color: #F2F2F2");
                    }
                }
            }
        }
        return first.toString();
    }

    private void cleanUp(Element first, String ele, String stylesheet) {
        Elements elements = first.select(ele);
        for(Element element: elements) {
            element.attr("style", stylesheet);
        }
    }

    @Override
    protected void onPostExecute(List<PrivateMessageContent> result) {
        doOnPostExecute(result);
        // do call back
        if (callback != null) {
            CallbackResult<PrivateMessageContent> callbackResult =
                    new CallbackResult.Builder<PrivateMessageContent>()
                    .setResult(result)
                    .setExtra(pmReplyLink, pmRecipient, pmQuote, securityToken, loggedInUser)
                    .build();
            callback.doCallback(callbackResult);
        }
    }
}
