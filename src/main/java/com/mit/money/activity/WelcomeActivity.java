package com.mit.money.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import com.mit.mitanalytics.UmengAgentActivity;
import com.mit.money.R;
import com.mit.money.activator.BundleContextFactory;

import org.osgi.framework.BundleContext;

/**
 * Created by android on 3/4/16.
 */
public class WelcomeActivity extends UmengAgentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.welcome_activity);

        TextView versionString = (TextView) findViewById(R.id.version_str);
        try {
            String bundleVersion = "";
            BundleContext bundleContext = BundleContextFactory.getInstance().getBundleContext();
            if (null != bundleContext && null != bundleContext.getBundle()) {
                bundleVersion = "-" + bundleContext.getBundle().getVersion();
            }
            versionString.setText(this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName + bundleVersion);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(WelcomeActivity.this, RobMainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                WelcomeActivity.this.finish();
            }
        }, 3000);
    }
}
