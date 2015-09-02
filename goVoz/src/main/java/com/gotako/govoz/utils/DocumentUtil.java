package com.gotako.govoz.utils;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class DocumentUtil {

	public static boolean containsText(String string, Elements tds) {
		boolean ret= false;
		for (Element ele : tds) {
			if (ele.text().contains(string)) {
				ret = true;
				break;
			}
		}
		return ret;
	}

}
