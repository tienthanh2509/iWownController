/*
 * Copyright (c) 2017 PT Studio all rights reserved.
 * Licensed under MIT
 */

package tk.d13ht01.bracelet.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.afollestad.materialdialogs.prefs.MaterialListPreference;

public class ListPreferenceSummaryFix extends MaterialListPreference {
    public ListPreferenceSummaryFix(Context context) {
        super(context);
    }

    public ListPreferenceSummaryFix(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);
        notifyChanged();
    }
}