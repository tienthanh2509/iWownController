/*
 * Copyright (c) 2017 PT Studio all rights reserved.
 * Licensed under MIT
 */

package tk.d13ht01.bracelet.model;

import android.bluetooth.BluetoothGattCharacteristic;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;

import tk.d13ht01.bracelet.utils.CommunicationUtils;

/**
 * Created by Dmitry on 30.08.2015.
 */
public class Sport implements Serializable {
    public static final int TYPE_LOCAL_SPORT = 0;
    public static final int TYPE_DAILY_A = 1;
    public static final int TYPE_DAILY_B = 2;
    private int bcc = 0;
    private int minute = 0;
    private int hour = 0;
    private int day = 0;
    private int month = 0;
    private int year = 0;
    private int flag = 0;
    private int steps = 0;
    private float distance = 0;
    private float calorie = 0;
    private int type = 0;

    public static Sport fromBytes(byte[] data, int type) {
        try {
            Sport sport = new Sport();
            if (type != TYPE_LOCAL_SPORT) {
                sport.setBcc(CommunicationUtils.bytesToInt(Arrays.copyOfRange(data, 4, 5)));
                sport.setYear(CommunicationUtils.bytesToInt(Arrays.copyOfRange(data, 7, 8)));
                sport.setMonth(CommunicationUtils.bytesToInt(Arrays.copyOfRange(data, 6, 7)));
                sport.setDay(CommunicationUtils.bytesToInt(Arrays.copyOfRange(data, 5, 6)));
                sport.setSteps(CommunicationUtils.bytesToInt(Arrays.copyOfRange(data, 8, 12)));
                sport.setDistance(((float) CommunicationUtils.bytesToInt(Arrays.copyOfRange(data, 12, 16))) * 0.1f);
                sport.setCalorie(((float) CommunicationUtils.bytesToInt(Arrays.copyOfRange(data, 16, 20))) * 0.1f);
                sport.setType(type);
            } else {
                sport.setBcc(CommunicationUtils.bytesToInt(Arrays.copyOfRange(data, 4, 5)));
                sport.setMinute(CommunicationUtils.bytesToInt(Arrays.copyOfRange(data, 5, 6)));
                sport.setHour(CommunicationUtils.bytesToInt(Arrays.copyOfRange(data, 6, 7)));
                sport.setDay(CommunicationUtils.bytesToInt(Arrays.copyOfRange(data, 7, 8)) + 1);
                sport.setMonth(CommunicationUtils.bytesToInt(Arrays.copyOfRange(data, 8, 9)) + 1);
                sport.setYear(CommunicationUtils.bytesToInt(Arrays.copyOfRange(data, 9, 10)) + 2000);
                sport.setFlag(CommunicationUtils.bytesToInt(Arrays.copyOfRange(data, 10, 11)));
                sport.setSteps(CommunicationUtils.bytesToInt(Arrays.copyOfRange(data, 11, 13)));
                sport.setDistance(((float) CommunicationUtils.bytesToInt(Arrays.copyOfRange(data, 13, 15))) * 0.1f);
                sport.setCalorie(((float) CommunicationUtils.bytesToInt(Arrays.copyOfRange(data, 15, 17))) * 0.1f);
                sport.setType(TYPE_LOCAL_SPORT);
            }
            return sport;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Create new Sport model from Characteristic
     *
     * @param chr Characteristic
     * @return
     */
    public static Sport fromCharacteristic(BluetoothGattCharacteristic chr, boolean daily) {
        Sport sp = new Sport();
        if (!daily) {
            sp.setBcc(chr.getIntValue(17, 0));
            sp.setMinute(chr.getIntValue(17, 1));
            sp.setHour(chr.getIntValue(17, 2));
            sp.setDay(chr.getIntValue(17, 3) + 1);
            sp.setMonth(chr.getIntValue(17, 4) + 1);
            sp.setYear(chr.getIntValue(17, 5) + 2000);
            sp.setFlag(chr.getIntValue(17, 6));
            sp.setSteps(chr.getIntValue(18, 7));
            sp.setDistance(((float) chr.getIntValue(18, 9)) * 0.1f);
            sp.setCalorie(((float) chr.getIntValue(17, 11)) * 0.1f);
            sp.setType(TYPE_LOCAL_SPORT);
        } else {
            if (chr.getValue().length == 12) {
                sp.setSteps(chr.getIntValue(20, 0));
                sp.setDistance(((float) chr.getIntValue(20, 4)) * 0.1f);
                sp.setCalorie(((float) chr.getIntValue(10, 8)) * 0.1f);
                sp.setType(TYPE_DAILY_A);
            } else {
                sp.setBcc(chr.getIntValue(17, 0));
                sp.setDay(chr.getIntValue(17, 1) + 1);
                sp.setMonth(chr.getIntValue(17, 2) + 1);
                sp.setYear(chr.getIntValue(17, 3) + 2000);
                sp.setSteps(chr.getIntValue(36, 4));
                sp.setDistance(((float) chr.getIntValue(20, 8)) * 0.1f);
                sp.setCalorie(((float) chr.getIntValue(20, 12)) * 0.1f);
                sp.setType(TYPE_DAILY_B);
            }
        }
        return sp;
    }

    /**
     * Return timestamp for this sport point
     *
     * @return
     */
    public long getTimestamp() {
        if (this.year == 0)
            return new GregorianCalendar().getTimeInMillis();
        long tmp = new GregorianCalendar(this.year, this.month, this.day, this.hour, this.minute).getTimeInMillis();
        if (tmp <= 0)
            return new Date().getTime();
        return tmp;
    }

    public int getBcc() {
        return this.bcc;
    }

    public void setBcc(int value) {
        this.bcc = value;
    }

    public int getMinute() {
        return this.minute;
    }

    public void setMinute(int value) {
        this.minute = value;
    }

    public int getHour() {
        return this.hour;
    }

    public void setHour(int value) {
        this.hour = value;
    }

    public int getDay() {
        return this.day;
    }

    public void setDay(int value) {
        this.day = value;
    }

    public int getMonth() {
        return this.month;
    }

    public void setMonth(int value) {
        this.month = value;
    }

    public int getYear() {
        return this.year;
    }

    public void setYear(int value) {
        this.year = value;
    }

    public int getFlag() {
        return this.flag;
    }

    public void setFlag(int value) {
        this.flag = value;
    }

    public int getSteps() {
        return this.steps;
    }

    public void setSteps(int value) {
        this.steps = value;
    }

    public float getDistance() {
        return this.distance;
    }

    public void setDistance(float value) {
        this.distance = value;
    }

    public float getCalorie() {
        return this.calorie;
    }

    public void setCalorie(float value) {
        this.calorie = value;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int value) {
        this.type = value;
    }

    @Override
    public String toString() {
        return "Sport{" +
                "bcc=" + bcc +
                ", minute=" + minute +
                ", hour=" + hour +
                ", day=" + day +
                ", month=" + month +
                ", year=" + year +
                ", flag=" + flag +
                ", steps=" + steps +
                ", distance=" + distance +
                ", calorie=" + calorie +
                ", type=" + type +
                '}';
    }
}