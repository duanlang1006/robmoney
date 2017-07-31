package com.mit.money.card;

import android.content.Context;

import com.android.dsc.core.DscCoreProto;
import com.android.dsc.core.DscCoreUtils;

import java.util.Map;

/**
 * Created by hxd on 16-1-20.
 */
public class FuliCardProto extends DscCoreProto {
    private final static String TYPE_STR = "fuli_card_request";
    private final static String PS_VER_STR = "1.0";

    private long whichDay;
    private long endDay;
    private String location;

    public FuliCardProto(Context context, long start, long end, String location) {
        super(context,
                DscCoreUtils.getAppMetaString(context, "MIT_APPKEY"),
                TYPE_STR, PS_VER_STR,
                DscCoreUtils.getAppMetaString(context, "UMENG_CHANNEL"),
                DscCoreUtils.getDeviceUuid(context),
                DscCoreUtils.getDeviceSwVersion());
        this.whichDay = start;
        this.endDay = end;
        this.location = location;
    }

    @Override
    public void fillParams(Map<String, String> map) {
        map.put("which_day", String.valueOf(whichDay));
        map.put("end_day", String.valueOf(endDay));
        map.put("location", (null == location) ? "" : location);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        final int prime = 31;
        result = prime * result + (int) (whichDay ^ (whichDay >>> 32));
        result = prime * result + (int) (endDay ^ (endDay >>> 32));
        result = prime * result + ((location == null) ? 0 : location.hashCode());
        return result;
    }
}
