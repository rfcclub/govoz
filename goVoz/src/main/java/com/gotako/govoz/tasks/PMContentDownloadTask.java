package com.gotako.govoz.tasks;

import com.gotako.govoz.ActivityCallback;
import com.gotako.govoz.data.PrivateMessageContent;
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
                Element divTime = Utils.getElementAt(tr1.select("div[class=normal]"),1);
                privateMessage.pmDate = divTime.text().trim();
            }
            Element tr2 = Utils.getElementAt(trs, 1);
            if (tr2 != null) {
                Elements detailTrs = Utils.getFirstElement(tr2.select("table")).select("td");
                Element td = Utils.getElementAt(detailTrs,0);
                privateMessage.pmSender = td.select("a[class=bigusername][href*=member.php]").get(0).text();
                privateMessage.pmSenderTitle = td.select("div[class*=smallfont]").get(0).text();
            }
            Element tr3 = Utils.getElementAt(trs, 2);
            if (tr3 != null) {
                privateMessage.content = Utils.getFirstElement(tr3.select("div[class=voz-post-message]")).text();
                privateMessage.pmTitle = Utils.getFirstElement(tr3.select("div[class=smallfont]")).text();
            }
            privateMessages.add(privateMessage);
        }
        return privateMessages;
    }
}
