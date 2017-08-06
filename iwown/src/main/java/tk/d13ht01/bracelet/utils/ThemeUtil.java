/*
 * Copyright (c) 2017 PT Studio all rights reserved.
 * Licensed under MIT
 */

package tk.d13ht01.bracelet.utils;

import android.content.Context;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;

import tk.d13ht01.bracelet.BaseActivity;
import tk.d13ht01.bracelet.MyApp;
import tk.d13ht01.bracelet.R;

public final class ThemeUtil {
    private static int[] THEMES = new int[]{
            R.style.AppTheme_Light,
            R.style.AppTheme_Dark,
            R.style.AppTheme_Dark_Black,};

    private ThemeUtil() {
    }

    public static int getSelectTheme() {
        int theme = MyApp.getPreferences().getInt("theme", 0);
        return (theme >= 0 && theme < THEMES.length) ? theme : 0;
    }

    public static void setTheme(BaseActivity activity) {
        activity.mTheme = getSelectTheme();
        activity.setTheme(THEMES[activity.mTheme]);
    }

    public static void reloadTheme(BaseActivity activity) {
        int theme = getSelectTheme();
        if (theme != activity.mTheme)
            activity.recreate();
    }

    public static int getThemeColor(Context context, int id) {
        Theme theme = context.getTheme();
        TypedArray a = theme.obtainStyledAttributes(new int[]{id});
        int result = a.getColor(0, 0);
        a.recycle();
        return result;
    }
}
