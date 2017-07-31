package com.mit.money.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mit.mitanalytics.UmengAgentActivity;
import com.mit.money.R;
import com.mit.money.utils.StatesUtil;

/**
 * Created by android on 3/4/16.
 */
public class NotificationSetting extends UmengAgentActivity {
    private TextView guideText;
    private StatesUtil statesUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guide_layout);
        statesUtil = new StatesUtil(this);
        initVeiws();
    }

    private void initVeiws() {
        guideText = (TextView) findViewById(R.id.guide_title);
        ImageView guideImage = (ImageView) findViewById(R.id.guide_image);
        guideImage.setImageResource(R.drawable.tips2);

        Button guideSet = (Button) findViewById(R.id.guide_set);
        guideSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                startActivity(intent);
            }
        });

        Button guideNext = (Button) findViewById(R.id.guide_next);
        guideNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NotificationSetting.this, AppMessageSetting.class);
                startActivity(intent);
                NotificationSetting.this.finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateTitle();
    }

    private void updateTitle() {
        if (statesUtil.getServiceStatus()) {
            guideText.setText("接收通知已经打开");
            guideText.setTextColor(Color.WHITE);
        } else {
            guideText.setText("接收通知未打开");
            guideText.setTextColor(Color.RED);
        }
    }

}
