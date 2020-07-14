package com.lmw.audiorecorder.lib;

import java.io.IOException;

public interface IRecorder {
    void start() throws IOException;

    void stop();

    void pause();

    void resume();

    void cancel();

    int getVoiceLevel(int i);

    void setFileDir(String path);
}
