package com.gotako.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.AsyncTask;
import android.util.Log;

public class HttpDownloadTask extends AsyncTask<String, Integer, String> {

	@Override
	protected String doInBackground(String... params) {
		String urlString = params[0];
		InputStream is = null;
		String result ="";
		try {
			URL url = new URL(urlString);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10*1000);
			conn.setConnectTimeout(10*1000);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			conn.connect();
			int responseCode = conn.getResponseCode();
			is = conn.getInputStream();
			final int bufferSize = 1024;
			byte[] buffer = new byte[1024];
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			int count = 0;
			while((count = is.read(buffer)) != -1) {
				os.write(buffer);
			}
			os.close();
			result = new String(os.toByteArray(), "UTF-8");			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(is!=null) {
				try {
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		

		return result;
	}

}
