/**
 * 
 */
package com.gotako.govoz;

/**
 * Provide an interface for call back activity
 * 
 * @author ntu
 * 
 */
public interface ActivityCallback<T> {
	void doCallback(CallbackResult<T> result);
}
