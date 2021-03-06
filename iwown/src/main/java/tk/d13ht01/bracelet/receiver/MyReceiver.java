/*
 * Copyright (c) 2017 PT Studio. All rights reserved.
 * Licensed under Apache License 2.0 (https://github.com/tienthanh2509/iWownController/blob/master/LICENSE)
 */

package tk.d13ht01.bracelet.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;

import java.util.Objects;

import tk.d13ht01.bracelet.MyApp;
import tk.d13ht01.bracelet.common.BroadcastConstants;
import tk.d13ht01.bracelet.model.Notification;
import tk.d13ht01.bracelet.model.Sport;
import tk.d13ht01.bracelet.service.impl.BleServiceImpl;
import tk.d13ht01.bracelet.service.GoogleFitConnector;

/**
 * Created by Aloyan Dmitry on 30.08.2015
 */
public class MyReceiver extends BroadcastReceiver {
    private static boolean isIncomingCallMuted = false;
    private static boolean hasIncomingCall = false;
    private static MediaPlayer player = new MediaPlayer();
    private static int lastVolume;

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (!Objects.equals(action, "android.intent.action.BOOT_COMPLETED") &&
                (BleServiceImpl.getInstance() == null || BleServiceImpl.getInstance().getDevice() == null))
            return;

        switch (action) {
            case "android.intent.action.BOOT_COMPLETED":
                // MyApp should init
                break;
            case BroadcastConstants.ACTION_NEW_NOTIFICATION_RECEIVED:
                Notification nf = (Notification) intent.getSerializableExtra("data");
                String message = nf.getFromName() + ": " + nf.getMsgText();
                BleServiceImpl.getInstance().getDevice().sendAlert(message, nf.getDeviceNoticeType());
                break;
            case BroadcastConstants.ACTION_INCOMING_CALL:
                hasIncomingCall = true;
                stopLocatior(context); // If we receive call and locate the phone, need end locator

                if (MyApp.getPreferences().getBoolean("cbx_action_mute_onclick", false))
                    BleServiceImpl.getInstance().getDevice().setSelfieMode(true); // Set one click mode for mute

                String callid = intent.getStringExtra("data");
                BleServiceImpl.getInstance().getDevice().sendCall(callid); // Show call in device
                break;
            case BroadcastConstants.ACTION_END_CALL:
                hasIncomingCall = false;
                BleServiceImpl.getInstance().getDevice().sendCallEnd();
                if (isIncomingCallMuted) { // If we mute call, we need restore it
                    ((AudioManager) context.getSystemService(Context.AUDIO_SERVICE)).setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    BleServiceImpl.getInstance().getDevice().setSelfieMode(false);
                }
                break;
            case BroadcastConstants.ACTION_SELFIE:
                // If one click and we have ringing, need to mute
                if (hasIncomingCall && MyApp.getPreferences().getBoolean("cbx_action_mute_onclick", false)) {
                    AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                    am.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                    CallReceiver.rejectCall(context, 1);
                    isIncomingCallMuted = true;
                    BleServiceImpl.getInstance().getDevice().setSelfieMode(false);
                    return;
                }
                stopLocatior(context);
                BleServiceImpl.getInstance().getDevice().setSelfieMode(false);
                break;
            case BroadcastConstants.ACTION_PLAYPAUSE:
                // Call reject
                if (hasIncomingCall && MyApp.getPreferences().getBoolean("cbx_action_reject_on_long", false)) {
                    CallReceiver.rejectCall(context, 0);
                    return;
                }

                // Locator service
                if (!hasIncomingCall && MyApp.getPreferences().getBoolean("cbx_action_locator_on_long", false)) {
                    startLocator(context);
                    BleServiceImpl.getInstance().getDevice().setSelfieMode(true);
                    return;
                }
                break;

            case BroadcastConstants.ACTION_SPORT_DATA:
                Sport sport = (Sport) intent.getSerializableExtra("data");
                if (MyApp.getPreferences().getBoolean("fit_connected", false))
                    GoogleFitConnector.publish(sport);
                break;

            case BroadcastConstants.ACTION_CONNECT_TO_GFIT:
                if (MyApp.getPreferences().getBoolean("fit_connected", false))
                    if (BleServiceImpl.getInstance() != null && BleServiceImpl.getInstance().getDevice() != null)
                        BleServiceImpl.getInstance().getDevice().subscribeForSportUpdates();
                break;
            case BroadcastConstants.ACTION_GATT_CONNECTED:
                if (MyApp.getPreferences().getBoolean("fit_connected", false))
                    BleServiceImpl.getInstance().getDevice().subscribeForSportUpdates();
                break;
        }
    }

    private void startLocator(Context context) {
        try {
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            lastVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
            am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);

            AssetFileDescriptor afd = context.getAssets().openFd("beep_4_times.mp3");
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            player.setLooping(true);
            player.prepare();
            player.start();
            autoOffLocator();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopLocatior(Context context) {
        try {
            if (player.isPlaying()) {
                player.stop();
                player.release();

                AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                am.setStreamVolume(AudioManager.STREAM_MUSIC, lastVolume, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void autoOffLocator() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(120000); // 2 min
                    if (player.isPlaying()) {
                        player.stop();
                        player.release();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
