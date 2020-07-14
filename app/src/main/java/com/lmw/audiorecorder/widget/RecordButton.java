package com.lmw.audiorecorder.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.lmw.audiorecorder.lib.ui.RecorderButton;

public class RecordButton extends RecorderButton {
    public RecordButton(Context context) {
        this(context, null);
    }

    public RecordButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecordButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void changeState(int state) {
        super.changeState(state);
        if (CURRENT_STATE != state) {
            CURRENT_STATE = state;
            switch (state) {
                case STATE_NORMAL:
                    setText("按住说话");
                    break;
                case STATE_RECORDING:
                    setText("松开结束");
                    getRecorderDialog().recording();
                    break;
                case STATE_WANT_CANCEL:
                    setText("松开手指 取消发送");
                    getRecorderDialog().wantCancel();
                    break;
            }
        }

    }
}

