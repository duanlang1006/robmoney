package com.tencent.mm;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.android.dsc.util.DscLog;
import com.mit.money.utils.PacketData;
import com.mit.money.utils.PacketInfo;
import com.mit.money.utils.SpUtil;
import com.mit.state.ITaskState;
import com.mit.state.StateMachine;
import com.mit.state.Task;
import com.umeng.analytics.MobclickAgent;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hxd on 16-3-9.
 * 拆包红状态
 */
public class MMStatePacketCover implements ITaskState {
    private static final String WECHAT_PACKET_DETAIL_ACTIVITY = "LuckyMoneyDetailUI";
    private static final String WECHAT_PACKET_GENERAL_ACTIVITY = "LauncherUI";
    private static final String WECHAT_BETTER_LUCK_CH = "手慢了";
    private static final String WECHAT_EXPIRES_CH = "红包已失效";

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

        if (!mmTask.isFirstEnterPacket()) {
            mmTask.setFirstEnterPacket(true);
            AccessibilityNodeInfo rootInfo = as.getRootInActiveWindow();
            if (rootInfo == null) {
                DscLog.d(this.getClass().getSimpleName(), "rootInfo is null");
                return;
            }
            AccessibilityNodeInfo openNode = findOpenNode(rootInfo);
            if (openNode == null) {
                //来晚一步，直接执行返回动作，第一次返回关闭当前红包窗口，第二次返回退出当前会话页面
                as.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
            } else if ("android.widget.Button".equals(openNode.getClassName())) {
                openNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        } else if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            //拆开红包成功，进入详情页面
            try {
                if (event.getClassName().toString().contains(WECHAT_PACKET_DETAIL_ACTIVITY)) {
                    //从红包界面跳转到详情页面，说明成功拆到有效红包
                    ((MMTask) task).setUnPacketSuccess(true);
                    task.setNextState(MMStateDetail.class.getName());

                    getAndSavePacketData(as);

                    Map<String, String> map_value = new HashMap<String, String>();
                    map_value.put("SDK_INT", String.valueOf(Build.VERSION.SDK_INT));
                    map_value.put("packageName", "com.tencent.mm");
                    MobclickAgent.onEventValue(context, "RobServiceSuccess", map_value, 1);
                } else if (event.getClassName().toString().contains(WECHAT_PACKET_GENERAL_ACTIVITY)) {
                    as.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                    task.setNextState(MMStateFinish.class.getName());
                    StateMachine.executeState(context, task);
                }
            } catch (Exception e) {
                task.setNextState(MMStateFinish.class.getName());
                StateMachine.executeState(context, task);
            }

        } else if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            boolean hasNodes = this.hasOneOfThoseNodes(as, WECHAT_BETTER_LUCK_CH, WECHAT_EXPIRES_CH);
            if (hasNodes) {
                as.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void getAndSavePacketData(AccessibilityService as) {
        Context context = as.getApplicationContext();
        PacketData packetData = new PacketData(context);
        PacketInfo packetInfo = new PacketInfo();
        AccessibilityNodeInfo nodeInfo = as.getRootInActiveWindow();
        if (nodeInfo == null) return;

        String moneyStr = null;
        int num = (int) SpUtil.getValue(context, SpUtil.KEY_TOTAL_NUM, 0);
        String value = (String) SpUtil.getValue(context, SpUtil.KEY_TOTAL_VALUE, "0.00");
        String name = getName(nodeInfo);
        String time = getTime();
        /* 准备获取红包金额 */
        /* 根据金额中的小数点字符来取金额大小 */
        List<AccessibilityNodeInfo> nodeInfos = nodeInfo.findAccessibilityNodeInfosByText(".");
        if (nodeInfos.size() > 0) {
            for (int i = 0; i < nodeInfos.size(); i++) {
                AccessibilityNodeInfo info = nodeInfos.get(i);
                if (info != null) {
                    moneyStr = info.getText().toString();
                    if (isNumeric(moneyStr))
                        break;
                }
            }
            if (moneyStr != null && !moneyStr.isEmpty() && isNumeric(moneyStr)) {
                /* 成功抢到红包，获取金额，写入存储 */
                double mTotleSize = sum(Double.valueOf(value), Double.valueOf(moneyStr));
                SpUtil.setValue(context, SpUtil.KEY_TOTAL_VALUE, String.valueOf(mTotleSize));
                SpUtil.setValue(context, SpUtil.KEY_TOTAL_NUM, ++num);
                packetInfo.setNo(String.valueOf(num));
                packetInfo.setName(name);
                packetInfo.setSize(String.valueOf(mTotleSize));
                packetInfo.setTime(time);
                packetInfo.setType("微信");
                packetData.add(packetInfo.getNo(), packetInfo.getType(), packetInfo.getName(),
                        packetInfo.getTime(), packetInfo.getSize());
            } else {
                Toast.makeText(context, "统计红包数量失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /* 通过正则表达式判断字符串是否为带小数点的数字 */
    private boolean isNumeric(String str) {
        if (str == null) return false;
        Boolean strResult = str.matches("^[0-9]+\\.{0,1}[0-9]{0,2}$");  //-?[0-9]*.?[0-9]*
        return strResult;
    }

    private double sum(double d1, double d2) {
        BigDecimal bd1 = new BigDecimal(Double.toString(d1));
        BigDecimal bd2 = new BigDecimal(Double.toString(d2));
        return bd1.add(bd2).doubleValue();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private String getName(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) return null;
        String name = null;
        List<AccessibilityNodeInfo> nodeInfos = nodeInfo.findAccessibilityNodeInfosByText("的红包");
        if (nodeInfos.size() > 0) {
            AccessibilityNodeInfo info = nodeInfos.get(0);
            name = info.getText().toString();
        }
        assert name != null;
        if (name.length() > 3)
            return name.substring(0, name.length() - 3);
        else return name;
    }

    private String getTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss ");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        return formatter.format(curDate);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private AccessibilityNodeInfo findOpenNode(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) return null;
        if (nodeInfo.getChildCount() == 0) {
            if ("android.widget.Button".equals(nodeInfo.getClassName()))
                return nodeInfo;
            else
                return null;
        }
        for (int i = 0; i < nodeInfo.getChildCount(); i++) {
            AccessibilityNodeInfo button = findOpenNode(nodeInfo.getChild(i));
            if (button != null) return button;
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private boolean hasOneOfThoseNodes(AccessibilityService as, String... texts) {
        AccessibilityNodeInfo rootNodeInfo = as.getRootInActiveWindow();
        for (String text : texts) {
            if (text == null) continue;
            List<AccessibilityNodeInfo> nodes = rootNodeInfo.findAccessibilityNodeInfosByText(text);
            if (!nodes.isEmpty()) return true;
        }
        return false;
    }

}
