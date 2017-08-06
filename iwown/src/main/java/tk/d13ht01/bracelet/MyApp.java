/*
 * Copyright (c) 2017 PT Studio all rights reserved.
 * Licensed under MIT
 */

package tk.d13ht01.bracelet;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.squareup.leakcanary.LeakCanary;

import tk.d13ht01.bracelet.service.GoogleFitConnector;
import tk.d13ht01.bracelet.service.impl.BleServiceImpl;
import tk.d13ht01.bracelet.utils.CommunicationUtils;

/**
 * Created by Aloyan Dmitry on 29.08.2015
 */
public class MyApp extends Application {
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    private static SharedPreferences mPref;

    public static Context getmContext() {
        return mContext;
    }

    public static SharedPreferences getPreferences() {
        return mPref;
    }

    private static void loadProperties() {
        if (getPreferences().getBoolean("fit_connected", false))
            GoogleFitConnector.connect(mContext);
    }

    /**
     * LeakCanary will automatically show a notification when an activity memory leak is detected in your debug build.
     */
    private void loadLeakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        loadLeakCanary();

        mContext = getApplicationContext();
        mPref = PreferenceManager.getDefaultSharedPreferences(getmContext());

        if (CommunicationUtils.isBluetoothAvailable()) {
            // Create service
            Intent gattServiceIntent = new Intent(this, BleServiceImpl.class);
            startService(gattServiceIntent);

            PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                    new Intent(this, WelcomeActivity.class), 0);

            Notification notification = new NotificationCompat.Builder(this)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                    .setSmallIcon(R.drawable.ic_android_head_24dp_black)
                    .setContentText("Iwown bracelet controller")
                    .setContentTitle("Bracelet Controller")
                    .setTicker("Bracelet Controller")
                    .setPriority(Notification.PRIORITY_LOW)
                    .setAutoCancel(false)
                    .setContentIntent(pi)
                    .build();

            notification.flags |= Notification.FLAG_FOREGROUND_SERVICE | Notification.FLAG_INSISTENT;// Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT |
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(0, notification);
            loadProperties();
        } else {
            Toast.makeText(MyApp.getmContext(), R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }
}
