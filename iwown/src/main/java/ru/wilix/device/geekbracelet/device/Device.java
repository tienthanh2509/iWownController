package ru.wilix.device.geekbracelet.device;

import android.bluetooth.BluetoothGattCharacteristic;

import ru.wilix.device.geekbracelet.model.DeviceClockAlarm;

/**
 * Created by ptthanh on 7/30/2017.
 */

interface Device {
    void askFmVersionInfo();

    void askPower();

    void askConfig();

    void askUserParams();

    void askBle();

    void askDailyData();

    void askDate();

    void askLocalSport();

    void subscribeForSportUpdates();

    void setDate();

    void setClockAlarm(DeviceClockAlarm alarm, int sectionId);

    void setBle(boolean enabled);

    void setSelfieMode(boolean enable);

    void setUserParams(int height, int weight, boolean gender, int age, int goal);

    void setConfig(boolean light, boolean gesture, boolean englishUnits,
                   boolean use24hour, boolean autoSleep);

    void sendMessage(String msg);

    void sendCall(String msg);

    void sendCallEnd();

    void sendAlert(String msg, int type);

    void parserAPIv0(BluetoothGattCharacteristic chr);

    void parserAPIv1(BluetoothGattCharacteristic chr);

    void writePacket(byte[] data);
}
