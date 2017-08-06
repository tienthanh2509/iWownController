/*
 * Copyright (c) 2017 PT Studio all rights reserved.
 * Licensed under Apache License 2.0 (https://github.com/tienthanh2509/iWownController/blob/master/LICENSE)
 */

package tk.d13ht01.bracelet.model;

import android.content.SharedPreferences;

import tk.d13ht01.bracelet.MyApp;

/**
 * Created by Aloyan Dmitry on 16.09.2015
 */
public class AppNotification {
    public static Integer canNotice(String packageName) {
        if (MyApp.getPreferences().contains("appnotif_" + packageName))
            return MyApp.getPreferences().getInt("appnotif_" + packageName, 0);
        return 0;
    }

    public static void enableApp(String packageName, Integer type) {
        SharedPreferences.Editor ed = MyApp.getPreferences().edit();
        ed.putInt("appnotif_" + packageName, type);
        ed.apply();
    }

    public static void disableApp(String packageName) {
        if (!MyApp.getPreferences().contains("appnotif_" + packageName))
            return;
        SharedPreferences.Editor ed = MyApp.getPreferences().edit();
        ed.remove("appnotif_" + packageName);
        ed.apply();
    }
}
