package com.lmw.audiorecorder.lib.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

public abstract class RecorderDialog extends Dialog {
    public RecorderDialog(Context context) {
        super(context);
    }

    public RecorderDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    protected RecorderDialog(Context context, boolean cancelable, DialogInterface.OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }


    public abstract void closeDialog();

    public abstract void updateCount(int voiceTime, int maxTime);

    public abstract void updateVoiceLevel(int voiceLevel);

    public abstract void wantCancel();

    public abstract void recording();

    public abstract void recordTooShort();

}
