package com.gotako.govoz;

import java.util.List;
import java.util.Map;

public class CallbackResult<T> {
    private List<T> result;
    private Object[] extra;
    private boolean sessionExpired;
    private boolean error;
    private boolean cancelled;

    protected CallbackResult(List<T> result, Object[] extra) {
        this.result = result;
        this.extra = extra;
        this.sessionExpired = false;
        this.error = false;
    }

    protected CallbackResult(List<T> result, Object[] extra, boolean error) {
        this(result, extra);
        this.error = error;
    }

    public List<T> getResult() {
        return result;
    }

    private void setResult(List<T> result) {
        this.result = result;
    }

    public Object[] getExtra() {
        return extra;
    }

    private void setExtra(Object[] extra) {
        this.extra = extra;
    }

    public boolean isSessionExpired() {
        return sessionExpired;
    }

    private void setSessionExpired(boolean sessionExpired) {
        this.sessionExpired = sessionExpired;
    }

    public boolean isError() {
        return error;
    }

    private void setError(boolean error) {
        this.error = error;
    }
    public boolean isCancelled() {
        return cancelled;
    }

    private void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public static class Builder<T> {
        List<T> list = null;
        Object[] extra = new Object[] {};
        boolean error = false;
        boolean sessionExpired = false;
        boolean cancelled = false;

        public Builder setResult(List<T> list) {
            this.list = list;
            return this;
        }
        public Builder setExtra(Object... extra) {
            this.extra = extra;
            return this;
        }
        public Builder hasError() {
            error = true;
            return this;
        }
        public Builder setError(boolean boo) {
            error = boo;
            return this;
        }
        public Builder setSessionExpire(boolean boo) {
            sessionExpired = boo;
            return this;
        }
        public Builder isSessionExpired() {
            sessionExpired = true;
            return this;
        }
        public Builder isCancelled() {
            cancelled = true;
            return this;
        }
        public CallbackResult<T> build() {
            CallbackResult<T> cr = new CallbackResult<T>(list, extra);
            cr.setError(error);
            cr.setSessionExpired(sessionExpired);
            cr.setCancelled(cancelled);
            return cr;
        }

    }
}
