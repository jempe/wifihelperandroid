package com.julianogv.wifihelper.receivers;

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
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.julianogv.wifihelper.Defines;
import com.julianogv.wifihelper.R;
import com.julianogv.wifihelper.WifiUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by juliano.vieira on 18/03/14.
 */
public class WifiReceiver extends BroadcastReceiver{
    WifiManager wifiManager;
    List<ScanResult> wifiList;

    String currentBSSID;
    ScanResult bestResult = null;
    ScanResult currentWifi = null;
    WifiConfiguration wifiConfig = null;
    WifiConfiguration bestWifiConfig = null;
    Boolean checkBoxAutoSwitch = false;
    long startMili = System.currentTimeMillis();
    public static int tolerate = 0;
    ArrayList<String> arrayWifiInfo;
    Context ctx;
    public WifiReceiver(){

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        long delay = (System.currentTimeMillis() - startMili);
        if (delay < Defines.WIFI_RECEIVER_DELAY){
            //minimum delay to receive wifi scan results, sometimes multiple SCAN_RESULTS are sent
            return;
        }
        ctx = context;
        arrayWifiInfo = new ArrayList<String>();
        startMili = System.currentTimeMillis();
        //Toast.makeText(context, "Wifi Receiver: " + delay, Toast.LENGTH_SHORT).show();
        wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            sendBroadcastToFillWifiList(arrayWifiInfo);
            WifiUtils.cancelNotification(context);
            return;
        }
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo currentWifiConnectivityManager = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (currentWifiConnectivityManager != null && !currentWifiConnectivityManager.isConnected()) {
            WifiUtils.cancelNotification(context);
            return;
        }

        wifiList = wifiManager.getScanResults();
        currentBSSID = wifiManager.getConnectionInfo().getBSSID();

        for (int i = 0; i < wifiList.size(); i++) {
            wifiConfig = WifiUtils.getConfiguredWifiBySSID("\"" + wifiList.get(i).SSID + "\"", context);
            if (wifiConfig == null)
                continue;

            //when it's the current wifi add a *
            if(wifiManager.getConnectionInfo().getSSID().equals("\""+wifiList.get(i).SSID+"\"")){
                arrayWifiInfo.add(wifiList.get(i).SSID + ": " + wifiList.get(i).level + "*");
            }else{
                arrayWifiInfo.add(wifiList.get(i).SSID + ": " + wifiList.get(i).level);
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
        sendBroadcastToFillWifiList(arrayWifiInfo);

        SharedPreferences settings = context.getSharedPreferences(Defines.PREFS_NAME, 0);
        tolerate = settings.getInt(Defines.TOLERATE_PREFS_NAME, 0);
        checkBoxAutoSwitch = settings.getBoolean(Defines.AUTO_SWITCH_PREFS_NAME, false);

        if(bestResult == null || currentWifi == null || bestWifiConfig == null){
            WifiUtils.cancelNotification(context);
            return;
        }

        int signalDiff = WifiManager.compareSignalLevel(currentWifi.level, bestResult.level);
        boolean isGreaterThanMinimum = (signalDiff+tolerate) < 0;
        //verify whether it has a better wifi or not
        if (!bestResult.BSSID.equals(currentBSSID)
                && signalDiff < 0
                && isGreaterThanMinimum){
            if(checkBoxAutoSwitch){
                Toast.makeText(context, "Switching to: " + bestWifiConfig.SSID, Toast.LENGTH_LONG).show();
                WifiUtils.connectToWifi(wifiManager, bestWifiConfig.networkId);
                WifiUtils.cancelNotification(context);
            }else{
                createNotification(bestResult, currentWifi, context);
            }
            return;
        }
        //cancel old notifications
        WifiUtils.cancelNotification(context);
        return;
    }

    private void sendBroadcastToFillWifiList(ArrayList<String> arrayWifiInfo) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(Defines.FILL_DATA);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putStringArrayListExtra("data", arrayWifiInfo);
        this.ctx.sendBroadcast(broadcastIntent);
    }

    public void createNotification(ScanResult newWifi, ScanResult currentWifi, Context context){
        //yesButton Listener
        Intent yesButtonIntent = new Intent(context, ButtonHandlerReceiver.class);
        yesButtonIntent.setAction(Defines.YES_ACTION);
        yesButtonIntent.putExtra("ssid", newWifi.SSID);
        PendingIntent pYesButton = PendingIntent.getBroadcast(context, 0, yesButtonIntent,
                android.content.Intent.FLAG_ACTIVITY_NEW_TASK);

        //noButton Listener
        Intent noButtonIntent = new Intent(context, ButtonHandlerReceiver.class);
        noButtonIntent.setAction(Defines.NO_ACTION);
        PendingIntent pNoButton = PendingIntent.getBroadcast(context, 1, noButtonIntent,
                android.content.Intent.FLAG_ACTIVITY_NEW_TASK);


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(true);
        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_activity);
        contentView.setOnClickPendingIntent(R.id.yesButton, pYesButton);
        contentView.setOnClickPendingIntent(R.id.noButton, pNoButton);
        mBuilder.setContent(contentView);

        //altera o conteudo do texto da notificacao
        contentView.setTextViewText(R.id.txtNotificationText, "Switch to: " + newWifi.SSID);

        //todo: improve the way it's done
        contentView.setTextViewText(R.id.txtWifiInfo, "Current: " + currentWifi.SSID+": "
                + currentWifi.level + "\nNew:      " + newWifi.SSID + ": " + newWifi.level);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(Defines.NOTIFICATION_ID);
        manager.notify(Defines.NOTIFICATION_ID, mBuilder.build());
    }
}
