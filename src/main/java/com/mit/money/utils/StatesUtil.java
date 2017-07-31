package com.mit.money.utils;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.accessibility.AccessibilityManager;

import java.util.List;

/**
 * Created by android on 3/4/16.
 */
public class StatesUtil {
    private AccessibilityManager accessibilityManager;
    private Context mContext;

    public AccessibilityManager getAccessibilityManager() {
        return accessibilityManager;
    }

    public void setAccessibilityManager(AccessibilityManager accessibilityManager) {
        this.accessibilityManager = accessibilityManager;
    }

    public StatesUtil(Context context) {
        this.mContext = context;
        accessibilityManager = (AccessibilityManager) mContext.getSystemService(Context.ACCESSIBILITY_SERVICE);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public boolean getServiceStatus() {
        List<AccessibilityServiceInfo> accessibilityServices =
                accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (AccessibilityServiceInfo info : accessibilityServices) {
            if (info.getId().equals(mContext.getPackageName() + "/com.mit.money.service.RobPacketService")) {
                return true;
            }
        }
        return false;
    }
}
