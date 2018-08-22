package com.gotako.govoz.service;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.system.OsConstants;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.gotako.govoz.R;
import com.gotako.govoz.SettingActivity;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class VozVpnService extends VpnService implements Runnable {
    private static final String TAG = VozVpnService.class.getSimpleName();
    public static final String ACTION_CONNECT = "com.gotako.govoz.service.START";
    public static final String ACTION_DISCONNECT = "com.gotako.govoz.service.STOP";

    private Thread mThread;
    private boolean running;
    private ParcelFileDescriptor mDescriptor;
    @Override
    public void onCreate() {
        super.onCreate();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_DISCONNECT.equals(intent.getAction())) {
            disconnect();
            return START_NOT_STICKY;
        } else {
            connect();
            return START_STICKY;
        }
    }


    public boolean handleMessage(Message message) {
        Toast.makeText(this, message.what, Toast.LENGTH_SHORT).show();
        return true;
    }

    private void connect() {
        // Become a foreground service. Background services can be VPN services too, but they can
        // be killed by background check before getting a chance to receive onRevoke().
        Log.d(TAG, "connecting");
        if (mThread == null) {
            mThread = new Thread(this, "DaedalusVpn");
            running = true;
            mThread.start();
        }
    }

    private void disconnect() {
        try {
            if (this.mDescriptor != null) {
                mDescriptor.close();
                mDescriptor = null;
            }
            if (mThread != null) {
                running = false;
                mThread.interrupt();
                mThread = null;
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        stopSelf();
    }

    @Override
    public void onDestroy() {
        disconnect();
    }

    @Override
    public void run() {
        try {
            Builder builder = new Builder()
                    .setSession("VozVpnService")
                    .setConfigureIntent(PendingIntent.getActivity(this, 0,
                            new Intent(this, SettingActivity.class),
                            PendingIntent.FLAG_ONE_SHOT));

            String format = null;
            for (String prefix : new String[]{
                    "10.0.0",
                    "192.0.2",
                    "198.51.100",
                    "203.0.113",
                    "192.168.50"}) {
                try {
                    builder.addAddress(prefix + ".1", 24);
                } catch (IllegalArgumentException e) {
                    continue;
                }

                format = prefix + ".%d";
                break;
            }

            String primaryServer = "8.8.8.8";
            String secondaryServer = "1.1.1.1";
            InetAddress primaryDNSServer = InetAddress.getByName(primaryServer);
            InetAddress secondaryDNSServer = InetAddress.getByName(secondaryServer);
            builder.addDnsServer(primaryDNSServer).addDnsServer(secondaryDNSServer);
            Log.i(TAG, "VPN service is listening on " + primaryServer + " and " + secondaryServer);
            mDescriptor = builder.establish();
            Log.i(TAG, "VPN service is started");

            while (running) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException ignored) {
        } catch (Exception e) {
            Log.d(TAG, "ERROR", e);
        } finally {
            Log.d(TAG, "quit");
            disconnect();
        }
    }

    @Override
    public void onRevoke() {
        disconnect();
    }
}
