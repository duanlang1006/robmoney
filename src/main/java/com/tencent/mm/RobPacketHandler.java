package com.tencent.mm;

import android.app.Notification;
import android.content.Context;
import android.os.Parcelable;
import android.view.accessibility.AccessibilityEvent;
import com.android.dsc.util.DscLog;
import com.mit.money.service.FIFOTaskHandler;
import com.mit.state.Task;

/**
 * Created by hxd on 16-3-8.
 */
public class RobPacketHandler {
    private static final String PACKAGE_NAME = "com.tencent.mm";
    private static final String WECHAT_NOTIFICATION_TIP = "[微信红包]";

    /***
     * 对于抢红包流程使用状态模式进行处理，收到合法的通知后会创建一个stateInfo实例
     * 通过StateMachine调度到实例标志的状态中去运行，每个状态会收到WM_CHANGE和CONTENT_CHANGE事件，
     * 各取所需后转入下一个状态处理
     * 微信的各状态描述如下：
     * StateReady        就绪（初始）状态，条件：收到一个合法的通知
     * StateStart        开始处理，做一些解锁之类的工作
     * StateEnterChat    点击通知后进入聊天界面，这个状态中可能会接收到多个节点信息（CONTENT_CHANGE事件），依次处理后进入StatePacketCover
     * StatePacketCover  拆红包界面,
     * StatePacketDetail 红包详情
     * StatePacketEmpty  来晚一步，没有抢到
     * StateBackToChat   退回聊天界面
     * StateAutoReply    自动回复
     * StateFinish       做上锁工作
     */

    public static void onAccessibilityEvent(Context context,AccessibilityEvent event) {
        if (!PACKAGE_NAME.equals(event.getPackageName().toString())) {
            return;
        }
        DscLog.d(RobPacketHandler.class.getName(), "onAccessibilityEvent," + event.getEventType()+",pkg:"+event.getPackageName()+",cls:"+event.getClassName());
        if (event.getEventType() != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED){
            FIFOTaskHandler.getInstance().onEvent(context, event);
        }else{
            String tip = event.getText().toString();
            Parcelable parcelable = event.getParcelableData();
            if (tip.contains(WECHAT_NOTIFICATION_TIP)
                    && null != parcelable
                    && parcelable instanceof Notification) {
                Task task = new MMTask(MMStateStart.class.getName(),MMStateFinish.class.getName());
                task.setStateListener(FIFOTaskHandler.getInstance());
                FIFOTaskHandler.getInstance().onEnque(context, task, event);
            }
        }
    }

}
