package com.mit.money.activity;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.mit.mitanalytics.UmengAgentActivity;
import com.mit.money.R;

/**
 * Created by android on 3/4/16.
 */
public class AppMessageSetting extends UmengAgentActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guide_set_app_layout);
        initVeiws();
        handleMaterialStatusBar();
    }

    private void initVeiws() {
//        TextView guideText = (TextView) findViewById(R.id.guide_title);
//        guideText.setText("关闭微信的消息免打扰");
//        guideText.setTextColor(Color.WHITE);
        ImageView guideImage = (ImageView) findViewById(R.id.guide_image);
        guideImage.setImageResource(R.drawable.tips_app);

        Button guideClose = (Button) findViewById(R.id.guide_close);
        guideClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppMessageSetting.this.finish();
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

    @Override
    protected void onResume() {
        super.onResume();
    }

}
