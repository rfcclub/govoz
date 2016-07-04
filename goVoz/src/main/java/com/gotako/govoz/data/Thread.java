package com.gotako.govoz.data;

import java.io.Serializable;

public class Thread implements Serializable {
    public final int SUBFORUM = 1;
    public final int HEADER = 2;
    public final int THREAD = 3;
    private String title;
    private String subTitle;


    private String poster;
    private String postDate;
    private String threadUrl;
    private String status;
    private String lastUpdate;
    private boolean isSubForum;
    private boolean isSticky;
    private int id;
    private String securitytoken;
    private String posthash;
    private String poststarttime;
    private String inputTitle;
    private String p;
    private boolean closed = false;
    private String replyLink;
    private boolean deleted;
    public String prefix;
    public String prefixColor;
    public String prefixLink;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getPostDate() {
        return postDate;
    }

    public void setPostDate(String postDate) {
        this.postDate = postDate;
    }

    public String getThreadUrl() {
        return threadUrl;
    }

    public void setThreadUrl(String threadUrl) {
        this.threadUrl = threadUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public boolean isSubForum() {
        return isSubForum;
    }

    public void setSubForum(boolean isSubForum) {
        this.isSubForum = isSubForum;
    }

    public boolean isSticky() {
        return isSticky;
    }

    public void setSticky(boolean isSticky) {
        this.isSticky = isSticky;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSecuritytoken() {
        return securitytoken;
    }

    public void setSecuritytoken(String securitytoken) {
        this.securitytoken = securitytoken;
    }

    public String getPosthash() {
        return posthash;
    }

    public void setPosthash(String posthash) {
        this.posthash = posthash;
    }

    public String getPoststarttime() {
        return poststarttime;
    }

    public void setPoststarttime(String poststarttime) {
        this.poststarttime = poststarttime;
    }

    public String getInputTitle() {
        return inputTitle;
    }

    public void setInputTitle(String inputTitle) {
        this.inputTitle = inputTitle;
    }

    public String getP() {
        return p;
    }

    public void setP(String p) {
        this.p = p;
    }

    /**
     * @return the closed
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * @param closed the closed to set
     */
    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    /**
     * @return the replyLink
     */
    public String getReplyLink() {
        return replyLink;
    }

    /**
     * @param replyLink the replyLink to set
     */
    public void setReplyLink(String replyLink) {
        this.replyLink = replyLink;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }
}
