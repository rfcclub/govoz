package com.gotako.govoz.data;

import org.jsoup.nodes.Document;

/**
 * Created by Nam on 9/12/2015.
 */
public class ThreadDumpObject extends VozDumpObject {
    public int threadId;
    public String threadName;
    public boolean closed;
    public String pValue;
    public String replyLink;
}
