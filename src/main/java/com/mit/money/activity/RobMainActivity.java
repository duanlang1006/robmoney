package com.mit.money.activity;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mit.mitanalytics.UmengAgentActivity;
import com.mit.money.R;
import com.mit.money.card.FuliCardInfo;
import com.mit.money.card.parser.CardParser;
import com.mit.money.utils.AndroidShare;
import com.mit.money.utils.SpUtil;
import com.mit.money.utils.StatesUtil;
import com.umeng.analytics.game.UMGameAgent;
import com.umeng.onlineconfig.OnlineConfigAgent;
import com.umeng.update.UmengUpdateAgent;

import java.util.HashMap;
import java.util.Map;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class RobMainActivity extends UmengAgentActivity implements AccessibilityManager.AccessibilityStateChangeListener,
        View.OnClickListener {

    //AccessibilityService 管理
    private AccessibilityManager accessibilityManager;
    private StatesUtil statesUtil;
    private Toast toast = null;
    private long clickTime = 0; //记录第一次点击的时间
    private TextView stateText;
    private TextView detailText;

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_activity);

//        explicitlyLoadPreferences();
        statesUtil = new StatesUtil(this);

        //监听AccessibilityService 变化
        accessibilityManager = statesUtil.getAccessibilityManager();
        accessibilityManager.addAccessibilityStateChangeListener(this);

        initViews();
        handleMaterialStatusBar();

        sendPollingBroadcast();

        if (!getFirstFlag()) {  //首次进入应用
            SpUtil.setValue(this, SpUtil.KEY_FIRST_ENTER_IN, true);
            creteAlertDialog();
        } else {
            UmengUpdateAgent.silentUpdate(this);
        }
    }

    private void sendPollingBroadcast() {
        long timeValue;
        String alarmTime = OnlineConfigAgent.getInstance().getConfigParams(getApplicationContext(),
                SpUtil.ONLINE_KEY_ALARM_TIME);
        if (alarmTime != null && !alarmTime.isEmpty()) {
            timeValue = Long.valueOf(alarmTime) * 1000;
        } else {
            int t = 1000 * 3600 * 24 * 3;
            timeValue = (long) t;
        }

        Intent intent = new Intent();
        intent.setClassName("com.mit.luck", "com.mit.luck.NotificationReceiver");
        intent.setAction("android.action.rob.notification");

        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        long triggerAtTime = SystemClock.elapsedRealtime();
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, triggerAtTime,
                timeValue, sender);

    }

//    private void showVersion() {
//        TextView versionString = (TextView) findViewById(R.id.version_str);
//        try {
//            String bundleVersion = "";
//            BundleContext bundleContext = BundleContextFactory.getInstance().getBundleContext();
//            if (null != bundleContext && null != bundleContext.getBundle()) {
//                bundleVersion = "-" + bundleContext.getBundle().getVersion();
//            }
//            versionString.setText(this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName + bundleVersion);
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
//    }

    private void creteAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.alertStrTitle));
        builder.setMessage(getString(R.string.alertStr));
        builder.setPositiveButton("给个好评", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    String str = "market://details?id=" + RobMainActivity.this.getPackageName();
                    Intent localIntent = new Intent("android.intent.action.VIEW");
                    localIntent.setData(Uri.parse(str));
                    startActivity(localIntent);
                } catch (Exception e) {
                    Toast.makeText(RobMainActivity.this, "未找到应用市场", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("以后再说", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private boolean getFirstFlag() {
        return (boolean) SpUtil.getValue(this, SpUtil.KEY_FIRST_ENTER_IN, false);
    }

    private void initViews() {
        stateText = (TextView) findViewById(R.id.tv_points);
        if (statesUtil.getServiceStatus()) {
            stateText.setText("已开启");
            stateText.setTextColor(Color.WHITE);
        } else {
            stateText.setText("未开启");
            stateText.setTextColor(Color.RED);
        }

        LinearLayout startLayout = (LinearLayout) findViewById(R.id.la_startGame);
        startLayout.setOnClickListener(this);
        LinearLayout aboutLayout = (LinearLayout) findViewById(R.id.la_about);
        aboutLayout.setOnClickListener(this);
        LinearLayout shareLayout = (LinearLayout) findViewById(R.id.la_voice);
        shareLayout.setOnClickListener(this);

        LinearLayout detailLayout = (LinearLayout) findViewById(R.id.rob_bottom);
        detailLayout.setOnClickListener(this);

        detailText = (TextView) findViewById(R.id.rob_detail_summary);
    }

    /**
     * 适配MIUI沉浸状态栏
     */
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
    public void onAccessibilityStateChanged(boolean enabled) {
        if (enabled) {
            Toast.makeText(this, "抢红包辅助服务已经打开，正在运行", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "抢红包辅助服务已经关闭", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateServiceStatus();

        String size = (String) SpUtil.getValue(getApplicationContext(), SpUtil.KEY_TOTAL_VALUE, "0.00");
        int num = (int) SpUtil.getValue(getApplicationContext(), SpUtil.KEY_TOTAL_NUM, 0);
        if (Double.valueOf(size) == 0) {
            detailText.setText("(已抢到0个红包 共计0元)");
        } else {
            detailText.setText("(已抢到" + String.valueOf(num) + "个红包 共计" + size + "元)");
        }
    }

    private void updateServiceStatus() {
        if (statesUtil.getServiceStatus()) {
            //TODO service is running
            stateText.setText("已开启");
            stateText.setTextColor(Color.WHITE);
        } else {
            //TODO service stoped
            stateText.setText("未开启");
            stateText.setTextColor(Color.RED);
        }
    }

//    public void openSettings(View view) {
//        Intent settingIntent = new Intent(this, SettingsActivity.class);
//        startActivity(settingIntent);
//    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - clickTime) > 2000) {
                toast = Toast.makeText(getApplicationContext(), "再按一次退出应用程序", Toast.LENGTH_SHORT);
                toast.show();
                clickTime = System.currentTimeMillis();
            } else {
                if (null != toast) {
                    toast.cancel();
                }
                this.finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.la_startGame) {
            Intent intent1 = new Intent(this, GuideActivity.class);
            startActivity(intent1);

            Map<String, String> map_value = new HashMap<String, String>();
            map_value.put("SDK_INT", String.valueOf(Build.VERSION.SDK_INT));
            UMGameAgent.onEventValue(this, "ClickRobmoneyGuide", map_value, 1);

        } else if (v.getId() == R.id.la_about) {
//            Intent intent = new Intent(this, AboutActivity.class);
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);

            Map<String, String> map_value = new HashMap<String, String>();
            map_value.put("SDK_INT", String.valueOf(Build.VERSION.SDK_INT));
            UMGameAgent.onEventValue(this, "ClickRobmoneyAbout", map_value, 1);
        } else if (v.getId() == R.id.la_voice) {
            openShare();

            Map<String, String> map_value = new HashMap<String, String>();
            map_value.put("SDK_INT", String.valueOf(Build.VERSION.SDK_INT));
            UMGameAgent.onEventValue(this, "ClickRobmoneyShare", map_value, 1);
        } else if (v.getId() == R.id.rob_bottom) {
            Intent intent = new Intent(this, DetailsActivity.class);
            startActivity(intent);

            Map<String, String> map_value = new HashMap<String, String>();
            map_value.put("SDK_INT", String.valueOf(Build.VERSION.SDK_INT));
            UMGameAgent.onEventValue(this, "ClickRobmoneyDetail", map_value, 1);
        }
    }

    private void openShare() {
        AndroidShare androidShare;
        String shareUrl = OnlineConfigAgent.getInstance().getConfigParams(getApplicationContext(),
                SpUtil.ONLINE_KEY_SHARE_URL);

        if (shareUrl != null && !shareUrl.isEmpty() && !shareUrl.equals("null")) {
            androidShare = new AndroidShare(
                    this,
                    shareUrl,
                    "shareUrl");
        } else {
            androidShare = new AndroidShare(
                    this,
                    getString(R.string.share_str),
                    "shareUrl");
        }
        androidShare.show();
    }

//    public boolean checkInstallation(String packageName) {
//        try {
//            this.getPackageManager().getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
//            return true;
//        } catch (PackageManager.NameNotFoundException e) {
//            return false;
//        }
//    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    protected void onDestroy() {
        accessibilityManager.removeAccessibilityStateChangeListener(this);
        super.onDestroy();
    }
}
