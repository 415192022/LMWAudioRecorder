package com.lmw.audiorecorder.lib.listener;

public interface IRecorderFinishListener {
    void onRecorderFinished(long time, String filePath);
}
