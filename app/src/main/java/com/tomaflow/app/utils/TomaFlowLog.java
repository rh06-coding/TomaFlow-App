package com.tomaflow.app.utils;

import android.util.Log;

import com.tomaflow.app.BuildConfig;

/**
 * Thin wrapper around {@link android.util.Log} that no-ops debug/verbose/info logs
 * in release builds so we don't leak internal details or clutter production logs.
 * Warnings and errors are always logged.
 *
 * <p>Use {@code TomaFlowLog.d(TAG, msg)} in place of {@code Log.d(TAG, msg)}.
 * Callers must not pass PII or secrets as the message.</p>
 */
public final class TomaFlowLog {

    private TomaFlowLog() {}

    public static void d(String tag, String msg) {
        if (BuildConfig.DEBUG) Log.d(tag, msg);
    }

    public static void d(String tag, String msg, Throwable tr) {
        if (BuildConfig.DEBUG) Log.d(tag, msg, tr);
    }

    public static void v(String tag, String msg) {
        if (BuildConfig.DEBUG) Log.v(tag, msg);
    }

    public static void i(String tag, String msg) {
        if (BuildConfig.DEBUG) Log.i(tag, msg);
    }

    public static void w(String tag, String msg) {
        Log.w(tag, msg);
    }

    public static void w(String tag, String msg, Throwable tr) {
        Log.w(tag, msg, tr);
    }

    public static void e(String tag, String msg) {
        Log.e(tag, msg);
    }

    public static void e(String tag, String msg, Throwable tr) {
        Log.e(tag, msg, tr);
    }
}
