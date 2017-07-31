package com.tencent.mm;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.android.dsc.util.DscLog;
import com.mit.state.ITaskState;
import com.mit.state.StateMachine;
import com.mit.state.Task;

/**
 * Created by android on 3/10/16.
 */
public class MMStateReply implements ITaskState {
    private static final String WECHAT_PACKET_GENERAL_ACTIVITY = "LauncherUI";

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void process(Context context, Task task, Object... params) {
        AccessibilityEvent event;
        AccessibilityService as;
        if (null == task || params.length < 1 || !(params[0] instanceof AccessibilityEvent)) {
            return;
        }
        event = (AccessibilityEvent) params[0];
        DscLog.d(this.getClass().getSimpleName(), "eventType:" + event.getEventType());

        if (context instanceof AccessibilityService) {
            as = (AccessibilityService) context;
        } else {
            return;
        }
        MMTask mmTask = (MMTask) task;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(as);
        String reply = generateReplyString(sharedPreferences, mmTask);
        if (reply != null) {
            mmTask.setReplyString(reply);
        } else {
            as.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
            task.setNextState(MMStateFinish.class.getName());
            StateMachine.executeState(context, task);
        }

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (mmTask.isUnPacketSuccess()) {
                setReply(as, mmTask.getReplyString());
                as.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);

                try {
                    ComponentName componentName = new ComponentName(
                            event.getPackageName().toString(),
                            event.getClassName().toString());
                    context.getPackageManager().getActivityInfo(componentName, 0);
                    if (componentName.flattenToShortString().contains(WECHAT_PACKET_GENERAL_ACTIVITY)) {
                        task.setNextState(MMStateFinish.class.getName());
                        StateMachine.executeState(context, task);
                    }
                } catch (Exception e) {
                    task.setNextState(MMStateFinish.class.getName());
                    StateMachine.executeState(context, task);
                }
            }

        } else if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {

        }

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setReply(AccessibilityService as, String text) {
        try {
            AccessibilityNodeInfo outNode = as.getRootInActiveWindow().getChild(0).getChild(0);
            AccessibilityNodeInfo nodeToInput = outNode.getChild(outNode.getChildCount() - 1).getChild(0).getChild(1);
            if ("android.widget.EditText".equals(nodeToInput.getClassName())) {
                Bundle arguments = new Bundle();
                arguments.putCharSequence(AccessibilityNodeInfo
                        .ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
                nodeToInput.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
            }

            SystemClock.sleep(300);      //主动休眠100ms

            AccessibilityNodeInfo outNode1 = as.getRootInActiveWindow().getChild(0).getChild(0);
            AccessibilityNodeInfo nodeToSend = outNode1.getChild(outNode1.getChildCount() - 1).getChild(0).getChild(3);
            if ("android.widget.Button".equals(nodeToSend.getClassName())) {
                if ("发送".equals(nodeToSend.getText().toString()))
                    nodeToSend.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }

        } catch (Exception e) {
            // Not support
        }
    }

    private String generateReplyString(SharedPreferences sharedPreferences, MMTask mmTask) {
        Boolean needComment = sharedPreferences.getBoolean("pref_reply_switch", false);
        if (!needComment) return null;

        String[] wordsArray = sharedPreferences.getString("pref_reply_words", "").split(" +");
        if (wordsArray.length == 0) return null;

        Boolean atSender = sharedPreferences.getBoolean("pref_reply_at", false);
        if (atSender) {
            return "@" + mmTask.getSender() + " " + wordsArray[(int) (Math.random() * wordsArray.length)];
        } else {
            return wordsArray[(int) (Math.random() * wordsArray.length)];
        }
    }

}
