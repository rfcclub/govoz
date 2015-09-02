/**
 * 
 */
package com.gotako.network;

import static com.gotako.govoz.VozConstant.*;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Html.ImageGetter;
import android.widget.TextView;

import com.gotako.govoz.data.UrlDrawable;
import com.gotako.govoz.tasks.DownloadImageTask;

/**
 * @author Nam
 *
 */
public class UrlImageGetter implements ImageGetter {
	TextView container;
	Context context;

	public UrlImageGetter(TextView container, Context context) {
		this.container = container;
		this.context = context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.text.Html.ImageGetter#getDrawable(java.lang.String)
	 */
	@Override
	public Drawable getDrawable(String source) {
		UrlDrawable drawable = new UrlDrawable();
		DownloadImageTask task = new DownloadImageTask(drawable, container,
				context);
		String url = source;
		if (!url.startsWith("http://") && !url.startsWith("https://")) {
			url = VOZ_LINK + url;
		}
		task.execute(url);
		return drawable;
	}

}
