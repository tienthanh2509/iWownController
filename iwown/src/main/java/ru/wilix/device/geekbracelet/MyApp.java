package ru.wilix.device.geekbracelet;

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

import ru.wilix.device.geekbracelet.service.BLEService;
import ru.wilix.device.geekbracelet.ui.MainActivity;
import ru.wilix.device.geekbracelet.utils.CommunicationUtils;

/**
 * Created by Aloyan Dmitry on 29.08.2015
 */
public class MyApp extends Application {
    public static Context mContext;
    public static SharedPreferences sPref;

    public static void loadProperties() {
        SharedPreferences sp = MyApp.sPref;
        if (sp.getBoolean("fit_connected", false))
            GoogleFitConnector.connect(MyApp.mContext);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        loadLeakCanary();

        MyApp.mContext = getApplicationContext();
        MyApp.sPref = PreferenceManager.getDefaultSharedPreferences(MyApp.mContext);

        if (CommunicationUtils.isBluetoothAvailable()) {
            // Create service
            Intent gattServiceIntent = new Intent(this, BLEService.class);
            startService(gattServiceIntent);

            PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                    new Intent(this, MainActivity.class), 0);

            Notification notification = new NotificationCompat.Builder(this)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                    .setSmallIcon(R.drawable.ic_watch_24dp_black)
                    .setContentText("WiliX Controller")
                    .setContentTitle("WiliX Controller")
                    .setTicker("WiliX Controller")
                    .setPriority(Notification.PRIORITY_LOW)
                    .setAutoCancel(false)
                    .setContentIntent(pi)
                    .build();

            notification.flags |= Notification.FLAG_FOREGROUND_SERVICE | Notification.FLAG_INSISTENT;// Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT |
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(0, notification);
            loadProperties();
        } else {
            Toast.makeText(MyApp.mContext, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            android.os.Process.killProcess(android.os.Process.myPid());
        }
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
}
