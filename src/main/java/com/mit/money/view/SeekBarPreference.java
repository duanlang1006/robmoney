package com.mit.money.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.mit.money.R;
import com.mit.money.utils.SpUtil;

/**
 * Created by android on 2/25/16.
 */
public class SeekBarPreference extends DialogPreference {
    private SeekBar seekBar;
    private TextView textView;
    private String hintText, prefKind;

    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.preference_seekbar);

        for (int i = 0; i < attrs.getAttributeCount(); i++) {
            String attr = attrs.getAttributeName(i);
            if (attr.equalsIgnoreCase("pref_kind")) {
                prefKind = attrs.getAttributeValue(i);
                break;
            }
        }

        if (prefKind.equals(SpUtil.KEY_OPEN_DELAY)) {
            hintText = "拆开红包";
        } else if (prefKind.equals(SpUtil.KEY_REPLY_DELAY)) {
            hintText = "发送回复";
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            SharedPreferences.Editor editor = getEditor();
            editor.putInt(prefKind, this.seekBar.getProgress());
            editor.commit();
        }
        super.onDialogClosed(positiveResult);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        SharedPreferences pref = getSharedPreferences();

        int delay = pref.getInt(prefKind, 0);

        this.textView = (TextView) view.findViewById(R.id.pref_seekbar_textview);
        if (delay == 0) {
            this.textView.setText("立即" + hintText);
        } else {
            this.textView.setText("延迟" + delay + "秒" + hintText);
        }

        /* 暂不支持延时答谢 */
        if (prefKind.equals("pref_comment_delay")) {
            this.seekBar.setEnabled(false);
        }

        this.seekBar = (SeekBar) view.findViewById(R.id.delay_seekBar);
        this.seekBar.setProgress(delay);
        this.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    textView.setText("立即" + hintText);
                } else {
                    textView.setText("延迟" + progress + "秒" + hintText);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


}
