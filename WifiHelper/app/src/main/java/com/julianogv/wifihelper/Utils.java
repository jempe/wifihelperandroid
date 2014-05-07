package com.julianogv.wifihelper;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

/**
 * Created by juliano.vieira on 27/04/14.
 */
public class Utils {

    public static int increaseOpenCount(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                Defines.PREFS_NAME, Context.MODE_PRIVATE);
        int loginCount = prefs.getInt(Defines.PREF_LOGIN_COUNT, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(Defines.PREF_LOGIN_COUNT, loginCount + 1);
        editor.commit();
        return loginCount + 1;
    }

    public static void showRateDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.dialog_rate_app_title);
        builder.setMessage(R.string.dialog_rate_app_message);

        EasyTracker.getInstance(context).send(MapBuilder.createEvent("ui_action", // Event category (required)
                        "rate_app_dialog",  // Event action (required)
                        "RATE DIALOG OPENED",   // Event label
                        null)            // Event value
                        .build()
        );

        builder.setPositiveButton(R.string.dialog_rate_app_ok_button, new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EasyTracker.getInstance(context).send(MapBuilder.createEvent("ui_action", // Event category (required)
                                "rate_app_dialog",  // Event action (required)
                                "RATE NOW",   // Event label
                                null)            // Event value
                                .build()
                );
                startGooglePlay(context);
            }
        });

        builder.setNeutralButton(R.string.dialog_rate_app_no_button, new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EasyTracker.getInstance(context).send(MapBuilder.createEvent("ui_action", // Event category (required)
                                "rate_app_dialog",  // Event action (required)
                                "NO THANKS",   // Event label
                                null)            // Event value
                                .build()
                );
            }
        });
        builder.create().show();
    }


    public static void startGooglePlay(final Context context) {
        String url;
        Intent intent;

        try {
            //Check whether Google Play store is installed or not:
            context.getPackageManager().getPackageInfo("com.android.vending", 0);
            url = "market://details?id=" + context.getPackageName();
        } catch (final Exception e) {
            url = "https://play.google.com/store/apps/details?id=" + context.getPackageName();
        }
        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        context.startActivity(intent);
    }
}