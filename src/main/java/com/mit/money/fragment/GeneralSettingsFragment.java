package com.mit.money.fragment;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.dsc.feedback.DscFeedbackAgent;
import com.mit.mitanalytics.UmengAgentPreferenceFragment;
import com.mit.money.R;
import com.mit.money.activator.BundleContextFactory;
import com.mit.money.utils.SpUtil;

import org.osgi.framework.BundleContext;


/**
 * Created by android on 2/25/16.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class GeneralSettingsFragment extends UmengAgentPreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        assert view != null;
        addPreferencesFromResource(R.xml.general_preferences);
        setPrefListeners();
//        view.setBackgroundColor(getResources().getColor(R.color.crazy_bg));
        return view;
    }

    private void setPrefListeners() {
        Preference praisePref = findPreference(SpUtil.KEY_PRAISE);
        praisePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                try {
                    String str = "market://details?id=" + getActivity().getPackageName();
                    Intent localIntent = new Intent("android.intent.action.VIEW");
                    localIntent.setData(Uri.parse(str));
                    startActivity(localIntent);
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "未找到应用市场", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
        Preference feedbackPref = findPreference(SpUtil.KEY_FEEDBACK);
        feedbackPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DscFeedbackAgent.startFeedbackActivity(getActivity(), R.class.getPackage().getName());
                return false;
            }
        });

        Preference versionPref = findPreference(SpUtil.KEY_VERESION);
        versionPref.setSummary(getVersion());
    }

    private String getVersion() {
        try {
            String bundleVersion = "";
            String str = getActivity().getPackageManager().
                    getPackageInfo(getActivity().getPackageName(), 0).versionName;
            BundleContext bundleContext = BundleContextFactory.getInstance().getBundleContext();
            if (null != bundleContext && null != bundleContext.getBundle()) {
                bundleVersion = "-" + bundleContext.getBundle().getVersion();
            }
            return str + bundleVersion;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


}
