/*
 * Copyright (c) 2017 PT Studio all rights reserved.
 * Licensed under Apache License 2.0 (https://github.com/tienthanh2509/iWownController/blob/master/LICENSE)
 */

package tk.d13ht01.bracelet;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;

import tk.d13ht01.bracelet.utils.ThemeUtil;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeUtil.setTheme(this);
        setContentView(R.layout.activity_container);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(R.string.nav_item_settings);
            ab.setDisplayHomeAsUpEnabled(true);
        }

        setFloating(toolbar, 0);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new GeneralSettingsFragment()).commit();
        }

    }
}
