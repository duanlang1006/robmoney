package com.mit.money.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.mit.money.utils.PacketSignature;
import com.mit.money.utils.PowerUtil;
import com.mit.money.utils.SpUtil;
import com.umeng.analytics.MobclickAgent;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by android on 2/25/16.
 */
public class RobPacketService extends AccessibilityService {
    private SharedPreferences sharedPreferences;
    private PowerUtil powerUtil;

    //要监控的应用包名
    private static final String WECHAT_PACKAGE = "com.tencent.mm";
    private static final String QQ_MOBILE_PACKAGE = "com.tencent.mobileqq";
    private static final String ALI_PAY_PACKAGE = "com.eg.android.AlipayGphone";

    //监控的关键字
    private static final String WECHAT_NOTIFICATION_TIP = "[微信红包]";
    private static final String WECHAT_VIEW_OTHERS_CH = "领取红包";
    private static final String WECHAT_VIEW_SELF_CH = "查看红包";
    private static final String WECHAT_DETAILS_CH = "红包详情";
    private static final String WECHAT_BETTER_LUCK_CH = "手慢了";
    private static final String WECHAT_EXPIRES_CH = "红包已失效";
    private static final String WECHAT_DETAILS_EN = "Details";
    private static final String WECHAT_BETTER_LUCK_EN = "Better luck next time!";
    private static final String QQ_NOTIFICATION_TIP = "[QQ红包]";
    private static final String QQ_OPEN_CH = "点击拆开";
    private static final String QQ_DETAIL_CH = "已存入余额";
    private static final String QQ_LUCK_NEXT_TIME_CH = "来晚一步";

    //监控的activity的类名
    private static final String WECHAT_PACKET_RECEIVE_ACTIVITY = "LuckyMoneyReceiveUI";
    private static final String WECHAT_PACKET_DETAIL_ACTIVITY = "LuckyMoneyDetailUI";
    private static final String WECHAT_PACKET_GENERAL_ACTIVITY = "LauncherUI";
    private static final String QQ_PACKET_ACTIVITY = "QWalletPluginProxyActivity";
    private String currentActivityName = WECHAT_PACKET_GENERAL_ACTIVITY;

    private boolean mMutex = false;
    private AccessibilityNodeInfo rootNodeInfo;
    private AccessibilityNodeInfo mWxReceiveNode;
    private AccessibilityNodeInfo mWxUnpackNode;
    private AccessibilityNodeInfo mQQReceiveNode;
    private boolean mWxPacketPicked;
    private boolean mWxPacketReceived;
    private boolean mQQPacketPicked;
    private boolean mQQPacketReceived;
    private int mUnpackCount = 0;

    private boolean autoIn;
    private int checkTimes;

    private PacketSignature signature = new PacketSignature();

    private void watchFlagsFromPreference() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    protected void onServiceConnected() {
        Map<String, String> map_value = new HashMap<String, String>();
        map_value.put("SDK_INT", String.valueOf(Build.VERSION.SDK_INT));
        MobclickAgent.onEventValue(this, "RobServiceConnected", map_value, 1);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            this.watchFlagsFromPreference();

            this.powerUtil = new PowerUtil(this);

            AccessibilityServiceInfo info = getServiceInfo();
            info.packageNames = new String[]{WECHAT_PACKAGE};   //, QQ_MOBILE_PACKAGE, ALI_PAY_PACKAGE
            setServiceInfo(info);

            super.onServiceConnected();
        } else {
            Toast.makeText(this, "设备android版本过低，不支持红包辅助，请升级到4.2以上", Toast.LENGTH_SHORT).show();
        }
        FIFOTaskHandler.getInstance().onInit();
    }

    @Override
    public void onInterrupt() {
        //TODO
        FIFOTaskHandler.getInstance().onInit();

        Map<String, String> map_value = new HashMap<String, String>();
        map_value.put("SDK_INT", String.valueOf(Build.VERSION.SDK_INT));
        MobclickAgent.onEventValue(this, "RobServiceInterrupt", map_value, 1);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        FIFOTaskHandler.getInstance().onInit();

        super.onDestroy();
        Map<String, String> map_value = new HashMap<String, String>();
        map_value.put("SDK_INT", String.valueOf(Build.VERSION.SDK_INT));
        MobclickAgent.onEventValue(this, "RobServiceDestroy", map_value, 1);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        boolean flag = (boolean) SpUtil.getValue(getApplicationContext(), SpUtil.KEY_SUPPORT, false);
        if (!flag) {
            return;
        }
        try {
            String handler = event.getPackageName().toString() + ".RobPacketHandler";
            Class cls = Class.forName(handler);
            Method method = cls.getDeclaredMethod("onAccessibilityEvent", Context.class, AccessibilityEvent.class);
            method.invoke(null, this, event);
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }

        //为优化服务,被触发时优先做应用包名判断，只有在被监控应用时才启动动作
        if (event.getPackageName().toString().equals(WECHAT_PACKAGE)) {     //微信
            if (sharedPreferences == null) return;
            setWxCurrentActivityName(event);
            if (!mMutex) {
                if (sharedPreferences.getBoolean(SpUtil.KEY_WATCH_WECHAT, true) && watchWechatNotifications(event))
                    return;
            }
            if (sharedPreferences.getBoolean(SpUtil.KEY_OPEN_PACKET, true) && autoIn)     //默认自动打开微信红包
                openWxPacket(event);
        } else if (event.getPackageName().toString().equals(QQ_MOBILE_PACKAGE)) {   //QQ
            if (sharedPreferences == null) return;
            setQQCurrentActivityName(event);
            if (!mMutex) {
                if (sharedPreferences.getBoolean(SpUtil.KEY_WATCH_QQ, false) && watchQQNotifications(event))
                    return;
            }
            if (sharedPreferences.getBoolean(SpUtil.KEY_OPEN_PACKET, true))     //默认自动打开QQ红包
                openQQPacket(event);
        } else if (event.getPackageName().toString().equals(ALI_PAY_PACKAGE)) {     //支付宝
            if (sharedPreferences == null) return;
            setWxCurrentActivityName(event);
            if (!mMutex) {
                if (sharedPreferences.getBoolean(SpUtil.KEY_WATCH_ALI, false))
                    return;
            }
            if (sharedPreferences.getBoolean(SpUtil.KEY_OPEN_PACKET, true))     //默认自动打开支付宝红包
                openAliPacket(event);
        }
    }

//    private void playAudio() {
//        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
//        r.play();
//    }

    private boolean watchWechatNotifications(AccessibilityEvent event) {
        if (event.getEventType() != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED)
            return false;
        String tip = event.getText().toString();
        if (!tip.contains(WECHAT_NOTIFICATION_TIP)) return true;

        Map<String, String> map_value = new HashMap<String, String>();
        map_value.put("SDK_INT", String.valueOf(Build.VERSION.SDK_INT));
        map_value.put("packageName", event.getPackageName().toString());
        MobclickAgent.onEventValue(this, "RobServiceNotification", map_value, 1);

        Parcelable parcelable = event.getParcelableData();
        if (parcelable instanceof Notification) {
            Notification notification = (Notification) parcelable;
            try {
                signature.cleanSignature();
                flagResetDelay();
                if (powerUtil != null) {
                    powerUtil.disableKeyguard();
                }

                /* 当powerUtil为null时，即自动亮屏解锁失败情况，
                仍然选择激活PendingIntent,这样手动亮屏解锁后自动开抢红包 */
                autoIn = true;
                checkTimes = 0;
                notification.contentIntent.send();

                /* 为防止意外情况出现导致自动抢红包卡在过程中某一个界面上，从而无法释放键盘锁
                     * 在触发条件自动亮屏解锁后，直接启动一个延时任务来复位所有标志位与键盘锁
                     * 时间大小为: 开红包延时时间 + 答谢延时时间 + 10s左右*/
                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mMutex = false;
                        mWxPacketPicked = false;
                        mUnpackCount = 0;
                        autoIn = false;
                        checkTimes = 0;
                        if (powerUtil != null) {
                            powerUtil.reenableKeyguard();
                        }
                    }
                }, sharedPreferences.getInt(SpUtil.KEY_OPEN_DELAY, 0) * 1000 +
                        sharedPreferences.getInt(SpUtil.KEY_REPLY_DELAY, 0) * 1000 + 10000);

                /* 以上逻辑会存在一个现象，当环境处于解锁失败时，PendingIntent仍被激活，
                但后续会紧跟着启动一个延时复位线程，待复位线程启动后，autoIn被置为false，不会自动打开红包
                 界面会处于红包所在的聊天对象窗口，没有后续触发性动作*/

            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private boolean watchQQNotifications(AccessibilityEvent event) {
        if (event.getEventType() != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED)
            return false;
        String tip = event.getText().toString();
        if (!tip.contains(QQ_NOTIFICATION_TIP)) return true;

        Parcelable parcelable = event.getParcelableData();
        if (parcelable instanceof Notification) {
            Notification notification = (Notification) parcelable;
            try {
                signature.cleanSignature();
                flagResetDelay();
                if (powerUtil != null) {
                    powerUtil.disableKeyguard();
                }

                /* 为防止意外情况出现导致自动抢红包卡在过程中某一个界面上，从而无法释放键盘锁
                     * 在触发条件自动亮屏解锁后，直接启动一个延时任务来复位所有标志位与键盘锁
                     * 时间大小为: 开红包延时时间 + 答谢延时时间 + 10s左右*/
                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mMutex = false;
                        mQQPacketPicked = false;
                        mUnpackCount = 0;
                        if (powerUtil != null) {
                            powerUtil.reenableKeyguard();
                        }
                    }
                }, sharedPreferences.getInt(SpUtil.KEY_OPEN_DELAY, 0) * 1000 +
                        sharedPreferences.getInt(SpUtil.KEY_REPLY_DELAY, 0) * 1000 + 10000);

                notification.contentIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /*
    *
    * 添加定时器，复位所有标志位，互斥锁。
    */
    private void flagResetDelay() {
        int openTime = sharedPreferences.getInt(SpUtil.KEY_OPEN_DELAY, 0) * 1000;
        int replyTime = sharedPreferences.getInt(SpUtil.KEY_REPLY_DELAY, 0) * 1000;
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mMutex = false;
                mWxPacketPicked = false;
                mQQPacketPicked = false;
                mUnpackCount = 0;
            }
        }, openTime + replyTime + 10000);
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void openWxPacket(AccessibilityEvent event) {
        this.rootNodeInfo = getRootInActiveWindow();
        if (rootNodeInfo == null) return;

        mWxReceiveNode = null;
        mWxUnpackNode = null;
        checkWxNodeInfo(event.getEventType());

        /* 如果已经接收到红包并且还没有领取 */
        if (mWxPacketReceived && !mWxPacketPicked && (mWxReceiveNode != null)) {
            mMutex = true;
            mWxReceiveNode.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
            mWxPacketReceived = false;
            mWxPacketPicked = true;
        }

        /* 如果领取但还未戳开 */
        if (mUnpackCount == 1 && (mWxUnpackNode != null)) {
            int delayFlag = sharedPreferences.getInt(SpUtil.KEY_OPEN_DELAY, 0) * 1000;
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        mWxUnpackNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    } catch (Exception e) {
                        mMutex = false;
                        mWxPacketPicked = false;
                        mUnpackCount = 0;
                    }
                }
            }, delayFlag);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void checkWxNodeInfo(int eventType) {
        if (this.rootNodeInfo == null) return;

        /* 聊天会话窗口，遍历节点匹配“领取红包”和"查看红包" */
        AccessibilityNodeInfo node1 = (sharedPreferences.getBoolean("pref_watch_self", false)) ?
                this.getWxTheLastNode(WECHAT_VIEW_OTHERS_CH, WECHAT_VIEW_SELF_CH) : this.getWxTheLastNode(WECHAT_VIEW_OTHERS_CH);
        if (checkTimes == 0 && node1 == null) {
            checkTimes++;
            mMutex = false;
            autoIn = false;
            performGlobalAction(GLOBAL_ACTION_BACK);
        } else if (node1 != null && currentActivityName.contains(WECHAT_PACKET_GENERAL_ACTIVITY)) {
            checkTimes++;
            if (this.signature.generateSignature(node1, "wechat")) {
                mWxPacketReceived = true;
                mWxReceiveNode = node1;
            }
            return;
        }

        /* 戳开红包，红包还没抢完，遍历节点匹配“拆红包” */
        AccessibilityNodeInfo node2 = findWxOpenButton(this.rootNodeInfo);
        if (node2 != null && "android.widget.Button".equals(node2.getClassName()) &&
                currentActivityName.contains(WECHAT_PACKET_RECEIVE_ACTIVITY)) {
            mWxUnpackNode = node2;
            mUnpackCount += 1;
            return;
        }

        /* 戳开红包，红包已被抢完，遍历节点匹配“红包详情”和“手慢了” */
        boolean hasNodes = this.hasOneOfThoseNodes(
                WECHAT_BETTER_LUCK_CH, WECHAT_DETAILS_CH,
                WECHAT_BETTER_LUCK_EN, WECHAT_DETAILS_EN, WECHAT_EXPIRES_CH);
        if (mMutex && hasNodes && (currentActivityName.contains(WECHAT_PACKET_DETAIL_ACTIVITY)
                || currentActivityName.contains(WECHAT_PACKET_RECEIVE_ACTIVITY))) {
            boolean losePacket = this.hasOneOfThoseNodes(WECHAT_BETTER_LUCK_CH, WECHAT_BETTER_LUCK_EN, WECHAT_EXPIRES_CH);
            mMutex = false;
            mWxPacketPicked = false;
            mUnpackCount = 0;
            autoIn = false;
            performGlobalAction(GLOBAL_ACTION_BACK);

            if (losePacket) {
                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        performGlobalAction(GLOBAL_ACTION_BACK);
                        if (powerUtil != null) {
                            powerUtil.reenableKeyguard();
                        }
                    }
                }, 500);
            } else {
                //传入自动答谢内容并退出当前会话页面
                signature.replyString = generateReplyString();
                int delayReply = sharedPreferences.getInt(SpUtil.KEY_REPLY_DELAY, 0) * 1000;
                new android.os.Handler().postDelayed(new Runnable() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void run() {
                        if (signature.replyString != null) {
                            setWxReply();
                            signature.replyString = null;
                        }
                        performGlobalAction(GLOBAL_ACTION_BACK);

                        if (powerUtil != null) {
                            powerUtil.reenableKeyguard();
                        }
                    }
                }, delayReply + 500);

                Map<String, String> map_value = new HashMap<String, String>();
                map_value.put("SDK_INT", String.valueOf(Build.VERSION.SDK_INT));
                map_value.put("packageName", WECHAT_PACKAGE);
                MobclickAgent.onEventValue(this, "RobServiceSuccess", map_value, 1);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private AccessibilityNodeInfo getWxTheLastNode(String... texts) {
        int bottom = 0;
        AccessibilityNodeInfo lastNode = null;

        for (String text : texts) {
            if (text == null) continue;

            List<AccessibilityNodeInfo> nodes = this.rootNodeInfo.findAccessibilityNodeInfosByText(text);
            if (nodes != null && nodes.size() > 0) {
                AccessibilityNodeInfo node = nodes.get(nodes.size() - 1);
                Rect bounds = new Rect();
                node.getBoundsInScreen(bounds);
                if (bounds.bottom > bottom) {
                    bottom = bounds.bottom;
                    lastNode = node;
                    signature.others = text.equals(WECHAT_VIEW_OTHERS_CH);
                }
            }
        }
        return lastNode;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private AccessibilityNodeInfo findWxOpenButton(AccessibilityNodeInfo node) {
        if (node == null)
            return null;

        //非layout元素
        if (node.getChildCount() == 0) {
            if ("android.widget.Button".equals(node.getClassName()))
                return node;
            else
                return null;
        }

        //layout元素，遍历找button
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo button = findWxOpenButton(node.getChild(i));
            if (button != null)
                return button;
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void openQQPacket(AccessibilityEvent event) {
        this.rootNodeInfo = getRootInActiveWindow();
        if (rootNodeInfo == null) return;

        mQQReceiveNode = null;
        checkQQNodeInfo(event.getEventType());

        /* 如果已经接收到红包并且还没有戳开 */
        if (mQQPacketReceived && !mQQPacketPicked && (mQQReceiveNode != null)) {
            mMutex = true;
            mQQPacketReceived = false;
            mQQPacketPicked = true;
            final AccessibilityNodeInfo mQQReceiveParentNode = mQQReceiveNode.getParent();
            int delayFlag = sharedPreferences.getInt(SpUtil.KEY_OPEN_DELAY, 0) * 1000;
            new android.os.Handler().postDelayed(new Runnable() {
                @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                @Override
                public void run() {
                    mQQReceiveParentNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }, delayFlag);
        }

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void checkQQNodeInfo(int eventType) {
        if (this.rootNodeInfo == null) return;

        /* 聊天会话窗口，遍历节点匹配“领取红包”和"查看红包" */
        AccessibilityNodeInfo node1 = (sharedPreferences.getBoolean("pref_watch_self", false)) ?
                this.getQQTheLastNode(QQ_OPEN_CH) : this.getQQTheLastNode(QQ_OPEN_CH);
        if (node1 != null) {
            if (this.signature.generateSignature(node1, "qq")) {
                mQQPacketReceived = true;
                mQQReceiveNode = node1;
            }
            return;
        }

        /* 戳开红包，红包已被抢完，遍历节点匹配“已存入余额”和“来晚一步” */
        boolean hasNodes = this.hasOneOfThoseNodes(QQ_LUCK_NEXT_TIME_CH, QQ_DETAIL_CH);
        if (mMutex && eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && hasNodes
                && (currentActivityName.contains(QQ_PACKET_ACTIVITY))) {
            boolean losePacket = this.hasOneOfThoseNodes(QQ_LUCK_NEXT_TIME_CH);
            mMutex = false;
            mQQPacketPicked = false;
            mUnpackCount = 0;
            performGlobalAction(GLOBAL_ACTION_BACK);

            if (losePacket) {
                performGlobalAction(GLOBAL_ACTION_BACK);
                performGlobalAction(GLOBAL_ACTION_HOME);
                if (powerUtil != null) {
                    powerUtil.reenableKeyguard();
                }
            } else {
                //传入自动答谢内容并退出当前会话页面
                signature.replyString = generateReplyString();
                int delayReply = sharedPreferences.getInt(SpUtil.KEY_REPLY_DELAY, 0) * 1000;
                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (signature.replyString != null) {
                            setQQReply();
                            signature.replyString = null;
                        }
                        performGlobalAction(GLOBAL_ACTION_BACK);
                        performGlobalAction(GLOBAL_ACTION_HOME);
                        if (powerUtil != null) {
                            powerUtil.reenableKeyguard();
                        }
                    }
                }, delayReply);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private AccessibilityNodeInfo getQQTheLastNode(String... texts) {
        int bottom = 0;
        AccessibilityNodeInfo lastNode = null;

        for (String text : texts) {
            if (text == null) continue;

            List<AccessibilityNodeInfo> nodes = this.rootNodeInfo.findAccessibilityNodeInfosByText(text);
            if (nodes != null && nodes.size() > 0) {
                AccessibilityNodeInfo node = nodes.get(nodes.size() - 1);
                Rect bounds = new Rect();
                node.getBoundsInScreen(bounds);
                if (bounds.bottom > bottom) {
                    bottom = bounds.bottom;
                    lastNode = node;
                    signature.others = text.equals(QQ_OPEN_CH);
                }
            }
        }
        return lastNode;
    }

    private void openAliPacket(AccessibilityEvent event) {
//        this.rootNodeInfo = getRootInActiveWindow();
//        if (rootNodeInfo == null) return;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private boolean hasOneOfThoseNodes(String... texts) {
        for (String text : texts) {
            if (text == null) continue;
            List<AccessibilityNodeInfo> nodes = this.rootNodeInfo.findAccessibilityNodeInfosByText(text);
            if (!nodes.isEmpty()) return true;
        }
        return false;
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setWxReply() {
        try {
            AccessibilityNodeInfo outNode = getRootInActiveWindow().getChild(0).getChild(0);
            AccessibilityNodeInfo nodeToInput = outNode.getChild(outNode.getChildCount() - 1).getChild(0).getChild(1);
            if ("android.widget.EditText".equals(nodeToInput.getClassName())) {
                Bundle arguments = new Bundle();
                arguments.putCharSequence(AccessibilityNodeInfo
                        .ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, signature.replyString);
                nodeToInput.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
            }

            SystemClock.sleep(100);      //主动休眠100ms

            AccessibilityNodeInfo outNode1 = getRootInActiveWindow().getChild(0).getChild(0);
            AccessibilityNodeInfo nodeToSend = outNode1.getChild(outNode1.getChildCount() - 1).getChild(0).getChild(3);
            if ("android.widget.Button".equals(nodeToSend.getClassName())) {
                if ("发送".equals(nodeToSend.getText().toString()))
                    nodeToSend.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }

        } catch (Exception e) {
            // Not support
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setQQReply() {
        try {
            AccessibilityNodeInfo outNode = getRootInActiveWindow().getChild(6);
            AccessibilityNodeInfo nodeToInput = outNode.getChild(0);
            if ("android.widget.EditText".equals(nodeToInput.getClassName())) {
                Bundle arguments = new Bundle();
                arguments.putCharSequence(AccessibilityNodeInfo
                        .ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, signature.replyString);
                nodeToInput.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
            }

            SystemClock.sleep(1000);

            AccessibilityNodeInfo outNode1 = getRootInActiveWindow().getChild(0).getChild(0);
            AccessibilityNodeInfo nodeToSend = outNode1.getChild(1);
            if ("android.widget.Button".equals(nodeToSend.getClassName())) {
                if ("发送".equals(nodeToSend.getText().toString()))
                    nodeToSend.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }

        } catch (Exception e) {
            // Not support
        }
    }


    public void setWxCurrentActivityName(AccessibilityEvent event) {
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)
            return;

        try {
            ComponentName componentName = new ComponentName(
                    event.getPackageName().toString(),
                    event.getClassName().toString());
            getPackageManager().getActivityInfo(componentName, 0);
            currentActivityName = componentName.flattenToShortString();
        } catch (Exception e) {
            currentActivityName = WECHAT_PACKET_GENERAL_ACTIVITY;
        }
    }

    public void setQQCurrentActivityName(AccessibilityEvent event) {
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)
            return;

        try {
            ComponentName componentName = new ComponentName(
                    event.getPackageName().toString(),
                    event.getClassName().toString());
            getPackageManager().getActivityInfo(componentName, 0);
            currentActivityName = componentName.flattenToShortString();
        } catch (Exception e) {
            currentActivityName = WECHAT_PACKET_GENERAL_ACTIVITY;
        }
    }


    private String generateReplyString() {
        if (!signature.others) return null;

        Boolean needComment = sharedPreferences.getBoolean("pref_reply_switch", false);
        if (!needComment) return null;

        String[] wordsArray = sharedPreferences.getString("pref_reply_words", "").split(" +");
        if (wordsArray.length == 0) return null;

        Boolean atSender = sharedPreferences.getBoolean("pref_reply_at", false);
        if (atSender) {
            return "@" + signature.sender + " " + wordsArray[(int) (Math.random() * wordsArray.length)];
        } else {
            return wordsArray[(int) (Math.random() * wordsArray.length)];
        }
    }
}
