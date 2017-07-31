package com.mit.money.service;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

/**
 * Created by langduan on 16/3/23.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
@SuppressLint("OverrideAbstract")
public class RobNofitificationService extends NotificationListenerService {
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
//        Log.i("SevenNLS","Notification posted");
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
//        Log.i("SevenNLS","Notification removed");
    }
}
