/*
 * Copyright (c) 2017 PT Studio. All rights reserved.
 * Licensed under Apache License 2.0 (https://github.com/tienthanh2509/iWownController/blob/master/LICENSE)
 */

package tk.d13ht01.bracelet.service.impl;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import tk.d13ht01.bracelet.MyApp;
import tk.d13ht01.bracelet.service.bluetooth.Communication;
import tk.d13ht01.bracelet.common.BleServiceConstants;
import tk.d13ht01.bracelet.common.WristbandModel;
import tk.d13ht01.bracelet.model.device.Device;
import tk.d13ht01.bracelet.model.device.GenericDevice;
import tk.d13ht01.bracelet.model.device.I5Device;
import tk.d13ht01.bracelet.model.device.I7s2Device;
import tk.d13ht01.bracelet.service.BleService;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BleServiceImpl extends Service implements BleService {
    private final static String TAG = BleServiceImpl.class.getSimpleName();
    private static BleServiceImpl instance;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;

    private ArrayList<BluetoothGattCharacteristic> characteristics = new ArrayList<>();
    private ArrayList<BluetoothGattService> services = new ArrayList<>();

    private Device device;
    private int mConnectionState = BleServiceConstants.STATE_DISCONNECTED;
    private boolean alreadyChecking = false;

    public static BleServiceImpl getInstance() {
        return instance;
    }

    public ArrayList<BluetoothGattCharacteristic> getCharacteristics() {
        return characteristics;
    }

    public void setCharacteristics(ArrayList<BluetoothGattCharacteristic> characteristics) {
        this.characteristics = characteristics;
    }

    public ArrayList<BluetoothGattService> getServices() {
        return services;
    }

    public void setServices(ArrayList<BluetoothGattService> services) {
        this.services = services;
    }

    @Override
    public int getmConnectionState() {
        return mConnectionState;
    }

    @Override
    public void setmConnectionState(int mConnectionState) {
        this.mConnectionState = mConnectionState;
    }


    @Override
    public Device getDevice() {
        return this.device;
    }

    @Override
    public BluetoothGatt getmBluetoothGatt() {
        return this.mBluetoothGatt;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        if (MyApp.getPreferences().getString("device_model", "").equals(WristbandModel.MODEL_I7S2))
            this.device = new I7s2Device(this);
        else if (MyApp.getPreferences().getString("device_model", "").equals(WristbandModel.MODEL_I5PLUS))
            this.device = new I5Device(this);
        else
            this.device = new GenericDevice(this);

        if (!initialize()) {
            Log.e(TAG, "Unable to initialize Bluetooth");
            return;
        }
        this.connect(MyApp.getPreferences().getString("device_mac_address", ""), true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    @Override
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    @Override
    public boolean connect(final String address, boolean forceConnect) {
        if (mBluetoothAdapter == null || address == null ||
                (MyApp.getPreferences().getString("device_mac_address", "").length() <= 0) && address.length() <= 0) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (!forceConnect && MyApp.getPreferences().getString("device_mac_address", "").length() > 0 &&
                address.equals(MyApp.getPreferences().getString("device_mac_address", "")) && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = BleServiceConstants.STATE_CONNECTING;
                Log.d(TAG, "Reconnecting");
                return true;
            } else {
                Log.d(TAG, "Connection problem");
                return false;
            }
        }

        if (forceConnect && mBluetoothGatt != null)
            disconnect();

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found. Unable to connect.");
            return false;
        }
        // We want to directly connect to the device
        mBluetoothGatt = device.connectGatt(this, true, this.device.getComm());
        Log.d(TAG, "Trying to create a new connection.");
        mConnectionState = BleServiceConstants.STATE_CONNECTING;
        this.checkConnection();
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    @Override
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        try {
            mBluetoothGatt.disconnect();
            close();
        } catch (Exception ignored) {

        }
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    @Override
    public void close() {
        if (mBluetoothGatt == null)
            return;
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    @Override
    public BluetoothGattCharacteristic getCharacteristic(UUID uuid) {
        try {
            for (BluetoothGattCharacteristic characteristic : characteristics) {
                if (characteristic.getUuid().equals(uuid)) {
                    return characteristic;
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void checkConnection() {
        alreadyChecking = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (alreadyChecking)
                        return;
                    alreadyChecking = true;
                    Thread.sleep(60000);
                    if (MyApp.getPreferences().getString("device_mac_address", "").length() > 0) {
                        if (isConnected()) {
                            // FIXME bad practice use comm object
                            if (Communication.lastDataReceived - (new Date().getTime()) >= 120000) {
                                BleServiceImpl.getInstance().disconnect();
                                BleServiceImpl.getInstance().connect(MyApp.getPreferences().getString("device_mac_address", ""), true);
                                checkConnection();
                                return;
                            }
                            BleServiceImpl.getInstance().getDevice().askPower();
                            BleServiceImpl.getInstance().getDevice().askDailyData();


                        }
                    }
                    checkConnection();
                } catch (Exception ignored) {
                }
            }
        }).start();
    }

    @Override
    public boolean isConnected() {
        return BleServiceImpl.getInstance() != null && BleServiceImpl.getInstance().getDevice() != null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
