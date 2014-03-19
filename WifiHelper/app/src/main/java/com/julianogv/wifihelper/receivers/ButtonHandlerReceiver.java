package com.julianogv.wifihelper.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import com.julianogv.wifihelper.Defines;
import com.julianogv.wifihelper.WifiUtils;

/**
 * Created by juliano.vieira on 19/03/14.
 */
public class ButtonHandlerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String SSID = intent.getStringExtra("ssid");

        if(action.equals(Defines.YES_ACTION)){
            WifiManager mainWifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiConfiguration wifiConfig =
                    WifiUtils.getConfiguredWifiBySSID("\"" + SSID + "\"", context);

            mainWifi.disconnect();
            mainWifi.enableNetwork(wifiConfig.networkId, true);
            mainWifi.reconnect();
        }

        WifiUtils.cancelNotification(context);
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(it);
    }
}