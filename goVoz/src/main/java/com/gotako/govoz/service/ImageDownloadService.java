package com.gotako.govoz.service;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by namtu on 7/3/16.
 */

public class ImageDownloadService {
    public List<DownloadBatch> batches;
    private Context ctx;
    private static ImageDownloadService service;
    private ImageDownloadService() {
        batches = new ArrayList<>();
    }
    public static ImageDownloadService service() {
        if(service == null) service = new ImageDownloadService();
        return service;
    }
    public void set(Context ctx) {
        this.ctx = ctx;
    }
    public DownloadBatch create() {
        DownloadBatch batch = new DownloadBatch(ctx);
        batches.add(batch);
        return batch;
    }
    public DownloadBatch get(int idx) {
        return batches.get(idx);
    }
    public void start() {
        final Queue<DownloadBatch> queue = new ArrayBlockingQueue<DownloadBatch>(50, true, batches);
        for(int i=0;i<2;i++) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(!queue.isEmpty()) {
                        try {
                            queue.poll().trigger();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            thread.start();
        }
    }
}
