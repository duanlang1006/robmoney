package com.tencent.mm;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.os.Parcelable;
import android.view.accessibility.AccessibilityEvent;

import com.android.dsc.util.DscLog;
import com.mit.money.utils.PowerUtil;
import com.mit.state.ITaskState;
import com.mit.state.ITaskStateListener;
import com.mit.state.StateMachine;
import com.mit.state.Task;
import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hxd on 16-3-8.
 */
public class MMStateStart implements ITaskState {
    private static final String WECHAT_PACKET_GENERAL_ACTIVITY = "LauncherUI";

    @Override
    public void process(Context context, Task task, Object... params) {
        AccessibilityEvent event = null;
        if (null == task || params.length < 1 || !(params[0] instanceof AccessibilityEvent)) {
            return;
        }
        MMTask mmTask = (MMTask) task;

        event = (AccessibilityEvent) params[0];
        DscLog.d(this.getClass().getSimpleName(), "eventType:" + event.getEventType() + ",pkg:" + event.getPackageName() + ",cls:" + event.getClassName());
        if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            Parcelable parcelable = event.getParcelableData();
            if (parcelable instanceof Notification) {
                Notification notification = (Notification) parcelable;
                try {
                    PowerUtil.getInstance(context).disableKeyguard();
                    notification.contentIntent.send();
                    mmTask.setStartFromList(true);
                    mmTask.setAutoInFlag(true);

                    Map<String, String> map_value = new HashMap<String, String>();
                    map_value.put("SDK_INT", String.valueOf(Build.VERSION.SDK_INT));
                    map_value.put("packageName", event.getPackageName().toString());
                    MobclickAgent.onEventValue(context, "RobServiceNotification", map_value, 1);
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                    task.setNextState(MMStateFinish.class.getName());
                    StateMachine.executeState(context, task);

                }
            }
            ITaskStateListener listener = task.getStateListener();
            if (null != listener) {
                listener.onStateStarted(context, task);
            }
        } else if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            try {
                if (event.getClassName().toString().contains(WECHAT_PACKET_GENERAL_ACTIVITY)) {
                    task.setNextState(MMStateEnterChat.class.getName());
                }
            } catch (Exception e) {
                task.setNextState(MMStateFinish.class.getName());
                StateMachine.executeState(context, task);
            }
        } else if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            if (mmTask.isStartFromList()) {
                mmTask.setStartFromList(false);
                task.setNextState(MMStateEnterChat.class.getName());
            }
        }
    }
}
