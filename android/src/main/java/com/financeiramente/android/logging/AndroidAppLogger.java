package com.financeiramente.android.logging;

import android.util.Log;

import com.financeiramente.core.db.AppLogger;

public class AndroidAppLogger implements AppLogger {

    private final String tag;

    public AndroidAppLogger(String tag) {
        this.tag = tag;
    }

    @Override
    public void info(String message) {
        Log.i(tag, message);
    }

    @Override
    public void warn(String message) {
        Log.w(tag, message);
    }

    @Override
    public void error(String message) {
        Log.e(tag, message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        Log.e(tag, message, throwable);
    }
}