package com.mit.money.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by android on 2/25/16.
 */
public class SpUtil {
    private static final String LUCKY_CONFIG = "rob_config";

    private static final String LUCKY_FIRST = "lucky_first_enterin";

    public static final String KEY_PRAISE = "pref_praise";      //点赞表扬
    public static final String KEY_FEEDBACK = "pref_feedback";      //反馈
    public static final String KEY_VERESION = "pref_version";      //版本

    public static final String KEY_WATCH_WECHAT = "pref_watch_wechat";      //监视微信
    public static final String KEY_WATCH_QQ = "pref_watch_qq";              //监视QQ
    public static final String KEY_WATCH_ALI = "pref_watch_alipay";         //监视支付宝
    public static final String KEY_OPEN_PACKET = "pref_open_packet";        //拆开红包
    public static final String KEY_OPEN_DELAY = "pref_open_delay";          //延时拆开红包
    public static final String KEY_REPLY_DELAY = "pref_reply_delay";        //延时答谢

    public static final String KEY_FIRST_ENTER_IN = "first_enter_in";       //首次进入应用
    public static final String KEY_ROB_PAY_TITLE = "rob_pay_title";       //详情页面打赏上面显示字符
    public static final String KEY_PAYMENT_SUCCESS = "RobPaySuccess";
    public static final String KEY_PAYMENT_FAIL = "RobPayFail";
    public static final String KEY_PAYMENT_CANCEL = "RobPayCancel";
    public static final String KEY_PAYMENT_INVALID = "RobPayInvalid";
    public static final String KEY_PAYMENT_WEB_NAME = "RobWebName";
    public static final String KEY_PAYMENT_WEB_URL = "RobWebUrl";

    public static final String KEY_SHARE = "rob_share";
    public static final String KEY_SUPPORT = "function_support";
    public static final String KEY_TOTAL_NUM = "detail_number";
    public static final String KEY_TOTAL_VALUE = "detail_value";

    public static final String ONLINE_KEY_SHARE_URL = "share_url";       //分享链接
    public static final String ONLINE_KEY_ALARM_TIME = "alarm_time";       //唤醒时间
    public static final String ONLINE_KEY_ALERT_INFO = "alert_info";       //通知内容
    public static final String ONLINE_KEY_ENTER_RUL = "enter_url";       //首次进入读取的url

    public static void setValue(Context context, String key, Object object) {
        SharedPreferences sp = context.getSharedPreferences(SpUtil.LUCKY_CONFIG, 0);
        SharedPreferences.Editor editor = sp.edit();
        if (object instanceof String) {
            editor.putString(key, (String) object);
        } else if (object instanceof Integer) {
            editor.putInt(key, (Integer) object);
        } else if (object instanceof Boolean) {
            editor.putBoolean(key, (Boolean) object);
        } else if (object instanceof Float) {
            editor.putFloat(key, (Float) object);
        } else if (object instanceof Long) {
            editor.putLong(key, (Long) object);
        } else {
            editor.putString(key, object.toString());
        }
        editor.apply();
    }

    public static Object getValue(Context context, String key, Object object) {
        SharedPreferences sp = context.getSharedPreferences(SpUtil.LUCKY_CONFIG, 0);
        if (object instanceof String) {
            return sp.getString(key, (String) object);
        } else if (object instanceof Integer) {
            return sp.getInt(key, (Integer) object);
        } else if (object instanceof Boolean) {
            return sp.getBoolean(key, (Boolean) object);
        } else if (object instanceof Float) {
            return sp.getFloat(key, (Float) object);
        } else if (object instanceof Long) {
            return sp.getLong(key, (Long) object);
        } else {
            return null;
        }
    }

}
