package com.gotako.govoz.tasks;

import android.widget.Spinner;

import com.gotako.govoz.VozCache;
import com.gotako.govoz.data.EmoticonSetObject;
import com.gotako.govoz.data.Forum;
import com.gotako.govoz.utils.DocumentUtil;
import com.gotako.util.Utils;

import org.apache.http.conn.ssl.BrowserCompatHostnameVerifierHC4;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class TaskHelper {

    /**
     * Helper method which parse forum and return a list contain parsed forums
     *
     * @param document XML document
     * @return list of forum
     */
    public static List<Forum> parseForum(Document document) {
        List<Forum> forums = new ArrayList<Forum>();

        Elements tbodies = document.select("tbody");
        for (int i = 0; i < tbodies.size(); i++) {
            Element tbody = tbodies.get(i);
            if (!tbody.hasAttr("id") && Utils.getFirstElement(tbody.select("td[class=tcat]")) == null)
                continue;
            if (tbody.hasAttr("id")) { // forum
                // get forum id
                Element checkLink = Utils.getFirstElement(tbody.select("a[href^=forumdisplay.php?f=]"));
                if (checkLink != null) {
                    Elements forumRows = tbody.select("tr");
                    if (forumRows != null && forumRows.size() > 0) {
                        for(Element tr : forumRows) {
                            Forum forum = new Forum();
                            Element ele = Utils.getFirstElement(tr.select("a[href^=forumdisplay.php?f=]"));
                            forum.setForumName(ele.text());
                            forum.setForumGroupName(null);
                            Element spanEle = ele.nextElementSibling();
                            if (spanEle != null && spanEle.nodeName().equals("span")) {
                                forum.setViewing(spanEle.ownText().replace("Viewing", "đang xem"));
                            } else {
                                forum.setViewing("(0 đang xem)");
                            }
                            Elements tds = tr.select("td");
                            if (tds.size() == 5) {
                                forum.setThreadCount("Threads:" + tds.get(3).text());
                                forum.setThreadReplies("Replies:"
                                        + tds.get(4).text());
                            }
                            String href = ele.attr("href");
                            forum.setId(href.substring(href.lastIndexOf("=") + 1));
                            forums.add(forum);
                        }
                    }
                }
            } else {
                Forum forum = null;
                Elements check = tbody.select("tbody");
                if (check != null && check.size() > 1) continue; // in parent tbody
                Element ele = Utils.getFirstElement(tbody.select("a[href^=forumdisplay.php?f=]"));
                if (ele != null) {
                    forum = new Forum();
                    forum.setForumGroupName(ele.text());
                }
                if (forum != null) forums.add(forum);
            }

        }
        // get security token
        Elements secuTokens = document
                .select("input[type=hidden][name=securitytoken]");
        if (secuTokens.size() > 0) {
            VozCache.instance().setSecurityToken(
                    secuTokens.get(0).attr("value"));
        }
        return forums;
    }

    public static List<Forum> parseSubForum(Document document) {
        List<Forum> forums = new ArrayList<Forum>();
        Elements tables = document
                .select("table[class=tborder][cellpadding=6][cellspacing=1][border=0][width=100%][align=center]");
        int pos = 0;
        boolean hasSubForum = false;
        for (Element table : tables) {
            Elements tds = table.select("td[class=tcat][width=100%]");
            if (DocumentUtil.containsText("Sub-Forums", tds)) {
                hasSubForum = true;
                break;
            }
            pos += 1;
        }
        if (hasSubForum) {
            Element subForumTable = tables.get(pos + 1); // next table contains
            // forums
            Elements subEles = subForumTable
                    .select("a[href^=forumdisplay.php?f=]");

            for (Element ele : subEles) {
                Forum forum = new Forum();
                forum.setForumName(ele.text());
                String hrefValue = ele.attr("href");
                forum.setId(hrefValue.substring(hrefValue.lastIndexOf("=") + 1));
                forums.add(forum);
            }
        }
        return forums;
    }

    public static void disableSSLCertCheck() throws NoSuchAlgorithmException,
            KeyManagementException {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] chain, String authType)
                    throws CertificateException {
                // TODO Auto-generated method stub

            }

            @Override
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] chain, String authType)
                    throws CertificateException {
                // TODO Auto-generated method stub

            }
        }};

        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                if (hostname.indexOf("voz") >= 0 || hostname.indexOf("imgur") >= 0) return true;
                return false;
            }
        };
        // BrowserCompatHostnameVerifierHC4 bchv = new BrowserCompatHostnameVerifierHC4();
        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }

    public static List<EmoticonSetObject> createDefaultEmoticonSetList() {
        List<EmoticonSetObject> emoticonSetList = new ArrayList<>();
        emoticonSetList.add(new EmoticonSetObject("VozForums emoticons","assets"));
        emoticonSetList.add(new EmoticonSetObject("Mèo mập","https://imgur.com/a/eRCCtp1"));
        emoticonSetList.add(new EmoticonSetObject("Ếch xanh (Pepe)","https://imgur.com/a/PyAepyl"));
        emoticonSetList.add(new EmoticonSetObject("Ếch lai ong Pebee = (Pepe + Quobee)","https://imgur.com/a/0LEvazq"));
        emoticonSetList.add(new EmoticonSetObject("Voz Hồi giáo","https://imgur.com/a/y0xX2"));
        return emoticonSetList;
    }
}
