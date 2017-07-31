package com.mit.money.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


/**
 * Created by langduan on 16/3/23.
 */
public class RobService extends Service {

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

}
