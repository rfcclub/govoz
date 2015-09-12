package com.gotako.govoz.data;

import org.jsoup.nodes.Document;

/**
 * Created by Nam on 9/12/2015.
 */
public class ThreadDumpObject {
    public int threadId;
    public int lastPage;
    public String threadName;
    public boolean closed;
    public String pValue;
    public String replyLink;
    public Document document;
}
