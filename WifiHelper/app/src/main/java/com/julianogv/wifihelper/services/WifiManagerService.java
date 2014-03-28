package com.julianogv.wifihelper.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import com.julianogv.wifihelper.Defines;
import com.julianogv.wifihelper.receivers.WifiReceiver;

/**
 * Created by juliano.vieira on 18/03/14.
 */
public class WifiManagerService extends Service{
    WifiManager wifiManager;
    Context ctx;
    private static int tolerate = 0;
    private boolean running = false;
    private Handler handler;
    WifiReceiver receiverWifi;

    private Runnable wifiStartScanRunnable = new Runnable() {
        @Override
        public void run() {
            wifiManager.startScan();
            handler.postDelayed(this, Defines.DELAY);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(wifiStartScanRunnable);
        Toast.makeText(getApplicationContext(), "Stopping Service...", Toast.LENGTH_LONG).show();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        running = true;
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        registerReceivers();

        //I've heard its better to use handler than timer
        handler = new Handler();
        handler.removeCallbacks(wifiStartScanRunnable);
        handler.postDelayed(wifiStartScanRunnable, Defines.WIFI_RECEIVER_DELAY+1);

        wifiManager.startScan();
        return super.onStartCommand(intent, flags, startId);
    }

    public void registerReceivers(){
        receiverWifi = new WifiReceiver();
        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }
}
