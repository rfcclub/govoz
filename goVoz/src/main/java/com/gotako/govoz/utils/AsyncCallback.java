package com.gotako.govoz.utils;

/**
 * Created by Nam on 10/31/2015.
 */
public interface AsyncCallback<T> {
    void callback(T result, Object... extras);
}
