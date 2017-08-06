/*
 * Copyright (c) 2017 PT Studio all rights reserved.
 * Licensed under MIT
 */

package tk.d13ht01.bracelet.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Browser;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.URLSpan;
import android.text.util.Linkify;

import com.afollestad.materialdialogs.MaterialDialog;

import tk.d13ht01.bracelet.MyApp;
import tk.d13ht01.bracelet.R;

public final class NavUtil {

    public static Uri parseURL(String str) {
        if (str == null || str.isEmpty())
            return null;

        Spannable spannable = new SpannableString(str);
        Linkify.addLinks(spannable, Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES);
        URLSpan spans[] = spannable.getSpans(0, spannable.length(), URLSpan.class);
        return (spans.length > 0) ? Uri.parse(spans[0].getURL()) : null;
    }

    public static void startURL(Activity activity, Uri uri) {
        if (!MyApp.getPreferences().getBoolean("chrome_tabs", true)) {
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.putExtra(Browser.EXTRA_APPLICATION_ID, activity.getPackageName());
            activity.startActivity(intent);
            return;
        }

        CustomTabsIntent.Builder customTabsIntent = new CustomTabsIntent.Builder();
        customTabsIntent.setShowTitle(true);
        customTabsIntent.setToolbarColor(ContextCompat.getColor(activity, R.color.colorPrimary));
        customTabsIntent.build().launchUrl(activity, uri);
    }

    public static void startURL(Activity activity, String url) {
        startURL(activity, parseURL(url));
    }

    public static void showMessage(@NonNull Context context, CharSequence message) {
        new MaterialDialog.Builder(context)
                .content(message)
                .positiveText(android.R.string.ok)
                .show();
    }
}
