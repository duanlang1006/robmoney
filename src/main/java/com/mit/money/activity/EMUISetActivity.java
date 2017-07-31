package com.mit.money.activity;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mit.mitanalytics.UmengAgentActivity;
import com.mit.money.R;

/**
 * Created by android on 3/4/16.
 */
public class EMUISetActivity extends UmengAgentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guide_set_device_layout);
        initVeiws();
        handleMaterialStatusBar();
    }

    private void initVeiws() {
//        TextView guideTitle = (TextView) findViewById(R.id.guide_title);
//        guideTitle.setText(getString(R.string.emui_setting_title));
//        guideTitle.setTextColor(Color.WHITE);
        TextView guideMessage = (TextView) findViewById(R.id.device_menu_set);
        guideMessage.setText(getString(R.string.emui_setting_message));
        ImageView guideImage = (ImageView) findViewById(R.id.guide_image);
        guideImage.setImageResource(R.drawable.tips_emui);

        Button guideSet = (Button) findViewById(R.id.guide_set);
        guideSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                ComponentName cn = new ComponentName("com.huawei.systemmanager",
                        "com.huawei.systemmanager.optimize.process.ProtectActivity");
                intent.setComponent(cn);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        Button guideNext = (Button) findViewById(R.id.guide_next);
        guideNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EMUISetActivity.this, AppMessageSetting.class);
                startActivity(intent);
                EMUISetActivity.this.finish();
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void handleMaterialStatusBar() {
        // Not supported in APK level lower than 21
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;

        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(0xffd84e43);
    }

}
