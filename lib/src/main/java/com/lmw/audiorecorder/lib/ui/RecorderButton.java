package com.lmw.audiorecorder.lib.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.widget.AppCompatButton;

import com.lmw.audiorecorder.lib.MediaRecorder;
import com.lmw.audiorecorder.lib.IRecorder;
import com.lmw.audiorecorder.lib.R;
import com.lmw.audiorecorder.lib.listener.IRecordStatusListener;
import com.lmw.audiorecorder.lib.listener.IRecorderFinishListener;
import com.lmw.audiorecorder.lib.utils.PermissionConstants;
import com.lmw.audiorecorder.lib.utils.PermissionUtils;


public class RecorderButton extends AppCompatButton {
    /**
     * 状态相关
     */
    public static final int STATE_NORMAL = 1;
    public static final int STATE_RECORDING = 2;
    public static final int STATE_WANT_CANCEL = 3;
    /**
     * handler消息相关
     */
    private static final int MSG_AUDIO_PREPARE = 0x100;
    private static final int MSG_VOICE_CHANGE = 0x101;
    private static final int MSG_DIALOG_DISMISS = 0x102;
    private static final int MSG_COUNT_DOWN_DONE = 0x103;
    private static final int MSG_COUNTING = 0x104;
    //最大音量
    private static final int MAX_VOICE_LEVEL = 8;
    //当前状态
    public int CURRENT_STATE = STATE_NORMAL;
    private int maxDistance;
    //保存存储路径
    private String mFileDir = "";
    private boolean isRecording;
    private RecorderDialog mRecorderDialog;
    //保存录音时间
    private long mVoiceTime;
    //录音最长时间
    private double mMaxTime = 30;
    //录音最短时间
    private double mMinTime = 0.7;
    private IRecorderFinishListener mListener;
    private IRecorder mRecorder;
    private Runnable maxLevelRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                while (isRecording) {
                    Thread.sleep(30);
                    mHandler.sendEmptyMessage(MSG_VOICE_CHANGE);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };
    private Runnable maxTimeRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                while (isRecording) {
                    Thread.sleep(1000);
                    mVoiceTime = mVoiceTime + 1;
                    mHandler.sendEmptyMessage(MSG_COUNTING);

                    // 控制最大录制时间
                    if (mVoiceTime >= mMaxTime) {
                        mHandler.sendEmptyMessage(MSG_COUNT_DOWN_DONE);
                        return;
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_AUDIO_PREPARE:
                    changeState(STATE_RECORDING);
                    mRecorderDialog.show();
                    mRecorderDialog.recording();
                    mRecorderDialog.updateCount((int) mVoiceTime, (int) mMaxTime);
                    new Thread(maxLevelRunnable).start();
                    new Thread(maxTimeRunnable).start();
                case MSG_VOICE_CHANGE:
                    mRecorderDialog.updateVoiceLevel(mRecorder.getVoiceLevel(MAX_VOICE_LEVEL));
                    break;
                case MSG_DIALOG_DISMISS:
                    mRecorderDialog.closeDialog();
                    break;
                case MSG_COUNTING:
                    mRecorderDialog.updateCount((int) mVoiceTime, (int) mMaxTime);
                    break;
                case MSG_COUNT_DOWN_DONE:
                    mRecorderDialog.closeDialog();
                    mRecorder.stop();
                    reset();
                    break;
            }
            return false;
        }
    });

    public RecorderButton(Context context) {
        this(context, null);
    }

    public RecorderButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecorderButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(context, attrs);
        init(context);
    }

    private void initAttr(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.recorder_button);
        final int count = ta.getIndexCount();
        for (int i = 0; i < count; i++) {
            int attr = ta.getIndex(i);
            if (attr == R.styleable.recorder_button_max_time) {
                mMaxTime = ta.getFloat(attr, (float) mMaxTime);
            } else if (attr == R.styleable.recorder_button_min_time) {
                mMinTime = ta.getFloat(attr, (float) mMinTime);
            } else if (attr == R.styleable.recorder_button_max_distance) {
                maxDistance = ta.getInt(attr, 0);
            }
        }

        ta.recycle();
    }

    public void init(Context context) {
        initRecorder(context);
        initListener(context);
    }

    private void initRecorder(Context context) {
        mRecorder = new MediaRecorder
                .Builder()
                .setFileDir(mFileDir)
                .setRecordStatusListener(new IRecordStatusListener() {
                    @Override
                    public void recording() {
                        isRecording = true;
                        mHandler.sendEmptyMessage(MSG_AUDIO_PREPARE);
                    }

                    @Override
                    public void stop(String filePath) {
                        if (mListener != null)
                            mListener.onRecorderFinished(mVoiceTime, filePath);
                    }

                    @Override
                    public void pause(String filePath) {

                    }

                    @Override
                    public void resume() {

                    }

                    @Override
                    public void error(Exception e) {

                    }
                })
                .build();
    }

    /**
     * 初始化AudioDialog
     *
     * @param context
     */
    private void initListener(final Context context) {
        setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                try {
                    if (hasPermission()) {
                        mRecorder.start();
                    } else {
                        requestPermission();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
    }

    private boolean hasPermission() {
        String readStorage = "android.permission.READ_EXTERNAL_STORAGE";
        String writeStorage = "android.permission.WRITE_EXTERNAL_STORAGE";
        String recordAudio = "android.permission.RECORD_AUDIO";
        return PermissionUtils.isGranted(readStorage, writeStorage, recordAudio);
    }

    private void requestPermission() {
        PermissionUtils
                .permission(PermissionConstants.STORAGE, PermissionConstants.MICROPHONE)
                .callback(new PermissionUtils.SimpleCallback() {
                    @Override
                    public void onGranted() {
                    }

                    @Override
                    public void onDenied() {
                    }
                })
                .request();
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                getParent().requestDisallowInterceptTouchEvent(true);
                if (isRecording) {
                    if (wantCancel(x, y)) {
                        changeState(STATE_WANT_CANCEL);
                    } else {
                        changeState(STATE_RECORDING);
                    }
                }

                break;
            case MotionEvent.ACTION_UP:
                //录音还未就绪
                if (!isRecording) {
                    reset();
                    return super.onTouchEvent(event);
                }

                //录音时间太短
                if (!isRecording || mVoiceTime < mMinTime) {
                    isRecording = false;
                    mRecorderDialog.recordTooShort();
                    mRecorder.cancel();
                    mHandler.sendEmptyMessageDelayed(MSG_DIALOG_DISMISS, 500);
                } else if (CURRENT_STATE == STATE_RECORDING) {
                    //正常结束
                    isRecording = false;
                    mRecorderDialog.closeDialog();
                    mRecorder.stop();
                } else if (CURRENT_STATE == STATE_WANT_CANCEL) {
                    //取消
                    isRecording = false;
                    mRecorderDialog.closeDialog();
                    mRecorder.cancel();
                }
                reset();
                break;
            case MotionEvent.ACTION_CANCEL:
                reset();
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 重置
     */
    private void reset() {
        isRecording = false;
        mVoiceTime = 0;
        changeState(STATE_NORMAL);
    }

    @Override
    protected void onDetachedFromWindow() {
        reset();
        mHandler.removeCallbacksAndMessages(null);
        super.onDetachedFromWindow();
    }

    /**
     * 是否取消录音
     *
     * @return
     */
    private boolean wantCancel(int x, int y) {
        if (x < 0 || x > getWidth()) {
            return true;
        }

        return y < -maxDistance || y > getHeight() + maxDistance;
    }


    /**
     * 改变状态
     *
     * @param state
     */
    public void changeState(int state) {

    }

    public void setFileDir(String path) {
        this.mFileDir = path;
        this.mRecorder.setFileDir(path);
    }

    public RecorderDialog getRecorderDialog() {
        return mRecorderDialog;
    }

    public void setRecorderDialog(RecorderDialog mRecorderDialog) {
        this.mRecorderDialog = mRecorderDialog;
    }

    public void setRecorder(IRecorder recorder) {
        this.mRecorder = recorder;
    }

    public void setRecorderFinishListener(IRecorderFinishListener mListener) {
        this.mListener = mListener;
    }

}