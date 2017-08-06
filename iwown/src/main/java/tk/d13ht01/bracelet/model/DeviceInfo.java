/*
 * Copyright (c) 2017 PT Studio all rights reserved.
 * Licensed under Apache License 2.0 (https://github.com/tienthanh2509/iWownController/blob/master/LICENSE)
 */

package tk.d13ht01.bracelet.model;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import tk.d13ht01.bracelet.utils.CommunicationUtils;

/**
 * Created by Dmitry on 30.08.2015.
 */
public class DeviceInfo implements Serializable {
    private String model;
    private int oadmode;
    private String firmwareVersion;
    private String address;
    private int batterryLevel;
    private int displayWidthFont;

    public static DeviceInfo fromData(byte[] data) {
        DeviceInfo info = new DeviceInfo();
        info.setModel(CommunicationUtils.ascii2String(Arrays.copyOfRange(data, 6, 10)));
        info.setOadmode((data[10] * 255) + data[11]);
        info.setFirmwareVersion(data[12] + "." + data[13] + "." + data[14] + "." + data[15]);
        info.setAddress(CommunicationUtils.byteArrayToString(Arrays.copyOfRange(data, 16, 22)));
        if (data.length == 29)
            info.setDisplayWidthFont(CommunicationUtils.bytesToInt(Arrays.copyOfRange(data, 28, 29)));
        else if (data.length == 28)
            info.setDisplayWidthFont(CommunicationUtils.bytesToInt(Arrays.copyOfRange(data, 27, 28)));
        return info;
    }

    public String toString() {
        String buff = "";
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            if ((field.getModifiers() & Modifier.FINAL) == Modifier.FINAL)
                continue;
            try {
                buff += field.getName() + ": " + field.get(this) + " ";
            } catch (Exception e) {
            }
        }
        return buff;
    }

    public String getModel() {
        return this.model;
    }

    public void setModel(String value) {
        this.model = value;
    }

    public int getOadmode() {
        return this.oadmode;
    }

    public void setOadmode(int value) {
        this.oadmode = value;
    }

    public String getFirmwareVersion() {
        return this.firmwareVersion;
    }

    public void setFirmwareVersion(String value) {
        this.firmwareVersion = value;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String value) {
        this.address = value;
    }

    public int getDisplayWidthFont() {
        return this.displayWidthFont;
    }

    public void setDisplayWidthFont(int value) {
        this.displayWidthFont = value;
    }
}
