package com.gotako.govoz.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.gotako.govoz.VozConfig;
import com.gotako.govoz.data.GifUrlDrawable;
import com.gotako.govoz.data.UrlDrawable;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifDrawableBuilder;

import static com.gotako.govoz.VozConstant.VOZ_LINK;

public class DownloadImageExTask extends AsyncTask<String, Void, Drawable> {
	String vozUrl = VOZ_LINK + "/";
	UrlDrawable urlDrawable;
	View container;
	Context context;

	public DownloadImageExTask(UrlDrawable d, View container, Context context) {
		this.urlDrawable = d;
		this.container = container;
		this.context = context;
	}

	@Override
	protected Drawable doInBackground(String... params) {
		String url = params[0];
		try {
			InputStream is = fetch(url);
            if(!url.endsWith("gif")) {
                Bitmap bm = BitmapFactory.decodeStream(is);
                Drawable drawable = new BitmapDrawable(context.getResources(), bm);
                drawable.setBounds(0, 0, 0 + drawable.getIntrinsicWidth(),
                        0 + drawable.getIntrinsicHeight());
                return drawable;
            } else {
                GifUrlDrawable gifUrlDrawable = new GifUrlDrawable();
                gifUrlDrawable.drawableUrl = context.getCacheDir() + "/" + url;
                gifUrlDrawable.drawable = urlDrawable.drawable;
                return gifUrlDrawable;
            }
		} catch (Exception e) {
            Log.d("ERROR", e.getMessage());
			return null;
		}
	}

	@Override
	protected void onPostExecute(Drawable result) {
        ImageView imageView = ((ImageView) container);
		if (result != null) {
            if(result instanceof GifUrlDrawable) {
                imageView.setImageDrawable(result);
                imageView.invalidate();
            } else {
                Bitmap bm = ((BitmapDrawable) result).getBitmap();
                if (bm != null) {
                    Drawable d = new BitmapDrawable(context.getResources(),
                            Bitmap.createScaledBitmap(bm, urlDrawable.getWidth(),
                                    urlDrawable.getHeight(), true));
                    // set the correct bound according to the result from HTTP call

                    // change the reference of the current drawable to the result
                    // from the HTTP call
                    urlDrawable.setDrawable(d);
                    imageView.setImageBitmap(bm);
                }
                if (VozConfig.instance().isSupportLongAvatar()) {
                    double ratio = bm.getWidth() / bm.getHeight();
                    if (ratio >= 2.5) { // if long avatar ??
                        imageView.getLayoutParams().width = 230;
                    }
                    imageView.requestLayout();
                }
            }
            imageView.invalidate();
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
		Reader reader = null;
		Writer writer = null;
        FileOutputStream outputStream = null;
        try {
			String realUrl = baseUrl + urlString;
			URL aURL = new URL(realUrl);
	        URLConnection conn = aURL.openConnection();
	        HttpURLConnection httpConn = (HttpURLConnection) conn;
	        try {
				//conn.connect();
	            httpConn.setRequestMethod("GET");
	            httpConn.connect();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
            if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                httpConn.setDoInput(true);
                httpConn.setInstanceFollowRedirects(true);
            	InputStream stream = httpConn.getInputStream();
            	String savePath = context.getCacheDir() + "/" + urlString;
            	File file = new File(savePath.substring(0,savePath.lastIndexOf("/")));
            	file.mkdirs();
                if(urlString.endsWith("gif")) {
                    outputStream = new FileOutputStream(file);
                    byte[] buffer = new byte[1024];
                    int status;
                    while((stream.read(buffer)) != -1) {
                        outputStream.write(buffer);
                    }
                    outputStream.flush();
                    httpConn.disconnect();
                    outputStream.close();
                    outputStream = null;
                } else {
                    Bitmap bm = BitmapFactory.decodeStream(stream);
                    FileOutputStream fos = new FileOutputStream(savePath);
                    bm.compress(Bitmap.CompressFormat.WEBP, 90, fos);
                }
                httpConn.disconnect();
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
                if(outputStream != null) outputStream.close();
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
