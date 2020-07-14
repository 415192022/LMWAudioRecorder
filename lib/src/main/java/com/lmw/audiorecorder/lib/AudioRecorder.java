package com.lmw.audiorecorder.lib;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;

public class AudioRecorder {
    private static Application sApplication;

    public static void init(@NonNull final Context context) {
        AudioRecorder.sApplication = (Application) context.getApplicationContext();
    }


    public static Application getApp() {
        if (sApplication != null) return sApplication;
        throw new NullPointerException("u should init first");
    }

}
