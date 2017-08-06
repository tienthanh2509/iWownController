package tk.d13ht01.bracelet.service;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import java.util.UUID;

import tk.d13ht01.bracelet.model.device.Device;

/**
 * Created by ptthanh on 8/6/2017.
 */

public interface BleService {
    int getmConnectionState();

    void setmConnectionState(int mConnectionState);

    Device getDevice();

    BluetoothGatt getmBluetoothGatt();

    boolean initialize();

    boolean connect(String address, boolean forceConnect);

    void disconnect();

    void close();

    BluetoothGattCharacteristic getCharacteristic(UUID uuid);

    void checkConnection();

    boolean isConnected();

}
