package com.mit.money.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.dsc.core.DscCoreAgent;
import com.android.dsc.core.DscCoreListener;
import com.android.dsc.core.DscRequestBase;
import com.mit.mitanalytics.UmengAgentActivity;
import com.mit.money.R;

import com.android.volley.Request;
import com.android.volley.VolleyError;

import com.mit.money.activator.BundleContextFactory;
import com.mit.money.card.FuliCardInfo;
import com.mit.money.card.parser.CardParser;
import com.mit.money.utils.SpUtil;
import com.umeng.analytics.game.UMGameAgent;
import com.umeng.onlineconfig.OnlineConfigAgent;

import org.json.JSONObject;
import org.osgi.framework.BundleContext;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by langduan on 16/3/26.
 */
public class CommonActivity extends UmengAgentActivity implements HomeFragment.OnListFragmentInteractionListener {

    private Context mContext;
    private LinearLayout logoview;
    private Toast toast = null;
    private long clickTime = 0; //记录第一次点击的时间
    private String url;

    private Fragment homeFragment;

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.common_webview);

        mContext = getApplicationContext();
        OnlineConfigAgent.getInstance().updateOnlineConfig(mContext);
        url = OnlineConfigAgent.getInstance().getConfigParams(mContext,
                SpUtil.ONLINE_KEY_ENTER_RUL);
        if (url == null || url.isEmpty()) {
            url = "http://www.fuli365.net/intelligent_red_package";
        }

        logoview = (LinearLayout) this.findViewById(R.id.logo_image_view);
        logoview.setVisibility(View.VISIBLE);
    }

    private void enterHome() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (null == homeFragment) {
            homeFragment = HomeFragment.newInstance(1);
        }
        ft.replace(R.id.content, homeFragment).commitAllowingStateLoss();
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    protected void onResume() {
        super.onResume();

        BundleContext bundleContext = BundleContextFactory.getInstance().getBundleContext();
        String version = bundleContext.getBundle().getVersion();

        String meta = getAppMetaData(getApplicationContext());

        if (meta != null && !meta.isEmpty()) {
            url = url + "/" + version + "/" + meta + "/1.txt";
        } else {
            url = url + "/" + version + "/1.txt";
        }

        getInfo(mContext, url);

        final Intent intent = new Intent();
        intent.setAction("android.rob.ExitApp");
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                CommonActivity.this.sendBroadcast(intent);
            }
        }, 2000);
    }

    public String getAppMetaData(Context ctx) {
        if (ctx == null) {
            return null;
        }
        String resultData = null;
        try {
            PackageManager packageManager = ctx.getPackageManager();
            if (packageManager != null) {
                ApplicationInfo applicationInfo = packageManager.
                        getApplicationInfo(ctx.getPackageName(), PackageManager.GET_META_DATA);
                if (applicationInfo != null) {
                    if (applicationInfo.metaData != null) {
                        resultData = applicationInfo.metaData.getString("UMENG_CHANNEL");
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return resultData;
    }


    private void getInfo(final Context context, String url) {
        Request<String> request = new DscRequestBase(Request.Method.POST, url,
                null,
                null,
                new DscCoreListener<String>() {
                    @Override
                    public void onDscResponse(String response) {
                        resolve(response);
                    }

                    @Override
                    public void onDscErrorResponse(VolleyError error) {
                        enterHome();
                        final Intent intent = new Intent();
                        intent.setAction("android.rob.ExitApp");
                        new android.os.Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                CommonActivity.this.sendBroadcast(intent);
                            }
                        }, 2000);
                    }
                });
        DscCoreAgent.robReq(context, request);
    }

    private void resolve(String jsonStr) {
        if (jsonStr == null || TextUtils.isEmpty(jsonStr.trim())) {
            return;
        }
        try {
            JSONObject object = new JSONObject(jsonStr);
            String getValue = object.getString("show_rob");

            if (getValue.equals("1")) {
                SpUtil.setValue(getApplicationContext(), SpUtil.KEY_SUPPORT, true);
                Intent intent = new Intent(CommonActivity.this, RobMainActivity.class);
                startActivity(intent);
                CommonActivity.this.finish();
            } else {
                SpUtil.setValue(getApplicationContext(), SpUtil.KEY_SUPPORT, false);
                logoview.setVisibility(View.GONE);
                enterHome();
            }
        } catch (Exception e) {
            logoview.setVisibility(View.GONE);
            enterHome();
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

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
    public void onListFragmentInteraction(FuliCardInfo cardInfo) {
        Map<String, String> map_value = new HashMap<String, String>();
        map_value.put("SDK_INT", String.valueOf(Build.VERSION.SDK_INT));
        CardParser parserInstance = cardInfo.getParserInstance();
        if (null != parserInstance && parserInstance.launchIntent(this, cardInfo)) {
            map_value.put("FULI_ID", String.valueOf(cardInfo.getId()));
            map_value.put("FULI_NAME", cardInfo.getTitle());
            map_value.put("FULI_INTENT", "success");
        } else {
            Toast.makeText(this, "出错啦，赶紧去反馈吧", Toast.LENGTH_SHORT).show();

            map_value.put("FULI_ID", String.valueOf(cardInfo.getId()));
            map_value.put("FULI_NAME", cardInfo.getTitle());
            map_value.put("FULI_INTENT", "failure");
        }
        UMGameAgent.onEventValue(this, "ClickViewpagerItem", map_value, 1);
    }

}
