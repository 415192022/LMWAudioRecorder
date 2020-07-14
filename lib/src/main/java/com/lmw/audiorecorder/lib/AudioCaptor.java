package com.lmw.audiorecorder.lib;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import com.lmw.audiorecorder.lib.listener.IRecordStatusListener;
import com.lmw.audiorecorder.lib.utils.FileIOUtils;
import com.lmw.audiorecorder.lib.utils.FileUtils;
import com.lmw.audiorecorder.lib.utils.PermissionConstants;
import com.lmw.audiorecorder.lib.utils.PermissionUtils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class AudioCaptor implements IRecorder {
    private final String TAG = getClass().getSimpleName();

    private int mAudioFormat;//编码制式和采样大小
    private int mAudioSource;//音频源
    private int mSampleRate;//采样率
    private int mChannel;//单声道
    private AudioRecord mAudioRecord;
    private int mMinBufferSize = 0;//缓冲区的大小
    private Thread mCaptureThread;
    private String mFileDir;//文件夹路径
    private String mFilePath;//音频文件路径
    private boolean isAcousticEcho;//是否设置回声消除
    private boolean mIsCaptureStarted = false;
    private volatile boolean mIsLoopExit = false;
    private File mFile;

    private OnAudioFrameCapturedListener mAudioFrameCapturedListener;

    private AudioCaptor(Builder builder) {
        this.mAudioFormat = builder.audioFormat;//编码制式和采样大小
        this.mAudioSource = builder.audioFormat;//音频源
        this.mSampleRate = builder.audioFormat;//音频源
        this.mChannel = builder.audioFormat;//通道数
        this.isAcousticEcho = builder.isAcousticEcho;//是否设置回声消除
        setAcousticEcho();
    }

    /**
     * 设置回声消除
     */
    public void setAcousticEcho() {
        if (isAcousticEcho) {
            mChannel = AudioFormat.CHANNEL_IN_MONO;//单通道
            mAudioSource = MediaRecorder.AudioSource.VOICE_COMMUNICATION;//音频源
            AudioManager audioManager = (AudioManager) AudioRecorder.getApp().getSystemService(Context.AUDIO_SERVICE);
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            audioManager.setSpeakerphoneOn(true);
        } else {
            mChannel = AudioFormat.CHANNEL_IN_STEREO;//立体声
            mAudioSource = MediaRecorder.AudioSource.MIC;//音频源
        }
    }

    @Override
    public void start() throws IOException {
        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
            throw new IOException("please checkout your sdcard!!!");
        }

        PermissionUtils
                .permission(PermissionConstants.STORAGE)
                .callback(new PermissionUtils.SimpleCallback() {
                    @Override
                    public void onGranted() {
                        startCapture();
                    }

                    @Override
                    public void onDenied() {

                    }
                })
                .request();
    }

    @Override
    public void stop() {
        stopCapture();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void cancel() {
        stop();
        try {
            if (mFilePath != null) {
                FileUtils.deleteFile(mFilePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getVoiceLevel(int i) {

        try {
            if (mAudioRecord != null && mAudioRecord.getActiveMicrophones() != null && mAudioRecord.getActiveMicrophones().size() > 0) {
                return (int) (mAudioRecord.getActiveMicrophones().get(0).getMaxSpl() / 32768 + 1);
            }
        } catch (Exception e) {

        }

        return 1;
    }

    @Override
    public void setFileDir(String dir) {
        this.mFileDir = dir;
    }

    public boolean startCapture() {
        return startCapture(mAudioSource, mSampleRate, mChannel, mAudioFormat);
    }

    public boolean startCapture(int audioSource, int sampleRateInHz, int channelConfig, int audioFormat) {

        if (mIsCaptureStarted) {
            Log.e(TAG, "Capture already started !");
            return false;
        }

        //创建文件夹
        FileUtils.createOrExistsDir(mFileDir);
        //随机生成文件名
        String fileName = generateFileName();
        //创建文件
        mFile = new File(mFileDir, fileName);
        mMinBufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        if (mMinBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(TAG, "Invalid parameter !");
            return false;
        }
        Log.d(TAG, "getMinBufferSize = " + mMinBufferSize + " bytes !");

        mAudioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, mMinBufferSize);
        if (mAudioRecord.getState() == AudioRecord.STATE_UNINITIALIZED) {
            Log.e(TAG, "AudioRecord initialize fail !");
            return false;
        }

        mAudioRecord.startRecording();

        mIsLoopExit = false;
        mCaptureThread = new Thread(new AudioCaptureRunnable());
        mCaptureThread.start();

        mIsCaptureStarted = true;

        Log.d(TAG, "Start audio capture success !");

        return true;
    }

    public void stopCapture() {

        if (!mIsCaptureStarted) {
            return;
        }

        mIsLoopExit = true;
        try {
            mCaptureThread.interrupt();
            mCaptureThread.join(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
            mAudioRecord.stop();
        }

        mAudioRecord.release();

        mIsCaptureStarted = false;

        if (mAudioFrameCapturedListener != null) {
            mAudioFrameCapturedListener.onAudioFrameClosed();
        }
        mAudioFrameCapturedListener = null;

        Log.d(TAG, "Stop audio capture success !");
    }

    /**
     * 随机生成文件名
     *
     * @return
     */
    private String generateFileName() {
        return UUID.randomUUID().toString() + ".pcm";
    }


    public interface OnAudioFrameCapturedListener {
        void onAudioFrameCaptured(byte[] audioData);

        void onAudioFrameClosed();
    }

    public static final class Builder {
        private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;//编码制式和采样大小
        private int audioSource = MediaRecorder.AudioSource.MIC;//音频源
        private int sampleRate = 44100;//音频源
        private int channel = AudioFormat.CHANNEL_IN_MONO;//通道数
        private boolean isAcousticEcho;//通道数
        private IRecordStatusListener listener;

        public Builder setAudioFormat(int audioFormat) {
            this.audioFormat = audioFormat;
            return this;
        }

        public Builder setAudioSource(int audioSource) {
            this.audioSource = audioSource;
            return this;
        }

        public Builder setSampleRate(int sampleRate) {
            this.sampleRate = sampleRate;
            return this;
        }

        public Builder setChannel(int channel) {
            this.channel = channel;
            return this;
        }

        public void setAcousticEcho(boolean acousticEcho) {
            isAcousticEcho = acousticEcho;
        }

        public Builder setListener(IRecordStatusListener listener) {
            this.listener = listener;
            return this;
        }

        public AudioCaptor build() {
            return new AudioCaptor(this);
        }
    }

    private class AudioCaptureRunnable implements Runnable {

        @Override
        public void run() {

            while (!mIsLoopExit) {

                byte[] buffer = new byte[mMinBufferSize];

                int ret = mAudioRecord.read(buffer, 0, mMinBufferSize);
                if (ret == AudioRecord.ERROR_INVALID_OPERATION) {
                    Log.e(TAG, "Error ERROR_INVALID_OPERATION");
                } else if (ret == AudioRecord.ERROR_BAD_VALUE) {
                    Log.e(TAG, "Error ERROR_BAD_VALUE");
                } else {
                    if (mAudioFrameCapturedListener != null) {
                        mAudioFrameCapturedListener.onAudioFrameCaptured(buffer);
                    }
                    Log.d(TAG, "OK, Captured " + ret + " bytes !");
                }

                FileIOUtils.writeFileFromBytesByStream(mFile, buffer, true);
                SystemClock.sleep(10);
            }
        }
    }
}