package tk.d13ht01.bracelet;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.HashMap;

import tk.d13ht01.bracelet.common.BroadcastConstants;
import tk.d13ht01.bracelet.service.impl.BleServiceImpl;
import tk.d13ht01.bracelet.service.NotificationMonitorService;

public class DeviceSettingsActivity extends AppCompatActivity {
    ProgressDialog dialog;
    IntentFilter inFilter;
    private int packetIterator = 0;
    private final BroadcastReceiver resultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            packetIterator++;
            SharedPreferences.Editor ed = MyApp.getPreferences().edit();
            final String action = intent.getAction();
            switch (action) {
                case BroadcastConstants.ACTION_BLE_DATA:
                    ed.putBoolean("dev_conf_ble", intent.getIntExtra("data", 0) > 0);
                    break;
                case BroadcastConstants.ACTION_USER_BODY_DATA:
                    HashMap<String, Integer> userData = (HashMap<String, Integer>) intent.getSerializableExtra("data");

                    ed.putString("dev_conf_goal", Integer.toString(userData.get("goal_high")));
                    ed.putString("dev_conf_weight", Integer.toString(userData.get("weight")));
                    ed.putString("dev_conf_height", Integer.toString(userData.get("height")));
                    ed.putString("dev_conf_age", Integer.toString(userData.get("age")));
                    ed.putString("dev_conf_gender", Integer.toString(userData.get("gender")));
                    break;
                case BroadcastConstants.ACTION_DEVICE_CONF_DATA:
                    HashMap<String, Integer> confData = (HashMap<String, Integer>) intent.getSerializableExtra("data");

                    ed.putBoolean("dev_conf_light", confData.get("light") > 0);
                    ed.putBoolean("dev_conf_gesture", confData.get("gesture") > 0);
                    ed.putBoolean("dev_conf_englishunits", confData.get("englishUnits") > 0);
                    ed.putBoolean("dev_conf_use24hours", confData.get("use24hour") > 0);
                    ed.putBoolean("dev_conf_autosleep", confData.get("autoSleep") > 0);
                    break;
            }
            ed.apply();
            if (packetIterator >= 3) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                    PreferenceManager.setDefaultValues(DeviceSettingsActivity.this, R.xml.pref_general, true);
                    getFragmentManager().beginTransaction()
                            .replace(android.R.id.content, new prefGeneral())
                            .commit();
                }
            }
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (BleServiceImpl.getInstance() == null || BleServiceImpl.getInstance().getDevice() == null) {
            Toast.makeText(this, R.string.device_settings_device_not_connected, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        inFilter = new IntentFilter();
        inFilter.addAction(BroadcastConstants.ACTION_DEVICE_CONF_DATA);
        inFilter.addAction(BroadcastConstants.ACTION_BLE_DATA);
        inFilter.addAction(BroadcastConstants.ACTION_USER_BODY_DATA);

        dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage(getResources().getString(R.string.device_settings_wait_device_alert));
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BleServiceImpl.getInstance().getDevice().askBle();
                    Thread.sleep(500);
                    BleServiceImpl.getInstance().getDevice().askConfig();
                    Thread.sleep(500);
                    BleServiceImpl.getInstance().getDevice().askUserParams();
                } catch (Exception ignored) {
                }
            }
        }).start();
        dialog.show();

        registerReceiver(resultReceiver, inFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        new Saver().run();
        unregisterReceiver(resultReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        new Saver().run();

        if (BleServiceImpl.getInstance() == null || BleServiceImpl.getInstance().getDevice() == null)
            return;

    }

    public static class prefGeneral extends PreferenceFragment {
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
        }
    }

    private class Saver implements Runnable {
        @Override
        public void run() {
            int goal_high = Integer.parseInt(MyApp.getPreferences().getString("dev_conf_goal", "1000"));
            int weight = Integer.parseInt(MyApp.getPreferences().getString("dev_conf_weight", "70"));
            int height = Integer.parseInt(MyApp.getPreferences().getString("dev_conf_height", "180"));
            int age = Integer.parseInt(MyApp.getPreferences().getString("dev_conf_age", "26"));
            int gender = Integer.parseInt(MyApp.getPreferences().getString("dev_conf_gender", "0"));
            BleServiceImpl.getInstance().getDevice().setUserParams(height, weight, gender > 0, age, goal_high);

            boolean light = MyApp.getPreferences().getBoolean("dev_conf_light", false);
            boolean gesture = MyApp.getPreferences().getBoolean("dev_conf_gesture", false);
            boolean englishunits = MyApp.getPreferences().getBoolean("dev_conf_englishunits", false);
            boolean use24hours = MyApp.getPreferences().getBoolean("dev_conf_use24hours", false);
            boolean autosleep = MyApp.getPreferences().getBoolean("dev_conf_autosleep", false);
            BleServiceImpl.getInstance().getDevice().setConfig(light, gesture, englishunits,
                    use24hours, autosleep);

            BleServiceImpl.getInstance().getDevice().setBle(MyApp.getPreferences().getBoolean("dev_conf_ble", false));

            NotificationMonitorService.settingsKeepForeign = MyApp.getPreferences().getBoolean("notif_foreign", false);
            NotificationMonitorService.settingsDelay = Integer.parseInt(MyApp.getPreferences().getString("notif_delay", "0"));

        }
    }
}
