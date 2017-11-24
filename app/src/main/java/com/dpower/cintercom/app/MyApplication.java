package com.dpower.cintercom.app;

import android.app.Application;
import android.content.Context;
import android.telephony.TelephonyManager;

import com.dpower.cintercom.domain.DeviceInfoMod;

import java.util.List;

import cn.jpush.android.api.JPushInterface;

public class MyApplication extends Application {
    public static final int MAX_SEND_LENGTH = 400; //发送数据的最大长度
    public static final boolean SHOW_LOG = true; //是否打开打印
    public static List<DeviceInfoMod> deviceList = null;

    private static String uuid;

    @Override
    public void onCreate() {
        super.onCreate();
        JPushInterface.init(this);
    }

    public String getUUID(Context context) {
        if (uuid == null) {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            uuid = tm.getDeviceId();
        }
        return uuid;
    }
}
