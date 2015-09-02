/**
 * 
 */
package com.gotako.govoz.service;

import static com.gotako.govoz.VozConstant.VOZ_LINK;

import com.gotako.govoz.VozCache;
import com.gotako.govoz.tasks.VozThreadDownloadTask;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

/**
 * @author lnguyen66
 *
 */
public class CachePostService extends Service {

	private IBinder binder = new MyLocalBinder();
	
	/* (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
	
	public void cachePost() {
		com.gotako.govoz.data.Thread currentThread = VozCache.instance()
				.getCurrentThread();
		String threadUrl = VOZ_LINK + "/"  + currentThread.getThreadUrl()
				+ "&page="
				+ String.valueOf(VozCache.instance().getCurrentThreadPage());
		VozThreadDownloadTask task = new VozThreadDownloadTask(null);		
		task.execute(threadUrl);
	}

	public class MyLocalBinder extends Binder {
		public CachePostService getService() {
			return CachePostService.this;
		}
	}
}
