package com.lmw.audiorecorder.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lmw.audiorecorder.R;
import com.lmw.audiorecorder.lib.ui.RecorderDialog;
import com.lmw.audiorecorder.utils.TimeUtils;


public class RecordDialog extends RecorderDialog {

    private RelativeLayout rlParent;

    private LinearLayout llParent;

    private ImageView ivCancel;

    private ImageView ivItem;

    private TextView tvCount;

    private TextView tvTips;

    private TextView tvItem;

    public RecordDialog(Context context) {
        this(context, 0);
    }

    public RecordDialog(Context context, int themeResId) {
        super(context, themeResId);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflate = LayoutInflater.from(context);
        View view = inflate.inflate(R.layout.dialog_recorder, null);
        setContentView(view);

        rlParent = findViewById(R.id.rlParent);
        llParent = findViewById(R.id.llParent);
        ivCancel = findViewById(R.id.ivCancel);
        ivItem = findViewById(R.id.ivItem);
        tvCount = findViewById(R.id.tvCount);
        tvTips = findViewById(R.id.tvTips);
        tvItem = findViewById(R.id.tvItem);

    }

    @Override
    public void closeDialog() {
        if (isShowing()) {
            dismiss();
        }
    }

    @Override
    public void updateCount(int voiceTime, int maxTime) {
        if (isShowing() && llParent.getVisibility() == View.VISIBLE) {
            rlParent.setVisibility(View.GONE);
            llParent.setVisibility(View.VISIBLE);
            //倒计时展示
            tvCount.setText(TimeUtils.secToTime((maxTime - voiceTime)));
        }

    }

    @Override
    public void updateVoiceLevel(int voiceLevel) {
        if (isShowing() && llParent.getVisibility() == View.VISIBLE) {
            switch (voiceLevel) {
                case 1:
                    ivItem.setImageResource(R.drawable.icon_store_im_volume01);
                    break;
                case 2:
                    ivItem.setImageResource(R.drawable.icon_store_im_volume02);
                    break;
                case 3:
                    ivItem.setImageResource(R.drawable.icon_store_im_volume03);
                    break;
                case 4:
                    ivItem.setImageResource(R.drawable.icon_store_im_volume04);
                    break;
                case 5:
                    ivItem.setImageResource(R.drawable.icon_store_im_volume05);
                    break;
                case 6:
                    ivItem.setImageResource(R.drawable.icon_store_im_volume06);
                    break;
                case 7:
                    ivItem.setImageResource(R.drawable.icon_store_im_volume07);
                    break;
            }
        }
    }

    @Override
    public void wantCancel() {
        if (isShowing()) {
            rlParent.setVisibility(View.VISIBLE);
            llParent.setVisibility(View.GONE);
        }

    }

    @Override
    public void recording() {
        if (isShowing()) {
            rlParent.setVisibility(View.GONE);
            llParent.setVisibility(View.VISIBLE);
            tvTips.setText("手指上滑，取消发送");
        }


    }

    @Override
    public void recordTooShort() {
        if (isShowing()) {
            rlParent.setVisibility(View.GONE);
            llParent.setVisibility(View.VISIBLE);
            tvTips.setText("录制时间过短");
        }

    }

}