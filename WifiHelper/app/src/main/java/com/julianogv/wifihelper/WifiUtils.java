package com.julianogv.wifihelper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.widget.RemoteViews;

import com.julianogv.wifihelper.services.ButtonHandlerService;

import java.util.List;

/**
 * Created by juliano.vieira on 18/03/14.
 */

public class WifiUtils {

    public static void cancelNotification(Context ctx){
        NotificationManager nMgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancel(Defines.NOTIFICATION_ID);
    }

    public static WifiConfiguration getConfiguredWifiBySSID(String SSID, Context ctx) {
        WifiManager mainWifi = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> wifiConfigurations;

        wifiConfigurations = mainWifi.getConfiguredNetworks();
        for (int i = 0; i < wifiConfigurations.size(); i++) {
            if (wifiConfigurations.get(i).SSID.equalsIgnoreCase(SSID))
                return wifiConfigurations.get(i);
        }
        return null;
    }
}
