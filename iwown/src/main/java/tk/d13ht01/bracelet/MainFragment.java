/*
 * Copyright (c) 2017 PT Studio all rights reserved.
 * Licensed under MIT
 */

package tk.d13ht01.bracelet;


import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

import tk.d13ht01.bracelet.common.BroadcastConstants;
import tk.d13ht01.bracelet.model.DeviceInfo;
import tk.d13ht01.bracelet.service.impl.BleServiceImpl;

public class MainFragment extends Fragment {
    private static final String TAG = MainFragment.class.getName();

    private Activity activity;

    private TextView txtBraceletModel, txtBraceletFirmware, txtBraceletBattery, txtBraceletTime;
    private TextView txtConnectStatus;
    private View txtConnectContainer;
    private ImageView txtConnectStatusIcon;

    /**
     * Handles various events fired by the Service.
     *
     * ACTION_GATT_CONNECTED: connected to a GATT server.
     * ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
     * ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
     */
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            final Intent in = intent;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final String action = in.getAction();
                    switch (action) {
                        case BroadcastConstants.ACTION_GATT_CONNECTED:
                            updateDeviceStatus(0);
                            break;
                        case BroadcastConstants.ACTION_GATT_DISCONNECTED:
                            updateDeviceStatus(1);
                            break;
                        case BroadcastConstants.ACTION_GATT_SERVICES_DISCOVERED:
                            updateDeviceStatus(0);
                            requestDeviceInfo();
                            break;
                        case BroadcastConstants.ACTION_DEVICE_INFO:
                            final DeviceInfo info = (DeviceInfo) in.getSerializableExtra("data");
                            txtBraceletModel.setText(info.getModel());
                            txtBraceletFirmware.setText(info.getSwversion());
                            break;
                        case BroadcastConstants.ACTION_DEVICE_POWER:
                            txtBraceletBattery.setText(in.getIntExtra("data", 0) + "%");
                            break;
                        case BroadcastConstants.ACTION_DATE_DATA:
                            long timestamp = in.getLongExtra("data", 0);
                            if (timestamp <= 0)
                                txtBraceletTime.setText(getResources().getText(R.string.label_wrong_time));
                            else {
                                CharSequence dt = android.text.format.DateFormat.format("dd/MM/yyyy HH:mm", new Date(timestamp));
                                txtBraceletTime.setText(dt);
                            }
                            BleServiceImpl.getInstance().getDevice().setDate();
                            break;
                        case BroadcastConstants.ACTION_CONNECT_TO_GFIT:
//                            if (MyApp.getPreferences().getBoolean("fit_connected", false) == false) {
//                                Toast.makeText(getActivity(), getResources().getString(R.string.google_fit_not_connected),
//                                        Toast.LENGTH_SHORT).show();
////                                ((Button) container.findViewById(R.id.connectToFitBtn))
////                                        .setText(getResources().getString(R.string.connect_to_fit));
//                                return;
//                            }
//
//                            Toast.makeText(getActivity(), getResources().getString(R.string.google_fit_connected),
//                                    Toast.LENGTH_SHORT).show();
//                            ((Button) container.findViewById(R.id.connectToFitBtn))
//                                    .setText(getResources().getString(R.string.reconnect_to_fit));
//                            if (BleService.getInstance() == null || BleService.getInstance().getDevice() == null)
//                                return;
//
////                            BleService.getInstance().getDevice().askDailyData();
//                            BleService.getInstance().getDevice().subscribeForSportUpdates();
                            break;
                    }
                }
            });
        }
    };

    public static IntentFilter gattIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastConstants.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BroadcastConstants.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BroadcastConstants.ACTION_GATT_SERVICES_DISCOVERED);

        intentFilter.addAction(BroadcastConstants.ACTION_DEVICE_INFO);
        intentFilter.addAction(BroadcastConstants.ACTION_DEVICE_POWER);
        intentFilter.addAction(BroadcastConstants.ACTION_DATE_DATA);
        intentFilter.addAction(BroadcastConstants.ACTION_CONNECT_TO_GFIT);

        return intentFilter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        activity = getActivity();
        setHasOptionsMenu(true);

        txtBraceletModel = (TextView) view.findViewById(R.id.bracelet_model);
        txtBraceletFirmware = (TextView) view.findViewById(R.id.bracelet_firmware);
        txtBraceletBattery = (TextView) view.findViewById(R.id.bracelet_battery_level);
        txtBraceletTime = (TextView) view.findViewById(R.id.bracelet_time);

        txtConnectStatus = (TextView) view.findViewById(R.id.connect_status);
        txtConnectContainer = view.findViewById(R.id.status_container);
        txtConnectStatusIcon = (ImageView) view.findViewById(R.id.status_icon);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_device_discovery:
                startActivity(new Intent(activity, DeviceScanActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onStart() {
        super.onStart();

        activity.registerReceiver(mGattUpdateReceiver, gattIntentFilter());

        View view = getView();

        if (view == null) {
            Toast.makeText(activity, "getView failed!", Toast.LENGTH_LONG).show();
            activity.finish();
            return;
        }

        checkDeviceConntected();
    }

    @Override
    public void onResume() {
        super.onResume();

        activity.registerReceiver(mGattUpdateReceiver, gattIntentFilter());
        checkDeviceConntected();
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            activity.unregisterReceiver(mGattUpdateReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Send request basic data to bracelet
     */
    private void requestDeviceInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BleServiceImpl.getInstance().getDevice().askFmVersionInfo();
                    Thread.sleep(500);
                    BleServiceImpl.getInstance().getDevice().askPower();
                    Thread.sleep(500);
                    BleServiceImpl.getInstance().getDevice().askDate();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Update GUI by code
     * <p>
     * 0 - Worked
     * 1: Lost connect
     * 2: Not connect
     *
     * @param code int
     */
    private void updateDeviceStatus(int code) {
        switch (code) {
            case 0: {
                txtConnectStatus.setText(String.format("%s is connected!", BleServiceImpl.getInstance().getmBluetoothGatt().getDevice().getName()));
                txtConnectStatus.setTextColor(ContextCompat.getColor(activity, R.color.darker_green));
                txtConnectContainer.setBackgroundColor(ContextCompat.getColor(activity, R.color.darker_green));
                txtConnectStatusIcon.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_check_circle_84dp_white));
                break;
            }
            case 1: {
                txtConnectStatus.setText(R.string.device_not_connected);
                txtConnectStatus.setTextColor(ContextCompat.getColor(activity, R.color.amber_500));
                txtConnectContainer.setBackgroundColor(ContextCompat.getColor(activity, R.color.amber_500));
                txtConnectStatusIcon.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_warning_84dp_white));
                break;
            }
            case 2: {
                txtConnectStatus.setText(R.string.device_not_setup);
                txtConnectStatus.setTextColor(ContextCompat.getColor(activity, R.color.warning));
                txtConnectContainer.setBackgroundColor(ContextCompat.getColor(activity, R.color.warning));
                txtConnectStatusIcon.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_error_84dp_white));
                break;
            }
        }
    }

    /**
     * Update device connected indicator
     */
    private void checkDeviceConntected() {
        if (MyApp.getPreferences().getString("device_mac_address", "").equals("")) {
            updateDeviceStatus(2);
        } else {
            if (BleServiceImpl.getInstance() != null &&
                    BleServiceImpl.getInstance().getmBluetoothGatt() != null &&
                    BleServiceImpl.getInstance().getmBluetoothGatt().getDevice() != null) {
                updateDeviceStatus(0);
                requestDeviceInfo();
            } else {
                updateDeviceStatus(1);
                if (BleServiceImpl.getInstance() != null)
                    BleServiceImpl.getInstance().connect(MyApp.getPreferences().getString("device_mac_address", ""), true);
            }
        }
    }
}
