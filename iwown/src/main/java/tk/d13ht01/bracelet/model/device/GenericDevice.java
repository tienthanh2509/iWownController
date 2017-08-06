/*
 * Copyright (c) 2017 PT Studio. All rights reserved.
 * Licensed under Apache License 2.0 (https://github.com/tienthanh2509/iWownController/blob/master/LICENSE)
 */

package tk.d13ht01.bracelet.model.device;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.UUID;

import tk.d13ht01.bracelet.MyApp;
import tk.d13ht01.bracelet.service.bluetooth.Communication;
import tk.d13ht01.bracelet.common.BroadcastConstants;
import tk.d13ht01.bracelet.common.Constants;
import tk.d13ht01.bracelet.model.DeviceClockAlarm;
import tk.d13ht01.bracelet.model.DeviceInfo;
import tk.d13ht01.bracelet.model.Sport;
import tk.d13ht01.bracelet.service.BleService;
import tk.d13ht01.bracelet.service.NotificationMonitorService;
import tk.d13ht01.bracelet.utils.CommunicationUtils;
import tk.d13ht01.bracelet.utils.PebbleBitmapUtil;

/**
 * Created by Dmitry on 29.08.2015.
 * My swversion: 1.1.0.9 I5
 * DEVICE_POWER﹕ bleAddr: 4d2b2a84bec4 displayWidthFont: 0 model: I5 oadmode: 0 swversion: 1.1.0.9
 */
public class GenericDevice implements Device {
    private static final String TAG = GenericDevice.class.getName();
    public Communication comm;
    private byte[] receiveBuffer;
    private int receiveBufferLength = 0;
    private boolean isDataOver = true;

    public GenericDevice(BleService bleService) {
        this.comm = new Communication(bleService, this);
    }

    public Communication getComm() {
        return comm;
    }

    /**
     * Return Firmware version
     */
    @Override
    public void askFmVersionInfo() {
        writePacket(CommunicationUtils.getDataByte(true, CommunicationUtils.form_Header(0, 0), null));
    }

    /**
     * Return battery power
     */
    @Override
    public void askPower() {
        writePacket(CommunicationUtils.getDataByte(true, CommunicationUtils.form_Header(0, 1), null));
    }

    // TODO ask Alarams. Need to test. If send this command, device return all alrms or need
    // specified alarm ID in ask
//    public void askAlarm(){
//        writePacket(CommunicationUtils.getDataByte(true, CommunicationUtils.form_Header(1, 3), null));
//    }

    /**
     * Return device configuration
     */
    @Override
    public void askConfig() {
        writePacket(CommunicationUtils.getDataByte(true, CommunicationUtils.form_Header(1, 9), null));
    }

    /**
     * Return User Body parameters
     */
    @Override
    public void askUserParams() {
        writePacket(CommunicationUtils.getDataByte(true, CommunicationUtils.form_Header(2, 1), null));
    }

    /**
     * Return BLE state
     * TODO need to understand what is this
     */
    @Override
    public void askBle() {
        writePacket(CommunicationUtils.getDataByte(true, CommunicationUtils.form_Header(1, 3), null));
    }

    /**
     * Return Daily Sport entries. This entries automatically clear in device on ask
     */
    @Override
    public void askDailyData() {
        writePacket(CommunicationUtils.getDataByte(true, CommunicationUtils.form_Header(2, 7), null));
    }

    /**
     * Return device data and time
     */
    @Override
    public void askDate() {
        writePacket(CommunicationUtils.getDataByte(true, CommunicationUtils.form_Header(1, 1), null));
    }

    /**
     * Return data of local sport. May be it sleep data...
     * TODO Check what is this data means
     */
    @Override
    public void askLocalSport() {
        writePacket(CommunicationUtils.getDataByte(true, CommunicationUtils.form_Header(2, 5), null));
    }

    /**
     * After subscribe, device will send Sport object on each activity
     * and once in minute if activity don't register
     */
    @Override
    public void subscribeForSportUpdates() {
        writePacket(CommunicationUtils.getDataByte(true, CommunicationUtils.form_Header(2, 3), null));
    }

    /**
     * Set date and time to device internal clock
     */
    @Override
    public void setDate() {
        GregorianCalendar date = new GregorianCalendar();
        ArrayList<Byte> data = new ArrayList<>();
        data.add(((byte) (date.get(Calendar.YEAR) - 2000)));

        data.add(((byte) (date.get(Calendar.MONTH) - 1)));
        data.add(((byte) (date.get(Calendar.DAY_OF_MONTH) - 1)));
        data.add(((byte) date.get(Calendar.HOUR_OF_DAY)));
        data.add(((byte) date.get(Calendar.MINUTE)));
        data.add(((byte) date.get(Calendar.SECOND)));

        writePacket(CommunicationUtils.getDataByte(true, CommunicationUtils.form_Header(1, 0), data));
    }

    /**
     * Sel alarm to device internal memory.
     *
     * @param alarm     - alarm to write
     * @param sectionId - Available 7 alarms. IDS: 0,1,2,3,4,5,6
     */
    @Override
    public void setClockAlarm(DeviceClockAlarm alarm, int sectionId) {
        ArrayList<Byte> data = new ArrayList<>();
        data.add((byte) sectionId);
        data.add((byte) 0);
        data.add((byte) (alarm.isOpen ? alarm.week : 0));
        data.add((byte) alarm.hour);
        data.add((byte) alarm.minute);
        writePacket(CommunicationUtils.getDataByte(true, CommunicationUtils.form_Header(1, 4), data));
    }

    /**
     * Unknown command. Call on send configuration params to device
     */
    @Override
    public void setBle(boolean enabled) {
        ArrayList<Byte> data = new ArrayList<>();
        data.add((byte) 0);
        data.add((byte) (enabled ? 1 : 0));
        writePacket(CommunicationUtils.getDataByte(true, CommunicationUtils.form_Header(1, 2), data));
    }

    /**
     * Set selfie mode. Rise selfie event on one click to button
     *
     * @param enable Enable selfie mode
     */
    @Override
    public void setSelfieMode(boolean enable) {
        ArrayList<Byte> data = new ArrayList<>();
        data.add(enable ? (byte) 1 : (byte) 0);
        writePacket(CommunicationUtils.getDataByte(true, CommunicationUtils.form_Header(4, 0), data));
    }

    /**
     * Send user body and goal params. This params important for calculating steps and distantion
     * FIXME NOT TESTED
     *
     * @param height - Body height. Like 180
     * @param weight - Body weight. Like 79
     * @param gender false = male, true = female
     * @param age    - age in years. Like 26 years
     * @param goal   - in steps. For example 10000 steps per day
     */
    @Override
    public void setUserParams(int height, int weight, boolean gender, int age, int goal) {
        ArrayList<Byte> datas = new ArrayList<>();
        datas.add((byte) height);
        datas.add((byte) weight);
        datas.add((byte) (!gender ? 0 : 1));
        datas.add((byte) age);
        int goal_low = goal % AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY;
        int goal_high = (goal - goal_low) / AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY;
        datas.add((byte) goal_low);
        datas.add((byte) goal_high);

        byte[] data = CommunicationUtils.getDataByte(true, CommunicationUtils.form_Header(2, 0), datas);
        writePacket(data);
    }

    /**
     * Send config to device
     * FIXME NOT TESTED
     *
     * @param light        - Enable blue light blinking
     * @param gesture      - Enable gesture. Turn display on if you try to look at the device
     * @param englishUnits - Use English Units (Miles, Foots and etc.)
     * @param use24hour    - Use 24 hour time format, if false used 12 hour time
     * @param autoSleep    - Enable auto sleep mode when you go to sleep
     */
    @Override
    public void setConfig(boolean light, boolean gesture, boolean englishUnits,
                          boolean use24hour, boolean autoSleep) {
        ArrayList<Byte> datas = new ArrayList<>();

        datas.add((byte) (light ? 1 : 0));
        datas.add((byte) (gesture ? 1 : 0));
        datas.add((byte) (englishUnits ? 1 : 0));
        datas.add((byte) (use24hour ? 1 : 0));
        datas.add((byte) (autoSleep ? 1 : 0));

        if (Communication.apiVersion == 2) {
            datas.add((byte) 1);
            datas.add((byte) 8); // Light Start Time (Whatever that means); Default
            datas.add((byte) 20); // Light End Time (Whatever that means); Default

            datas.add((byte) 0); // Inverse Colors
            datas.add((byte) 0); // Is English
            datas.add((byte) 0); // Disconnect Tip
        } else {
            datas.add((byte) 0);
            datas.add((byte) 0);
        }

        byte[] data = CommunicationUtils.getDataByte(true, CommunicationUtils.form_Header(1, 8), datas);
        writePacket(data);
    }

    /**
     * Show message alert in display
     *
     * @param msg - message to show
     */
    @Override
    public void sendMessage(String msg) {
        sendAlert(msg, Constants.ALERT_TYPE_MESSAGE);
    }

    /**
     * Show call message and vibrate while not receive sendCallEnd command
     *
     * @param msg - mesage to show
     */
    @Override
    public void sendCall(String msg) {
        sendAlert(msg, Constants.ALERT_TYPE_CALL);
    }

    /**
     * End vibration and call event started after sendCall command
     */
    @Override
    public void sendCallEnd() {
        ArrayList<Byte> datas = new ArrayList<>();
        datas.add((byte) 0);
        writePacket(CommunicationUtils.getDataByte(true, CommunicationUtils.form_Header(4, 1), datas));
    }

    /**
     * Send alert to device. Device shows your message and icon (call for type 1, msg for type 2)
     *
     * @param msg  - Text message
     * @param type - Type of alert. 1 - Call type, 2 - Message type
     */
    @Override
    public void sendAlert(String msg, int type) {
        if (msg == null)
            return;
        ArrayList<Byte> datas = new ArrayList<>();
        if (Communication.apiVersion == 2) {
            datas.add((byte) type);
            datas.add((byte) -1);

            byte[] buffer = new byte[0];

            try {
                if (NotificationMonitorService.settingsKeepForeign) {
                    buffer = msg.getBytes("utf-8");
                } else {
                    buffer = msg.replaceAll("[^\u0020-\u0079]", "#").getBytes("utf-8");
                }
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
            }
            for (byte b : buffer)
                datas.add(b);
        } else {
            datas.add((byte) type);
            int i = 0;
            if (msg.length() < 6) {
                while (msg.length() < 6)
                    msg += " ";
            } else if (msg.length() >= 6) {
                msg = msg.substring(0, 6);
            }

            while (i < msg.length()) {
                //            if (msg.charAt(i) < '@' || (msg.charAt(i) < '\u0080' && msg.charAt(i) > '`')) {
                //                char e = msg.charAt(i);
                //                datas.add(Byte.valueOf((byte) 0));
                //                for (byte valueOf : PebbleBitmapUtil.fromString(String.valueOf(e), 8, 1).data) {
                //                    datas.add(Byte.valueOf(valueOf));
                //                }
                //            } else {
                char c = msg.charAt(i);
                datas.add((byte) 1);
                for (byte valueOf2 : PebbleBitmapUtil.fromString(String.valueOf(c), 16, 1).data) {
                    datas.add(valueOf2);
                }
                //            }
                i++;
            }
        }
        byte[] data = CommunicationUtils.getDataByte(true, CommunicationUtils.form_Header(3, 1), datas);
        ArrayList<Communication.WriteDataTask> tasks = new ArrayList<>();
        for (int i = 0; i < data.length; i += 20) {
            byte[] writeData;
            if (i + 20 > data.length) {
                writeData = Arrays.copyOfRange(data, i, data.length);
            } else {
                writeData = Arrays.copyOfRange(data, i, i + 20);
            }
            tasks.add(new Communication.WriteDataTask(UUID.fromString(Constants.BAND_CHARACTERISTIC_NEW_WRITE), writeData));
        }
        comm.WriteDataPacket(tasks);
    }

    @Override
    public void parserAPIv1(BluetoothGattCharacteristic chr) {
        String uuid = chr.getUuid().toString();
        Log.i(TAG, "Parse APIv1 data. UUID:" + uuid);
        if (Constants.BAND_CHARACTERISTIC_NEW_NOTIFY.equals(uuid) || Constants.BAND_CHARACTERISTIC_NEW_INDICATE.equals(uuid)) {
            byte[] data = chr.getValue();
            if (data != null && data.length != 0) {
                if (this.isDataOver) {
                    if (data[0] == 34 || (Communication.apiVersion == 2 && data[0] == 35)) {
                        this.receiveBufferLength = data[3];
                        //Log.i(TAG, "Received length --->" + this.receiveBufferLength);
                        //Log.i(TAG, "Received data --->" + CommunicationUtils.bytesToString(data));
                    } else {
                        return;
                    }
                }
                this.receiveBuffer = CommunicationUtils.concat(this.receiveBuffer, data);
                if (this.receiveBuffer.length - 4 >= this.receiveBufferLength) {
                    this.isDataOver = true;
                    //Log.i(TAG, "Received length--->" + (this.receiveBuffer.length - 4));
                    //Log.i(TAG, "Received data--->" + CommunicationUtils.bytesToString(this.receiveBuffer));
                    // Data ready for parse
                    if (this.receiveBuffer.length >= 3) {
                        Intent intent;
                        SharedPreferences.Editor ed = MyApp.getPreferences().edit();
                        switch (this.receiveBuffer[2]) {
                            case Constants.APIv1_DATA_DEVICE_INFO:
                                DeviceInfo info = DeviceInfo.fromData(this.receiveBuffer);

                                ed.putString("device_model", info.getModel());
                                ed.putString("device_firmware_version", info.getFirmwareVersion());
                                ed.apply();

                                Log.d(TAG, "DEVICE_INFO: " + info.toString());
                                intent = new Intent(BroadcastConstants.ACTION_DEVICE_INFO);
                                intent.putExtra("data", DeviceInfo.fromData(this.receiveBuffer));
                                MyApp.getmContext().sendBroadcast(intent);
                                break;
                            case Constants.APIv1_DATA_DEVICE_POWER:
                                int power = CommunicationUtils.bytesToInt(Arrays.copyOfRange(this.receiveBuffer, 4, 5));
                                ed.putInt("device_battery_level", power);
                                ed.apply();

                                Log.d(TAG, "DEVICE_POWER: " + power + "%");

                                intent = new Intent(BroadcastConstants.ACTION_DEVICE_POWER);
                                intent.putExtra("data", power);
                                MyApp.getmContext().sendBroadcast(intent);
                                break;
                            case Constants.APIv1_DATA_DEVICE_BLE:
                                int ble = CommunicationUtils.bytesToInt(Arrays.copyOfRange(this.receiveBuffer, 4, 5));
                                Log.d(TAG, "DEVICE_BLE: " + (ble > 0 ? "enabled" : "disabled"));
                                intent = new Intent(BroadcastConstants.ACTION_BLE_DATA);
                                intent.putExtra("data", ble);
                                MyApp.getmContext().sendBroadcast(intent);
                                break;
                            case Constants.APIv1_DATA_USER_PARAMS:
                                HashMap<String, Integer> userData = new HashMap<>();
                                userData.put("height", CommunicationUtils.bytesToInt(Arrays.copyOfRange(this.receiveBuffer, 4, 5)));
                                userData.put("weight", CommunicationUtils.bytesToInt(Arrays.copyOfRange(this.receiveBuffer, 5, 6)));
                                userData.put("gender", CommunicationUtils.bytesToInt(Arrays.copyOfRange(this.receiveBuffer, 6, 7)));
                                userData.put("age", CommunicationUtils.bytesToInt(Arrays.copyOfRange(this.receiveBuffer, 7, 8)));
                                userData.put("goal_low", CommunicationUtils.bytesToInt(Arrays.copyOfRange(this.receiveBuffer, 8, 9)));
                                userData.put("goal_high", CommunicationUtils.bytesToInt(Arrays.copyOfRange(this.receiveBuffer, 9, 10)));
                                Log.d(TAG, "DEVICE_USER_PARAMS: " +
                                        " Height: " + userData.get("height") +
                                        " Weight: " + userData.get("weight") +
                                        " Gender: " + (userData.get("gender") > 0 ? "female" : "male") +
                                        " Age: " + userData.get("age") +
                                        " Goal: " + (userData.get("goal_high") + "." + userData.get("goal_low")));
                                intent = new Intent(BroadcastConstants.ACTION_USER_BODY_DATA);
                                intent.putExtra("data", userData);
                                MyApp.getmContext().sendBroadcast(intent);
                                break;
                            case Constants.APIv1_DATA_DEVICE_CONFIG:
                                HashMap<String, Integer> configData = new HashMap<>();
                                configData.put("light", CommunicationUtils.bytesToInt(Arrays.copyOfRange(this.receiveBuffer, 4, 5)));
                                configData.put("gesture", CommunicationUtils.bytesToInt(Arrays.copyOfRange(this.receiveBuffer, 5, 6)));
                                configData.put("englishUnits", CommunicationUtils.bytesToInt(Arrays.copyOfRange(this.receiveBuffer, 6, 7)));
                                configData.put("use24hour", CommunicationUtils.bytesToInt(Arrays.copyOfRange(this.receiveBuffer, 7, 8)));
                                configData.put("autoSleep", CommunicationUtils.bytesToInt(Arrays.copyOfRange(this.receiveBuffer, 8, 9)));

                                Log.d(TAG, "DEVICE_DEVICE_CONFIG: " +
                                        " light: " + configData.get("light") +
                                        " gesture: " + configData.get("gesture") +
                                        " englishUnits: " + configData.get("englishUnits") +
                                        " use24hour: " + configData.get("use24hour") +
                                        " autoSleep: " + configData.get("autoSleep"));
                                intent = new Intent(BroadcastConstants.ACTION_DEVICE_CONF_DATA);
                                intent.putExtra("data", configData);
                                MyApp.getmContext().sendBroadcast(intent);
                                break;
                            case Constants.APIv1_DATA_DEVICE_DATE:
                                int year = CommunicationUtils.bytesToInt(Arrays.copyOfRange(this.receiveBuffer, 4, 5)) + 2000;
                                int month;
                                if (MyApp.getPreferences().getString("device_model", "i5").contains("+")
                                        || MyApp.getPreferences().getString("device_model", "i5").contains("I7S2"))
                                    month = CommunicationUtils.bytesToInt(Arrays.copyOfRange(this.receiveBuffer, 5, 6));
                                else
                                    month = CommunicationUtils.bytesToInt(Arrays.copyOfRange(this.receiveBuffer, 5, 6)) + 1;
                                int day = CommunicationUtils.bytesToInt(Arrays.copyOfRange(this.receiveBuffer, 6, 7)) + 1;
                                int hour = CommunicationUtils.bytesToInt(Arrays.copyOfRange(this.receiveBuffer, 7, 8));
                                int minute = CommunicationUtils.bytesToInt(Arrays.copyOfRange(this.receiveBuffer, 8, 9));
                                int second = CommunicationUtils.bytesToInt(Arrays.copyOfRange(this.receiveBuffer, 9, 10));
                                long timestamp = new GregorianCalendar(year, month, day, hour, minute, second).getTimeInMillis();
                                Log.d(TAG, "DEVICE_DATE: " + new Date(timestamp).toString());
                                intent = new Intent(BroadcastConstants.ACTION_DATE_DATA);
                                intent.putExtra("data", timestamp);
                                MyApp.getmContext().sendBroadcast(intent);
                                break;
                            case Constants.APIv1_DATA_SUBSCRIBE_FOR_SPORT:
                                Sport dailySport = Sport.fromBytes(this.receiveBuffer, Sport.TYPE_DAILY_A);
                                Log.d(TAG, "DAILY_SPORT: " + dailySport);
                                intent = new Intent(BroadcastConstants.ACTION_SPORT_DATA);
                                intent.putExtra("data", dailySport);
                                MyApp.getmContext().sendBroadcast(intent);
                                break;
                            case Constants.APIv1_DATA_LOCAL_SPORT:
                                Sport localSport = Sport.fromBytes(this.receiveBuffer, Sport.TYPE_LOCAL_SPORT);
                                Log.d(TAG, "LOCAL_SPORT: " + localSport);
                                intent = new Intent(BroadcastConstants.ACTION_SPORT_DATA);
                                intent.putExtra("data", localSport);
                                MyApp.getmContext().sendBroadcast(intent);
                                break;
                            case Constants.APIv1_DATA_DAILY_SPORT2:
                                Sport dailySport2 = Sport.fromBytes(this.receiveBuffer, Sport.TYPE_DAILY_B);
                                Log.d(TAG, "DAILY_SPORT2: " + dailySport2);
                                intent = new Intent(BroadcastConstants.ACTION_SPORT_DATA);
                                intent.putExtra("data", dailySport2);
                                MyApp.getmContext().sendBroadcast(intent);
                                break;
                            case Constants.APIv1_DATA_SELFIE:
                                int code = CommunicationUtils.bytesToInt(Arrays.copyOfRange(this.receiveBuffer, 4, 5));
                                Log.d(TAG, "SELFIE_DATA: " + Integer.toString(code));
                                intent = new Intent((code == 1) ? BroadcastConstants.ACTION_SELFIE : BroadcastConstants.ACTION_PLAYPAUSE);
                                intent.putExtra("data", code);
                                MyApp.getmContext().sendBroadcast(intent);
                            default:
                                String buff = "";
                                Log.i(TAG, "MyReceiver unknown APIv1 command");
                                for (Byte b : this.receiveBuffer)
                                    buff += b + " ";
                                Log.i(TAG, "Buffer: " + buff);
                        }
                    }
                    // Clear buffer for new data
                    this.receiveBuffer = new byte[0];
                    return;
                }
                this.isDataOver = false;
            }
        }
    }

    @Override
    public void parserAPIv0(BluetoothGattCharacteristic chr) {
        String uuid = chr.getUuid().toString();
        Log.i(TAG, "Parse APIv0 data. UUID:" + uuid);
        Intent intent;
        Sport sp;
        switch (uuid) {
            case Constants.BAND_CHARACTERISTIC_SPORT:
                sp = Sport.fromCharacteristic(chr, false);
                intent = new Intent(BroadcastConstants.ACTION_SPORT_DATA);
                intent.putExtra("data", sp);
                MyApp.getmContext().sendBroadcast(intent);
                break;
            case Constants.BAND_CHARACTERISTIC_DAILY:
                sp = Sport.fromCharacteristic(chr, true);
                intent = new Intent(BroadcastConstants.ACTION_DAILY_DATA);
                intent.putExtra("data", sp);
                MyApp.getmContext().sendBroadcast(intent);
                break;
            case Constants.BAND_CHARACTERISTIC_DATE:
                long time = CommunicationUtils.parseDateCharacteristic(chr);
                intent = new Intent(BroadcastConstants.ACTION_DATE_DATA);
                intent.putExtra("data", time);
                MyApp.getmContext().sendBroadcast(intent);
                break;
            case Constants.BAND_CHARACTERISTIC_SEDENTARY:
                intent = new Intent(BroadcastConstants.ACTION_SEDENTARY_DATA);
                intent.putExtra("data", chr.getValue());
                MyApp.getmContext().sendBroadcast(intent);
                break;
            case Constants.BAND_CHARACTERISTIC_ALARM:
                intent = new Intent(BroadcastConstants.ACTION_ALARM_DATA);
                intent.putExtra("data", chr.getValue());
                MyApp.getmContext().sendBroadcast(intent);
                break;
            case Constants.BAND_CHARACTERISTIC_PAIR:
                intent = new Intent(BroadcastConstants.ACTION_PAIR_DATA);
                intent.putExtra("data", chr.getValue());
                MyApp.getmContext().sendBroadcast(intent);
                break;
        }
    }

    /**
     * Send data to device
     *
     * @param data array byte
     */
    @Override
    public void writePacket(byte[] data) {
        comm.WriteDataPacket(new Communication.WriteDataTask(UUID.fromString(Constants.BAND_CHARACTERISTIC_NEW_WRITE), data));
    }
}
