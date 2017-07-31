package com.mit.money.fragment;

import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;

import com.mit.mitanalytics.UmengAgentPreferenceFragment;
import com.mit.money.R;


/**
 * Created by android on 2/25/16.
 */
public class ReplySettingsFragment extends UmengAgentPreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.reply_preferences);
        setPrefListeners();
    }

    private void setPrefListeners() {
        Preference replyPref = findPreference("pref_reply_switch");
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            replyPref.setEnabled(false);
        }

        Preference replyWordsPref = findPreference("pref_reply_words");
        String value = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("pref_reply_words", "");
        if (value.length() > 0) replyWordsPref.setSummary(value);

        replyWordsPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                if (o != null && o.toString().length() > 0) {
                    preference.setSummary(o.toString());
                } else {
                    preference.setSummary("");
                }
                return true;
            }
        });
    }
}
