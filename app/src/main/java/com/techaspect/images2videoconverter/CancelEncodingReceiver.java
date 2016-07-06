package com.techaspect.images2videoconverter;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;
import android.util.Log;


/**
 * Created by damandeeps on 7/5/2016.
 */

public class CancelEncodingReceiver extends BroadcastReceiver {
    String TAG = "CancelEncodingReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: " + context);
        context.stopService(new Intent(context,ConverterService.class));
    }
}
