package com.lmw.audiorecorder.lib.listener;

public interface IRecordStatusListener {

    void recording();
    void stop(String filePath);
    void pause(String filePath);
    void resume();
    void error(Exception e);
}
