package tk.d13ht01.bracelet;

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

import tk.d13ht01.bracelet.R;
import tk.d13ht01.bracelet.service.BLEService;
import tk.d13ht01.bracelet.ui.WelcomeActivity;
import tk.d13ht01.bracelet.utils.CommunicationUtils;

/**
 * Created by Aloyan Dmitry on 29.08.2015
 */
public class MyApp extends Application {
    public static Context mContext;
    public static MyApp mInstance;
    public static SharedPreferences mPref;

    public static MyApp getInstance() {
        return mInstance;
    }
    public static SharedPreferences getPreferences() {
        return MyApp.mPref;
    }

    private static void loadProperties() {
        SharedPreferences sp = MyApp.mPref;
        if (sp.getBoolean("fit_connected", false))
            GoogleFitConnector.connect(MyApp.mContext);
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

        mInstance = this;
        MyApp.mContext = getApplicationContext();
        MyApp.mPref = PreferenceManager.getDefaultSharedPreferences(MyApp.mContext);

        if (CommunicationUtils.isBluetoothAvailable()) {
            // Create service
            Intent gattServiceIntent = new Intent(this, BLEService.class);
            startService(gattServiceIntent);

            PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                    new Intent(this, WelcomeActivity.class), 0);

            Notification notification = new NotificationCompat.Builder(this)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                    .setSmallIcon(R.drawable.ic_android_head_24dp_black)
                    .setContentText("IwownFit Controller")
                    .setContentTitle("IwownFit Controller")
                    .setTicker("IwownFit Controller")
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
}
