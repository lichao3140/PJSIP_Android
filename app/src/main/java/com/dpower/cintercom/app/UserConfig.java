package com.dpower.cintercom.app;

import com.dpower.cintercom.BuildConfig;

public class UserConfig {
    public static final boolean isTestMode = false; // 测试模式

    public static final String APP_IDENTIFIER = BuildConfig.appId; // APP识别码，CI表示公版云对讲，BK表示博科
    public static boolean isCallTransfer = false; // 直呼版
}