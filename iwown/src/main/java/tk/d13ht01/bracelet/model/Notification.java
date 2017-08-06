/*
 * Copyright (c) 2017 PT Studio all rights reserved.
 * Licensed under MIT
 */

package tk.d13ht01.bracelet.model;

import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.io.Serializable;

import tk.d13ht01.bracelet.common.Constants;

/**
 * Created by ptthanh on 8/1/2017.
 */
public class Notification implements Serializable {
    private String fromName = "";
    private String msgText = "";
    private Integer noticeType = 0;

    public static Notification fromSbn(StatusBarNotification sbn, int notice_type) {
        Notification nf = new Notification();
        //String ticker = "";

        if (sbn == null || sbn.getPackageName() == null)
            return nf;

        Log.i("Package", sbn.getPackageName());

        if (sbn.getNotification() == null)
            return nf;

//            if( sbn.getNotification().tickerText != null ) {
//                ticker = sbn.getNotification().tickerText.toString();
//            }
        if (sbn.getNotification().extras != null) {
            Bundle extras = sbn.getNotification().extras;
            if (extras.containsKey(android.app.Notification.EXTRA_TITLE))
                nf.fromName = extras.get(android.app.Notification.EXTRA_TITLE).toString();
            if (extras.containsKey("android.text"))
                nf.msgText = extras.get("android.text").toString();
        }

//            Log.i("Ticker", ticker);
        if (nf.fromName != null)
            Log.i("Title", nf.fromName);
//            if( nf.msgText != null )
//                Log.i("Text", nf.msgText);
        nf.noticeType = notice_type;

        return nf;
    }

    public Integer getDeviceNoticeType() {
        switch (this.noticeType) {
            case 1:
                return Constants.ALERT_TYPE_MESSAGE;
            case 2:
                return Constants.ALERT_TYPE_CLOUD;
            case 3:
                return Constants.ALERT_TYPE_ERROR;
            default:
                return Constants.ALERT_TYPE_MESSAGE;
        }
    }

    public String getFromName() {
        return this.fromName;
    }

    public String getMsgText() {
        return this.msgText;
    }

}
