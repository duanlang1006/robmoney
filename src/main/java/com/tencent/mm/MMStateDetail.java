package com.tencent.mm;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;

import com.android.dsc.util.DscLog;
import com.mit.state.ITaskState;
import com.mit.state.StateMachine;
import com.mit.state.Task;

/**
 * Created by android on 3/9/16.
 * 红包详情
 */
public class MMStateDetail implements ITaskState {
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

        if (!mmTask.isFirstEnterDetail() && mmTask.isAutoInFlag()) {
            DscLog.d(this.getClass().getSimpleName(), "exit enter in");
            mmTask.setFirstEnterPacket(true);
            as.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
        }else {
            return;
        }

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            try {
                if (event.getClassName().toString().contains(WECHAT_PACKET_GENERAL_ACTIVITY)
                        && mmTask.isAutoInFlag()) {
                    as.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                    task.setNextState(MMStateFinish.class.getName());
                    StateMachine.executeState(context, task);
                }
            } catch (Exception e) {
                task.setNextState(MMStateFinish.class.getName());
                StateMachine.executeState(context, task);
            }
        }

    }
}
