package com.julianogv.wifihelper.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.widget.RemoteViews;

import com.julianogv.wifihelper.Defines;
import com.julianogv.wifihelper.R;
import com.julianogv.wifihelper.WifiUtils;
import com.julianogv.wifihelper.services.ButtonHandlerService;

import java.util.List;

/**
 * Created by juliano.vieira on 18/03/14.
 */
public class WifiReceiver extends BroadcastReceiver{
    WifiManager mainWifi;
    List<ScanResult> wifiList;

    String currentBSSID;
    ScanResult bestResult = null;
    ScanResult currentWifi;
    WifiConfiguration wifiConfig = null;
    WifiConfiguration bestWifiConfig = null;
    public static int tolerate = 0;

    public WifiReceiver(){

    }


    @Override
    public void onReceive(Context context, Intent intent) {
        StringBuilder sb = new StringBuilder();
        mainWifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);

        if (mainWifi.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
            WifiUtils.cancelNotification(context);
            return;
        }
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo currentWifiConnectivityManager = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (currentWifiConnectivityManager != null && !currentWifiConnectivityManager.isConnected()) {
            WifiUtils.cancelNotification(context);
            return;
        }

        wifiList = mainWifi.getScanResults();

        currentBSSID = mainWifi.getConnectionInfo().getBSSID();

        for (int i = 0; i < wifiList.size(); i++) {
            wifiConfig = WifiUtils.getConfiguredWifiBySSID("\"" + wifiList.get(i).SSID + "\"", context);
            if (wifiConfig == null)
                continue;

            //when it's the current wifi add a *
            if(mainWifi.getConnectionInfo().getSSID().equals("\""+wifiList.get(i).SSID+"\"")){
                sb.append("*"+wifiList.get(i).SSID + ": " + wifiList.get(i).level + "\n");
            }else{
                sb.append(wifiList.get(i).SSID + ": " + wifiList.get(i).level + "\n");
            }

            //save current wifi
            if (currentBSSID.equals(wifiList.get(i).BSSID)){
                currentWifi = wifiList.get(i);
                continue;
            }

            //save the best result
            if (bestResult == null ||
                    WifiManager.compareSignalLevel(bestResult.level, wifiList.get(i).level) < 0) {
                bestResult = wifiList.get(i);
                bestWifiConfig = wifiConfig;
            }
        }

        //Todo: should use an array instead string builder
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(Defines.FILL_DATA);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra("data", sb.toString());
        context.sendBroadcast(broadcastIntent);

        SharedPreferences settings = context.getSharedPreferences(Defines.PREFS_NAME, 0);
        tolerate = settings.getInt(Defines.TOLERATE_PREFS_NAME, 0);
        if(bestResult == null){
            WifiUtils.cancelNotification(context);
            return;
        }

        int signalDiff = WifiManager.compareSignalLevel(currentWifi.level, bestResult.level);
        boolean isHigherThanMinimum = (signalDiff+tolerate) < 0;
        //verify whether it has a better wifi or not
        if (bestResult != null
                && !bestResult.BSSID.equals(currentBSSID)
                && signalDiff < 0
                && isHigherThanMinimum){
            WifiUtils.createNotification(bestResult, currentWifi, context);
            return;
        }
        //cancel old notifications
        WifiUtils.cancelNotification(context);
        return;
    }
}
