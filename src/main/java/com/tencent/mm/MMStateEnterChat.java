package com.tencent.mm;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.android.dsc.util.DscLog;
import com.mit.state.ITaskState;
import com.mit.state.StateMachine;
import com.mit.state.Task;

import java.util.List;

/**
 * Created by hxd on 16-3-9.
 * 进入聊天界面，收集红包节点放到task中保存
 */
public class MMStateEnterChat implements ITaskState {
    private static final String WECHAT_PACKET_RECEIVE_ACTIVITY = "LuckyMoneyReceiveUI";
    private static final String WECHAT_PACKET_DETAIL_ACTIVITY = "LuckyMoneyDetailUI";
    private static final String WECHAT_PACKET_GENERAL_ACTIVITY = "LauncherUI";

    private static final String WECHAT_BETTER_LUCK_CH = "手慢了";
    private static final String WECHAT_EXPIRES_CH = "红包已失效";

    private static final String WECHAT_VIEW_OTHERS_CH = "领取红包";
    private static final String WECHAT_VIEW_SELF_CH = "查看红包";

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void process(Context context, Task task, Object... params) {
        AccessibilityEvent event;
        AccessibilityService as;
        if (null == task || params.length < 1 || !(params[0] instanceof AccessibilityEvent)) {
            return;
        }
        event = (AccessibilityEvent) params[0];
        DscLog.d(this.getClass().getSimpleName(), "eventType:" + event.getEventType() + ",pkg:" + event.getPackageName() + ",cls:" + event.getClassName());

        if (context instanceof AccessibilityService) {
            as = (AccessibilityService) context;
        } else {
            return;
        }

        MMTask mmTask = (MMTask) task;
//        AccessibilityNodeInfo rootInfo = as.getRootInActiveWindow();
//        if (rootInfo == null) {
//            DscLog.d(this.getClass().getSimpleName(), "EnterChat get rootInfo fail");
//            task.setNextState(MMStateFinish.class.getName());
//            StateMachine.executeState(context, task);
//            return;
//        }

        if (!mmTask.isFirstEnterChat()) {
            mmTask.setFirstEnterChat(true);
            AccessibilityNodeInfo rootInfo = as.getRootInActiveWindow();
            if (rootInfo == null) {
                DscLog.d(this.getClass().getSimpleName(), "EnterChat get rootInfo fail");
                task.setNextState(MMStateFinish.class.getName());
                StateMachine.executeState(context, task);
                return;
            }
            AccessibilityNodeInfo receivedNode = getLastNode(rootInfo,
                    new String[]{WECHAT_VIEW_OTHERS_CH});   //WECHAT_VIEW_SELF_CH
            if (receivedNode == null) {
                DscLog.d(this.getClass().getSimpleName(), "EnterChat not found receive node");
                as.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                task.setNextState(MMStateFinish.class.getName());
                StateMachine.executeState(context, task);
                return;
            }
            if ("android.widget.LinearLayout".equals(receivedNode.getParent().getClassName())) {
                receivedNode.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        } else if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            try {
                if (event.getClassName().toString().contains(WECHAT_PACKET_RECEIVE_ACTIVITY)) {

                    AccessibilityNodeInfo rootInfo = as.getRootInActiveWindow();
                    if (rootInfo == null) {
                        DscLog.d(this.getClass().getSimpleName(), "EnterChat get rootInfo fail");
                        task.setNextState(MMStateFinish.class.getName());
                        StateMachine.executeState(context, task);
                        return;
                    }

                    List<AccessibilityNodeInfo> lateNode = rootInfo.findAccessibilityNodeInfosByText(WECHAT_BETTER_LUCK_CH);
                    if (!lateNode.isEmpty()) {    //未找到红包节点，直接执行返回动作
                        as.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                    } else {
                        task.setNextState(MMStatePacketCover.class.getName());
                    }
                } else if (event.getClassName().toString().contains(WECHAT_PACKET_DETAIL_ACTIVITY)) {
                    task.setNextState(MMStateDetail.class.getName());
                } else if (event.getClassName().toString().contains(WECHAT_PACKET_GENERAL_ACTIVITY)) {
                    DscLog.d(this.getClass().getSimpleName(), "EnterChat WECHAT_PACKET_GENERAL_ACTIVITY");
                    as.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                    task.setNextState(MMStateFinish.class.getName());
                    StateMachine.executeState(context, task);
                }
            } catch (Exception e) {
                DscLog.d(this.getClass().getSimpleName(), "EnterChat Window Content changed");
                e.printStackTrace();
                task.setNextState(MMStateFinish.class.getName());
                StateMachine.executeState(context, task);
            }
        } else if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            DscLog.d(this.getClass().getSimpleName(), "EnterChat Window Content changed");
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private AccessibilityNodeInfo getLastNode(AccessibilityNodeInfo rootInfo, String[] texts) {
        if (rootInfo == null) return null;
        int bottom = 0;
        AccessibilityNodeInfo lastNode = null;

        for (String text : texts) {
            if (text == null) continue;

            List<AccessibilityNodeInfo> nodes = rootInfo.findAccessibilityNodeInfosByText(text);
            if (nodes != null && nodes.size() > 0) {
                AccessibilityNodeInfo node = nodes.get(nodes.size() - 1);
                Rect bounds = new Rect();
                node.getBoundsInScreen(bounds);
                if (bounds.bottom > bottom) {
                    bottom = bounds.bottom;
                    lastNode = node;
                }
            }
        }
        return lastNode;
    }
}
