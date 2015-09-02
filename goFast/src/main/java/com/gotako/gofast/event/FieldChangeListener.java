/**
 * 
 */
package com.gotako.gofast.event;

import java.util.EventListener;


/**
 * @author Nam
 *
 */
public interface FieldChangeListener extends EventListener {

	public void onFieldChange(FieldChangeEvent event);
}
