/*
 * Copyright (c) 2017 PT Studio. All rights reserved.
 * Licensed under Apache License 2.0 (https://github.com/tienthanh2509/iWownController/blob/master/LICENSE)
 */

package tk.d13ht01.bracelet;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;

import tk.d13ht01.bracelet.utils.ThemeUtil;

/**
 * Created by ptthanh on 7/31/2017.
 */
public abstract class BaseActivity extends AppCompatActivity {
    public int mTheme = -1;

    @Override
    protected void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        ThemeUtil.setTheme(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        ThemeUtil.reloadTheme(this);
    }

    @SuppressWarnings("deprecation")
    public void setFloating(android.support.v7.widget.Toolbar toolbar, @StringRes int details) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
    }
}