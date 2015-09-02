/**
 * 
 */
package com.gotako.rss;

import java.io.InputStream;
import java.util.List;

/**
 * @author Nam
 *
 */
public interface RSSReader {

	public List<RSSObject> parse(InputStream stream);
}
