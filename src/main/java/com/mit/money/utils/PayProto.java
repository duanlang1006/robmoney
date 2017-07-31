package com.mit.money.utils;

import android.content.Context;

import com.android.dsc.core.DscCoreProto;
import com.android.dsc.core.DscCoreUtils;

import java.util.Map;

/**
 * Created by langduan on 16/3/30.
 */
public class PayProto extends DscCoreProto {
    private static final String TYPE_STR = "pay";
    private static final String PS_VER_STR = "1.0";

    String pay_channel;
    String commodity;

    public PayProto(Context context, String pay_channel, String commodity) {
        super(context,
                DscCoreUtils.getAppMetaString(context, "MIT_APPKEY"),
                TYPE_STR,
                PS_VER_STR,
                DscCoreUtils.getAppMetaString(context, "UMENG_CHANNEL"),
                DscCoreUtils.getDeviceUuid(context),
                DscCoreUtils.getDeviceSwVersion());
        this.pay_channel = pay_channel;
        this.commodity = commodity;
    }

    public String getPay_channel() {
        return pay_channel;
    }

    public void setPay_channel(String pay_channel) {
        this.pay_channel = pay_channel;
    }

    public String getCommodity() {
        return commodity;
    }

    public void setCommodity(String commodity) {
        this.commodity = commodity;
    }

    @Override
    public void fillParams(Map<String, String> map) {
        map.put("pay_channel", pay_channel);
        map.put("commodity",commodity);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        final int prime = 31;
        result = prime * result + ((pay_channel == null) ? 0 : pay_channel.hashCode());
        result = prime * result + ((commodity == null) ? 0 : commodity.hashCode());
        return result;
    }
}
