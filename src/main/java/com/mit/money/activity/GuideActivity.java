package com.mit.money.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mit.mitanalytics.UmengAgentActivity;
import com.mit.money.R;
import com.mit.money.utils.StatesUtil;
import com.umeng.analytics.game.UMGameAgent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by android on 3/4/16.
 */
public class GuideActivity extends UmengAgentActivity {
    private TextView guideText;
    private StatesUtil statesUtil;

    private String manufacturer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guide_layout);
        statesUtil = new StatesUtil(this);
        manufacturer = getManufacturer();
        initVeiws();
        handleMaterialStatusBar();
    }

    private void initVeiws() {
        guideText = (TextView) findViewById(R.id.guide_title);
        TextView guideMessage = (TextView) findViewById(R.id.guide_message);
        guideMessage.setText(getText(R.string.set_accessibility_msg));
        ImageView guideImage = (ImageView) findViewById(R.id.guide_image);
        guideImage.setImageResource(R.drawable.tips1);

        Button guideSet = (Button) findViewById(R.id.guide_set);
        guideSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                Toast.makeText(GuideActivity.this, getText(R.string.rob_set_service_notify), Toast.LENGTH_SHORT).show();

                Map<String, String> map_value = new HashMap<String, String>();
                map_value.put("SDK_INT", String.valueOf(Build.VERSION.SDK_INT));
                UMGameAgent.onEventValue(GuideActivity.this, "ClickRobmoneySetting", map_value, 1);
            }
        });

        Button guideNext = (Button) findViewById(R.id.guide_next);
        guideNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                if (manufacturer.equals(getString(R.string.miui))) {
                    intent = new Intent(GuideActivity.this, MIUISetActivity.class);
                } else if (manufacturer.equals(getString(R.string.emui))) {
                    intent = new Intent(GuideActivity.this, EMUISetActivity.class);
                } else {
                    intent = new Intent(GuideActivity.this, AppMessageSetting.class);
                }
                startActivity(intent);
                GuideActivity.this.finish();
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

    private String getManufacturer() {
        return Build.MANUFACTURER;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateTitle();
    }

    private void updateTitle() {
        if (statesUtil.getServiceStatus()) {
            guideText.setText("辅助功能已经打开");
            guideText.setTextColor(Color.WHITE);
        } else {
            guideText.setText("辅助功能未打开");
            guideText.setTextColor(Color.RED);
        }
    }
}
