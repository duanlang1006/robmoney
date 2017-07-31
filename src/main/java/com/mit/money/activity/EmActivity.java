package com.mit.money.activity;

import com.android.dsc.util.DscLog;
import com.mit.mitanalytics.UmengAgentActivity;
import android.os.Bundle;
import com.mit.money.R;

public class EmActivity extends UmengAgentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_em);
        DscLog.setIsDebug(true);
    }
}
