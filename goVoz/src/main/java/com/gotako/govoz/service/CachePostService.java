/**
 * 
 */
package com.gotako.govoz.service;

import static com.gotako.govoz.VozConstant.THREAD_URL_T;
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
		String currentThreadId = String.valueOf(VozCache.instance().getCurrentThread());
		String threadUrl = THREAD_URL_T + "/"  + currentThreadId
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
