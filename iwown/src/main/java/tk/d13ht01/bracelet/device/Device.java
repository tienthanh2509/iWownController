package tk.d13ht01.bracelet.device;

import android.bluetooth.BluetoothGattCharacteristic;

import tk.d13ht01.bracelet.bluetooth.Communication;
import tk.d13ht01.bracelet.model.DeviceClockAlarm;

/**
 * Created by ptthanh on 7/30/2017.
 */

public interface Device {
    public Communication getComm();

    /**
     * Send request get firmware version
     */
    void askFmVersionInfo();

    /**
     * Send request get current battery level
     */
    void askPower();

    /**
     * Send request get device config
     */
    void askConfig();

    /**
     * Send request get user params
     */
    void askUserParams();

    /**
     * Send request get ble setting
     */
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
