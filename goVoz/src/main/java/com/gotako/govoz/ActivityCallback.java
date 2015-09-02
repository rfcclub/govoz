/**
 * 
 */
package com.gotako.govoz;

import java.util.List;

/**
 * Provide an interface for call back activity
 * 
 * @author ntu
 * 
 */
public interface ActivityCallback<T> {
	void doCallback(List<T> result,Object... extra);		
}
