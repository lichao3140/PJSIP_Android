package com.dpower.cintercom.util;

import android.util.Log;

import com.dpower.cintercom.app.MyApplication;

public class DPLog {
    static public void println(String msg) {
        if(!MyApplication.SHOW_LOG){
            return;
        }
        Log.i("CIntercom", msg);
    }

    public static final int LOG_VERBOSE = 0;
    public static final int LOG_DEBUG = 1;
    public static final int LOG_INFO = 2;
    public static final int LOG_WARN = 3;
    public static final int LOG_ERROR = 4;

    public static void print(int flag, String content) {

        if(!MyApplication.SHOW_LOG){
            return;
        }
        String tag = "CIntercom";
        switch (flag) {
            case LOG_VERBOSE:
                Log.v(tag, "" + content);
                break;
            case LOG_DEBUG:
                Log.d(tag, "" + content);
                break;
            case LOG_INFO:
                Log.i(tag, "" + content);
                break;
            case LOG_WARN:
                Log.w(tag, "" + content);
                break;
            case LOG_ERROR:
                Log.e(tag, "" + content);
            default:
                break;
        }
    }

    // 测试log
    public static void print(String tag, String content) {
        if (true) {
            Log.i(tag, content);
        }
    }
}
