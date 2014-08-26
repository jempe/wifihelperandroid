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

    Boolean checkBoxAutoSwitch = false;
    public static int tolerate = 0;
    WifiManager wifiManager;
    ArrayList<String> arrayWifiInfo;
    Context ctx;
    SharedPreferences settings;

    public WifiReceiver(){
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        settings = context.getSharedPreferences(Defines.PREFS_NAME, 0);
        //
        if(!settings.getBoolean(Defines.SHOULD_RUN, true)){
            return;
        }
        WifiConfiguration wifiConfig;
        ScanResult currentWifi = null;
        ScanResult bestResult = null;
        WifiConfiguration bestWifiConfig = null;
        List<ScanResult> wifiList;
        ctx = context;

        arrayWifiInfo = new ArrayList<String>();
        wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        wifiList = wifiManager.getScanResults();

        if (!wifiManager.isWifiEnabled() || wifiList.size() < 1 ) {
           sendBroadcastToFillWifiList(arrayWifiInfo);
           WifiUtils.cancelNotification(context);
            return;
        }

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo currentWifiConnectivityManager = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (currentWifiConnectivityManager == null ||
                !currentWifiConnectivityManager.isConnected()) {
            WifiUtils.cancelNotification(context);
            return;
        }

        for (int i = 0; i < wifiList.size(); i++) {
            wifiConfig = WifiUtils.getConfiguredWifiBySSID("\"" + wifiList.get(i).SSID + "\"", context);
            if (wifiConfig == null)
                continue;

            //when it's the current wifi add a *
            if (wifiManager.getConnectionInfo() != null &&
                    wifiManager.getConnectionInfo().getSSID() != null &&
                    wifiManager.getConnectionInfo().getSSID().equals("\"" + wifiList.get(i).SSID + "\"")) {
                arrayWifiInfo.add(wifiList.get(i).SSID + ": " + wifiList.get(i).level + "*");
            } else {
                arrayWifiInfo.add(wifiList.get(i).SSID + ": " + wifiList.get(i).level);
            }

            //save current wifi
            if (wifiManager.getConnectionInfo() != null &&
                    wifiManager.getConnectionInfo().getBSSID() != null &&
                    wifiManager.getConnectionInfo().getBSSID().equals(wifiList.get(i).BSSID)) {
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
        if(currentWifi == null || bestResult == null){
            WifiUtils.cancelNotification(context);
            return;
        }
        tolerate = settings.getInt(Defines.TOLERATE_PREFS_NAME, 0);

        int signalDiff = WifiManager.compareSignalLevel(currentWifi.level, bestResult.level);
        boolean isGreaterThanMinimum = (signalDiff+tolerate) < 0;

        //verify whether it has a better wifi or not
        if (!bestResult.BSSID.equals(wifiManager.getConnectionInfo().getBSSID())
                && isGreaterThanMinimum){
            checkBoxAutoSwitch = settings.getBoolean(Defines.AUTO_SWITCH_PREFS_NAME, false);

            if(checkBoxAutoSwitch){
                //auto switch is checked it's not necessary to create a notification
                Toast.makeText(context, "Switching to: " + bestWifiConfig.SSID, Toast.LENGTH_LONG).show();
                WifiUtils.connectToWifi(wifiManager, bestWifiConfig.networkId);
            }else{
                createNotification(bestResult, currentWifi, context);
                return;
            }
        }
        //cancel old notifications
        WifiUtils.cancelNotification(context);
        return;
    }

    private void sendBroadcastToFillWifiList(ArrayList<String> arrayWifiInfo) {
        Intent broadcastIntent = new Intent(Defines.FILL_DATA);
        broadcastIntent.putStringArrayListExtra("data", arrayWifiInfo);
        ctx.sendBroadcast(broadcastIntent);
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
                .setSmallIcon(R.drawable.ic_launcher).setAutoCancel(true);
        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_activity);
        contentView.setOnClickPendingIntent(R.id.yesButton, pYesButton);
        contentView.setOnClickPendingIntent(R.id.noButton, pNoButton);
        mBuilder.setContent(contentView);

        //altera o conteudo do texto da notificacao
        contentView.setTextViewText(R.id.txtSwitchinfo, "Switch to: " + newWifi.SSID);

        contentView.setTextViewText(R.id.txtCurrentWifi, "Current: " + currentWifi.SSID+": "
                + currentWifi.level);
        contentView.setTextViewText(R.id.txtNewWifi, "New:      " + newWifi.SSID + ": "
                + newWifi.level);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(Defines.NOTIFICATION_ID, mBuilder.build());
    }
}
