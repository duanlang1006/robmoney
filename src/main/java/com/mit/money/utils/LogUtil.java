package com.mit.money.utils;

import android.util.Log;

/**
 * Created by android on 2/25/16.
 */
public class LogUtil {
    public static boolean isDebug = true;
    public static final String LOG_TAG = "RobMoney";

    private LogUtil() {
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    public static void i(String msg) {
        if (isDebug) {
            Log.i(LOG_TAG, msg);
        }
    }

    public static void d(String msg) {
        if (isDebug) {
            Log.d(LOG_TAG, msg);
        }
    }

    public static void e(String msg) {
        if (isDebug) {
            Log.e(LOG_TAG, msg);
        }
    }

    public static void v(String msg) {
        if (isDebug) {
            Log.v(LOG_TAG, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (isDebug) {
            Log.i(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (isDebug) {
            Log.d(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (isDebug) {
            Log.e(tag, msg);
        }
    }

    public static void v(String tag, String msg) {
        if (isDebug) {
            Log.v(tag, msg);
        }
    }

}
