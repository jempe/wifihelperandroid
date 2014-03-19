package com.julianogv.wifihelper.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.IBinder;

import com.julianogv.wifihelper.Defines;
import com.julianogv.wifihelper.WifiUtils;

/**
 * Created by juliano.vieira on 18/03/14.
 */
public class ButtonHandlerService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        //Toast.makeText(getApplicationContext(), "[x] ON DESTROY", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        String SSID = intent.getStringExtra("ssid");

        if(action.equals(Defines.YES_ACTION)){
            WifiManager mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            WifiConfiguration wifiConfig = WifiUtils.getConfiguredWifiBySSID("\"" + SSID + "\"", this);

            mainWifi.disconnect();
            mainWifi.enableNetwork(wifiConfig.networkId, true);
            mainWifi.reconnect();
        }
        //closes the notification and stop itself
        WifiUtils.cancelNotification(this);
        stopSelf();

        return super.onStartCommand(intent, flags, startId);
    }
}
