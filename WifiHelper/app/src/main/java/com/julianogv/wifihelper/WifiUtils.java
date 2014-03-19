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

    public static void createNotification(ScanResult newWifi, ScanResult currentWifi, Context context){
        //yesButton Listener
        Intent yesButtonIntent = new Intent(context, ButtonHandlerService.class);
        yesButtonIntent.setAction(Defines.YES_ACTION);
        yesButtonIntent.putExtra("ssid", newWifi.SSID);
        PendingIntent pYesButton = PendingIntent.getService(context, 0, yesButtonIntent,
                android.content.Intent.FLAG_ACTIVITY_NEW_TASK);

        //noButton Listener
        Intent noButtonIntent = new Intent(context, ButtonHandlerService.class);
        noButtonIntent.setAction(Defines.NO_ACTION);
        PendingIntent pNoButton = PendingIntent.getService(context, 1, noButtonIntent,
                android.content.Intent.FLAG_ACTIVITY_NEW_TASK);


        Notification.Builder mBuilder = new Notification.Builder(context).setSmallIcon(R.drawable.ic_launcher);
        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_activity);
        contentView.setOnClickPendingIntent(R.id.yesButton, pYesButton);
        contentView.setOnClickPendingIntent(R.id.noButton, pNoButton);
        mBuilder.setContent(contentView);

        //altera o conteudo do texto da notificacao
        contentView.setTextViewText(R.id.txtNotificationText, "Switch to: " + newWifi.SSID);

        contentView.setTextViewText(R.id.txtWifiInfo, "Current: " + currentWifi.SSID+": " + currentWifi.level + "\nNew:      " + newWifi.SSID + ": " + newWifi.level);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(Defines.NOTIFICATION_ID);
        manager.notify(Defines.NOTIFICATION_ID, mBuilder.getNotification());
    }

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
