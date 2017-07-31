package ru.wilix.device.geekbracelet.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ru.wilix.device.geekbracelet.MyApp;
import ru.wilix.device.geekbracelet.service.BLEService;

/**
 * Created by Aloyan Dmitry on 29.08.2015
 */
public class DeskClockAlarm extends BroadcastReceiver {
    public static final String ALARM_ALERT_ACTION = "com.android.deskclock.ALARM_ALERT";
    public static final String ALARM_SNOOZE_ACTION = "com.android.deskclock.ALARM_SNOOZE";
    public static final String ALARM_DISMISS_ACTION = "com.android.deskclock.ALARM_DISMISS";
    public static final String ALARM_DONE_ACTION = "com.android.deskclock.ALARM_DONE";
    public static boolean isAlarm = false;

    public void onReceive(Context context, Intent intent) {
        if (!MyApp.sPref.getBoolean("cbx_notice_deskclock", false))
            return;

        if (BLEService.getSelf() == null || BLEService.getSelf().getDevice() == null)
            return;

        String action = intent.getAction();
        switch (action) {
            case ALARM_ALERT_ACTION:
                isAlarm = true;
                BLEService.getSelf().getDevice().sendCall("UP!UP!");
                break;
            case ALARM_SNOOZE_ACTION:
            case ALARM_DISMISS_ACTION:
            case ALARM_DONE_ACTION:
                isAlarm = false;
                BLEService.getSelf().getDevice().sendCallEnd();
                break;
        }
    }
}
