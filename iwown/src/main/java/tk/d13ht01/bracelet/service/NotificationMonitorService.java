/*
 * Copyright (c) 2017 PT Studio all rights reserved.
 * Licensed under MIT
 */

package tk.d13ht01.bracelet.service;

import android.content.Intent;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import tk.d13ht01.bracelet.common.BroadcastConstants;
import tk.d13ht01.bracelet.model.AppNotification;
import tk.d13ht01.bracelet.model.Notification;

/**
 * Created by Aloyan Dmitry on 30.08.2015
 */
public class NotificationMonitorService extends NotificationListenerService {
    public static StatusBarNotification lastSbn;

    public static boolean settingsKeepForeign = true;
    public static int settingsDelay = 0;

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.i("NOTIFICATION", "From package: " + sbn.getPackageName());
        lastSbn = sbn;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (settingsDelay != 0) {
                        StatusBarNotification notif = lastSbn.clone();
                        Thread.sleep(settingsDelay * 1000);
                        if (lastSbn == null || lastSbn.getPostTime() != notif.getPostTime()) return;
                    }
                    int canNotice = AppNotification.canNotice(lastSbn.getPackageName());
                    if (canNotice > 0 && lastSbn.isClearable()) {
                        Intent intent = new Intent(BroadcastConstants.ACTION_NEW_NOTIFICATION_RECEIVED);
                        intent.putExtra("data", Notification.fromSbn(lastSbn, canNotice));
                        sendBroadcast(intent);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (lastSbn != null)
                    Log.i("NOTIFICATION", "Package: " + lastSbn.getPackageName() + " skipped");
            }
        }).start();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // Log.i("NOTIF!", "On remove");
        if (settingsDelay != 0) {
            if (lastSbn != null && sbn.getPackageName().equals(lastSbn.getPackageName())) {
                lastSbn = null;
            }
        }
    }

    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

}