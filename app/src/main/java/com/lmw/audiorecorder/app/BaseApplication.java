package com.lmw.audiorecorder.app;

import android.app.Application;

import com.lmw.audiorecorder.lib.AudioRecorder;

public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {
        AudioRecorder.init(this);
    }
}
