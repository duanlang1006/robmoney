package com.mit.money.utils;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.PowerManager;

/**
 * Created by android on 2/25/16.
 */
public class PowerUtil {
    private Context mContext;
    private PowerManager.WakeLock wakeLock;
    private KeyguardManager.KeyguardLock keyguardLock;

    private static PowerUtil sInstance;

    public static PowerUtil getInstance(Context context) {
        if (null == sInstance){
            sInstance = new PowerUtil(context);
        }
        return sInstance;
    }

    public PowerUtil(Context context) {
        mContext = context;
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        keyguardLock = keyguardManager.newKeyguardLock(context.getPackageName());
    }

    private PowerManager.WakeLock getAlarmWakeLock() {
        if (wakeLock == null) {
            PowerManager powerManager = (PowerManager) mContext.getSystemService(
                    Context.POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(
                    PowerManager.ACQUIRE_CAUSES_WAKEUP
                            | PowerManager.ON_AFTER_RELEASE
                            | PowerManager.FULL_WAKE_LOCK, mContext.getPackageName());
            wakeLock.setReferenceCounted(true);
        }
        return wakeLock;
    }

    private void acquireAlarmWakeLock() {
        getAlarmWakeLock().acquire();
    }

    private void releaseAlarmWakeLock() {
        if (wakeLock.isHeld()) {
            getAlarmWakeLock().release();
        }
    }

    public void disableKeyguard() {
        if (!getAlarmWakeLock().isHeld()) {
            acquireAlarmWakeLock();
            keyguardLock.disableKeyguard();
        }
    }

    public void reenableKeyguard() {
        if (getAlarmWakeLock().isHeld()) {
            releaseAlarmWakeLock();
            keyguardLock.reenableKeyguard();
        }
    }

}
