package com.lmw.audiorecorder.lib;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Environment;


import com.lmw.audiorecorder.lib.listener.IRecordStatusListener;
import com.lmw.audiorecorder.lib.utils.FileUtils;
import com.lmw.audiorecorder.lib.utils.PermissionConstants;
import com.lmw.audiorecorder.lib.utils.PermissionUtils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class MediaRecorder implements IRecorder {
    private android.media.MediaRecorder mMediaRecorder;
    private String mFileDir;//文件夹路径
    private String mFilePath;//音频文件路径
    private String mFileSuffix;//文件后缀
    private IRecordStatusListener mRecordStatusListener;

    private MediaRecorder(Builder builder) {
        mFileDir = builder.fileDir;
        mFileSuffix = builder.fileSuffix;
        mRecordStatusListener = builder.listener;
    }

    /**
     * 开始
     *
     * @throws IOException
     */
    @Override
    public void start() throws IOException {
        PermissionUtils
                .permission(PermissionConstants.STORAGE, PermissionConstants.MICROPHONE)
                .callback(new PermissionUtils.SimpleCallback() {
                    @Override
                    public void onGranted() {
                        starting();
                    }

                    @Override
                    public void onDenied() {

                    }
                })
                .request();
    }

    /**
     * 正式开始
     */
    private void starting() {
        try {
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                throw new IOException("please checkout your sdcard!!!");
            }

            //创建文件夹
            FileUtils.createOrExistsDir(mFileDir);
            //随机生成文件名
            String fileName = generateFileName();
            //创建文件
            File file = new File(mFileDir, fileName);
            mFilePath = file.getAbsolutePath();
            mMediaRecorder = new android.media.MediaRecorder();
            //设置输入文件
            mMediaRecorder.setOutputFile(file.getAbsolutePath());
            //设置音频源为麦克风
            mMediaRecorder.setAudioSource(android.media.MediaRecorder.AudioSource.MIC);
            //设置所有android系统都支持的采样频率
            mMediaRecorder.setAudioSamplingRate(44100);
            //设置比较好的音质
            mMediaRecorder.setAudioEncodingBitRate(96000);
            //设置输出格式
            mMediaRecorder.setOutputFormat(android.media.MediaRecorder.OutputFormat.AAC_ADTS);
            //设置音频编码
            mMediaRecorder.setAudioEncoder(android.media.MediaRecorder.AudioEncoder.AAC);
            mMediaRecorder.prepare();
            mMediaRecorder.start();

            if (mRecordStatusListener != null)
                mRecordStatusListener.recording();
        } catch (IllegalStateException e1) {
            if (mMediaRecorder != null) {
                mMediaRecorder.reset();
            }
            if (mRecordStatusListener != null)
                mRecordStatusListener.error(e1);
            e1.printStackTrace();
        } catch (IOException e2) {
            if (mMediaRecorder != null) {
                mMediaRecorder.reset();
            }
            if (mRecordStatusListener != null)
                mRecordStatusListener.error(e2);
            e2.printStackTrace();
        }
    }

    /**
     * 停止
     */
    @Override
    public void stop() {
        if (mMediaRecorder != null) {
            try {
                mMediaRecorder.stop();
                mMediaRecorder.release();
                mMediaRecorder = null;
                if (mRecordStatusListener != null)
                    mRecordStatusListener.stop(mFilePath);
            } catch (Exception e) {
                if (mMediaRecorder != null) {
                    mMediaRecorder.reset();
                }
                if (mRecordStatusListener != null)
                    mRecordStatusListener.error(e);
                e.printStackTrace();
            }
        }
    }

    /**
     * 暂停
     * sdk24以上才支持
     */
    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public void pause() {
        if (mMediaRecorder != null) {
            try {
                mMediaRecorder.pause();
                if (mRecordStatusListener != null)
                    mRecordStatusListener.pause(mFilePath);
            } catch (Exception e) {
                if (mMediaRecorder != null) {
                    mMediaRecorder.reset();
                }
                if (mRecordStatusListener != null)
                    mRecordStatusListener.error(e);
                e.printStackTrace();
            }
        }
    }

    /**
     * 重新开始
     */
    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public void resume() {
        if (mMediaRecorder != null) {
            try {
                mMediaRecorder.resume();
                if (mRecordStatusListener != null)
                    mRecordStatusListener.resume();
            } catch (Exception e) {
                if (mRecordStatusListener != null)
                    mRecordStatusListener.error(e);
                e.printStackTrace();
            } finally {
                if (mMediaRecorder != null) {
                    mMediaRecorder.reset();
                }
            }
        }
    }

    /**
     * 随机生成文件名
     *
     * @return
     */
    private String generateFileName() {
        return UUID.randomUUID().toString() + "." + mFileSuffix;
    }


    /**
     * 取消录音
     */
    public void cancel() {
        if (mMediaRecorder != null) {
            try {
                mMediaRecorder.stop();
                mMediaRecorder.release();
                mMediaRecorder = null;
                if (mFilePath != null) {
                    FileUtils.deleteFile(mFilePath);
                }
            } catch (Exception e) {
                if (mMediaRecorder != null) {
                    mMediaRecorder.reset();
                }
                if (mRecordStatusListener != null)
                    mRecordStatusListener.error(e);
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getVoiceLevel(int maxLevel) {

        if (mMediaRecorder != null) {
            try {
                //getMaxAmplitude()获取振幅
                return maxLevel * mMediaRecorder.getMaxAmplitude() / 32768 + 1;
            } catch (Exception e) {
            }
        }

        return 1;
    }

    @Override
    public void setFileDir(String dir) {
        this.mFileDir = dir;
    }

    public static final class Builder {
        private String fileDir;//文件路径
        private String fileSuffix = "aac";//文件后缀
        private IRecordStatusListener listener;

        public Builder setFileDir(String fileDir) {
            this.fileDir = fileDir;
            return this;
        }

        public Builder setFileSuffix(String fileSuffix) {
            this.fileSuffix = fileSuffix;
            return this;
        }

        public Builder setRecordStatusListener(IRecordStatusListener listener) {
            this.listener = listener;
            return this;
        }

        public MediaRecorder build() {
            return new MediaRecorder(this);
        }
    }

}