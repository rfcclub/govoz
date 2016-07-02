package com.gotako.govoz.tasks;

import static com.gotako.govoz.VozConstant.VOZ_LINK;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

import com.felipecsl.gifimageview.library.GifImageView;
import com.gotako.govoz.VozConfig;
import com.gotako.govoz.data.UrlDrawable;

public class DownloadImageTask extends AsyncTask<String, Void, Drawable> {
	String vozUrl = VOZ_LINK + "/";
	UrlDrawable urlDrawable;
	View container;
	Context context;
	String imageUrl;

	public DownloadImageTask(UrlDrawable d, View container, Context context) {
		this.urlDrawable = d;
		this.container = container;
		this.context = context;
	}

	@Override
	protected Drawable doInBackground(String... params) {
		String url = params[0];
		imageUrl = url;
		try {
			InputStream is = fetch(url);
			if(url.endsWith(".gif") && isAnimatedGif(url)) {
				final GifImageView imageView = ((GifImageView) container);
				final byte[] bytes = new byte[is.available()];
				is.read(bytes);
				((Activity)context).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						imageView.setBytes(bytes);
						imageView.startAnimation();
					}
				});
				return null;
			} else {
				Bitmap bm = BitmapFactory.decodeStream(is);
				Drawable drawable = new BitmapDrawable(context.getResources(), bm);
				drawable.setBounds(0, 0, 0 + drawable.getIntrinsicWidth(),
						0 + drawable.getIntrinsicHeight());
				return drawable;
			}
		} catch (Exception e) {
			return null;
		}
	}

	private boolean isAnimatedGif(String url) throws IOException {
		InputStream is = lookupFileInCache(url);

		boolean found =false;
		byte[] buf = new byte[1];
		byte[] sign = new byte[3];
		is.read(sign);
		if(!"GIF".equalsIgnoreCase(new String(sign))) return false;
		String aniSign = "NETSCAPE2.0";
		int index = 0;
		boolean firstByte = false;
		int i = 0;
		while(!found && (is.read(buf) != -1)) {
			if(aniSign.charAt(index) == (char) buf[0]) {
				if(!firstByte) {
					firstByte = true;
				} else { // check whether it ends with NETSCAPE2.0
					if(index == aniSign.length() -1) {
						found = true; // ani gif ?
						break;
					}
				}
				index++;
			} else {
				if(firstByte) {
					index = 0; // reset index
					if(aniSign.charAt(index) == (char) buf[0]) { // if current char is N so turn on first byte
						firstByte = true;
					}
				}
				if(i > 1500) {
					break; // if sign does not exists in fisrt 1.5K bytes so break too, it's not ani gif.
				}
			}
			i++;
		}
		is.close();
		if(found) return true;
		// otherwise treat image is non-animated
		return false;
	}

	@Override
	protected void onPostExecute(Drawable result) {
		if (result != null) {
			Bitmap bm = ((BitmapDrawable) result).getBitmap();
			if (bm != null) {
				Drawable d = new BitmapDrawable(context.getResources(),
						Bitmap.createScaledBitmap(bm, urlDrawable.getWidth(),
								urlDrawable.getHeight(), true));
				urlDrawable.setDrawable(d);
				ImageView imageView = ((ImageView) container);
				imageView.setImageBitmap(bm);
				if (VozConfig.instance().isSupportLongAvatar()) {
					double ratio = bm.getWidth() / bm.getHeight();
					if (ratio >= 2.5) { // if long avatar ??
						imageView.getLayoutParams().width = 230;
					}
					imageView.requestLayout();
				}
				imageView.invalidate();
			}

		}
	}

	private InputStream fetch(String urlString) throws MalformedURLException,
			IOException {
		if (!existInCache(urlString)) {
			saveFileToCache(urlString, vozUrl);
		}
		return lookupFileInCache(urlString);
	}

	private void saveFileToCache(String urlString, String baseUrl) {
		if(urlString.endsWith(".gif")) {
			saveGifToCache(urlString, baseUrl);
		} else {
			saveImageToCache(urlString, baseUrl);
		}
	}
	private void saveGifToCache(String urlString, String baseUrl) {
		String realUrl = baseUrl + urlString;
		OkHttpClient client = new OkHttpClient();
		Request request = null;
		Response response = null;
		try {
			request = new Request.Builder().url(realUrl)
					.addHeader("Content-Type","image/gif")
					.build();
			response = client.newCall(request).execute();

			String savePath = context.getCacheDir() + "/" + urlString;
			File file = new File(savePath.substring(0,savePath.lastIndexOf("/")));
			file.mkdirs();
			BufferedSink sink = Okio.buffer(Okio.sink(new File(savePath)));
			sink.writeAll(response.body().source());
			sink.close();
		} catch (IOException ex) {
			Log.e("AVATAR_ERROR", ex.getMessage());
		}
	}

	private void saveImageToCache(String urlString, String baseUrl) {
		Reader reader = null;
		Writer writer = null;
		try {
			String realUrl = baseUrl + urlString;
			URL aURL = new URL(realUrl);
	        URLConnection conn = aURL.openConnection();
	        HttpURLConnection httpConn = (HttpURLConnection) conn;
	        try {
	            httpConn.setRequestMethod("GET");
	            httpConn.connect();
			} catch (Exception e) {
				Log.e("DOWNLOADERROR", e.getMessage());
			}
	        
            if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            	InputStream stream = httpConn.getInputStream();
            	String savePath = context.getCacheDir() + "/" + urlString;
            	
            	Bitmap bm = BitmapFactory.decodeStream(stream);
            	File file = new File(savePath.substring(0, savePath.lastIndexOf("/")));
            	file.mkdirs();
            	FileOutputStream fos = new FileOutputStream(savePath);
            	bm.compress(Bitmap.CompressFormat.WEBP, 90, fos);
                fos.close();
            }
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {

			try {
				if (writer != null)
					writer.close();
				if (reader != null)
					reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	private InputStream lookupFileInCache(String urlString)
			throws FileNotFoundException {
		String savePath = context.getCacheDir() + "/" + urlString;
		File file = new File(savePath);
		return new FileInputStream(file);
	}

	private boolean existInCache(String urlString) {
		String realUrl = context.getCacheDir() + "/" + urlString;
		File file = new File(realUrl);
		return file.isFile() && file.exists();
	}

}
