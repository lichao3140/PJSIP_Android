package com.dpower.cintercom.domain;

import android.content.Context;
import android.os.Bundle;

import com.dpower.cintercom.R;
import com.dpower.cintercom.app.MyApplication;
import com.dpower.cintercom.app.UserConfig;
import com.dpower.cintercom.util.DPLog;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 二维码数据：
 * 1.解析二维码
 * 2.解析房号信息或者门口机编号
 */

public class QRCodeData {
    public static final String ID_ROOM_MACHINE = "01"; // 转呼版二维码标识
    public static final String ID_DOOR_MACHINE = "02"; // 直呼版二维码标识
    private static final int LENGTH_ID_QRCODE = 2; // 二维码标识的长度
    private static final int LENGTH_ROOM_NUMBER = 4; // 直呼版房号的长度
    private static final int LENGTH_DOOR_NUMBER = 3; // 直呼版门口机编号的长度
    private static final int LENGTH_DOOR_USER = 32; // 直呼版门口机sip账号的长度
    private static final int LENGTH_HEAD_QRCODE = LENGTH_ID_QRCODE + LENGTH_ROOM_NUMBER; // 直呼版二维码头部的长度（除门口机信息）
    private static final int LENGTH_DOOR_DATA = LENGTH_DOOR_NUMBER + LENGTH_DOOR_USER; // 直呼版门口机信息的长度
    private static final int LENGTH_MIX_QRCODE = LENGTH_HEAD_QRCODE + LENGTH_DOOR_DATA; // 直呼版二维码的长度
    private static final int LENGTH_INFO_DISTRICT = 2; // 转呼版房号信息 区 的长度
    private static final int LENGTH_INFO_BUILDING = 2; // 转呼版房号信息 栋 的长度
    private static final int LENGTH_INFO_UNIT = 2; // 转呼版房号信息 单元 的长度
    private static final int LENGTH_INFO_ROOM = 4; // 转呼版房号信息 室 的长度
    public static final int LENGTH_INFO_DATA = LENGTH_ID_QRCODE + LENGTH_INFO_DISTRICT + LENGTH_INFO_BUILDING + LENGTH_INFO_UNIT + LENGTH_INFO_ROOM; // 转呼版房号信息的长度
    private static final int LENGTH_MAX_QRCODE = 24; // 转呼版二维码的长度

    /**
     * 解析二维码数据
     * @param context
     * @param data 二维码数据
     * @return result 解析结果；info 房号信息；user 室内机SIP账号;；room_number 房号；door_number 门口机编号；door_user 门口机SIP账号； number 门口机个数
     */
    public static Bundle resolveQRCode(Context context, String data) {
        Bundle bundle = new Bundle();
        if (UserConfig.isCallTransfer) { // 转呼版
            String svraddr = null;
            if (data.contains("@")) {
                svraddr = data.split("@")[1];
                data = data.split("@")[0];
            }
            if (LENGTH_MAX_QRCODE == data.length()) {
                // 010203040506------------
                DPLog.print("xufan", "resolveQRCode(QRCodeData.java:45)-->>" + "qrcode length= " + data.length());
                if (ID_ROOM_MACHINE.equals(data.substring(0, LENGTH_ID_QRCODE))) {
                    bundle.putBoolean("result", true);
                    bundle.putString("info", resolveInfo(context, data.substring(0, LENGTH_INFO_DATA)));
                    bundle.putString("user", data);
                    if (svraddr != null)
                        bundle.putString("svraddr", svraddr);
                    return bundle;
                }
                bundle.putBoolean("result", false);
            }
        } else { // 直呼版
            if (LENGTH_MIX_QRCODE <= data.length()) {
                if (ID_DOOR_MACHINE.equals(data.substring(0, LENGTH_ID_QRCODE))) {
                    String doorNumber = data.substring(LENGTH_ID_QRCODE, LENGTH_HEAD_QRCODE);
                    bundle.putString("room_number", doorNumber);
                    int number = (data.length() - LENGTH_HEAD_QRCODE) / LENGTH_DOOR_DATA; // 门口机个数
                    int length = LENGTH_HEAD_QRCODE;
                    List<DeviceInfoMod> deviceList = new ArrayList<>();
                    for (int i = 1; i <= number; i++) {
                        String devnote = resolveInfo(context, data.substring(length, length + LENGTH_DOOR_NUMBER));
                        String devacc = data.substring(length + LENGTH_DOOR_NUMBER, length + LENGTH_DOOR_DATA);
//                        String devacc = "019999999999000500017F9C";
                        String doorMachineNumber = data.substring(length, length + LENGTH_DOOR_NUMBER);
                        DeviceInfoMod device = null;
                        if (i == 1)
                            device = new DeviceInfoMod(devnote, devacc, null, doorNumber, doorMachineNumber, true);
                        else
                            device = new DeviceInfoMod(devnote, devacc, null, doorNumber, doorMachineNumber, false);
//                        bundle.putString("door_number" + i, device.getDevnote());
//                        bundle.putString("door_user" + i, device.getDevacc());
                        deviceList.add(device);
                        length += LENGTH_DOOR_DATA;
                    }
                    // 02 1234 001-------------------------------1 002-------------------------------2 003-------------------------------3
                    // TODO 直呼版二维码
                    if (number > 0) {
                        bundle.putBoolean("result", true);
//                        bundle.putInt("number", number);
                        MyApplication.deviceList = deviceList;
                        return bundle;
                    }
                    bundle.putBoolean("result", false);
                }
                bundle.putBoolean("result", false);
            }
        }
        return bundle;
    }

    /**
     * 解析房号信息或者门口机编号
     * @param context
     * @param info
     * @return
     */
    public static String resolveInfo(Context context, String info) {
        String str = info;
        if (isNumeric(info)) {
            if (UserConfig.isCallTransfer) { // 转呼版
                if (LENGTH_INFO_DATA == info.length()) {
                    // 010203040506  02 03 04 0506
                    str = info.substring(LENGTH_ID_QRCODE, LENGTH_ID_QRCODE + LENGTH_INFO_DISTRICT) + context.getString(R.string.district);
                    str += info.substring(LENGTH_ID_QRCODE + LENGTH_INFO_DISTRICT, LENGTH_ID_QRCODE + LENGTH_INFO_DISTRICT + LENGTH_INFO_BUILDING) + context.getString(R.string.building);
                    str += info.substring(LENGTH_ID_QRCODE + LENGTH_INFO_DISTRICT + LENGTH_INFO_BUILDING, LENGTH_ID_QRCODE + LENGTH_INFO_DISTRICT + LENGTH_INFO_BUILDING + LENGTH_INFO_UNIT) + context.getString(R.string.unit);
                    str += info.substring(LENGTH_ID_QRCODE + LENGTH_INFO_DISTRICT + LENGTH_INFO_BUILDING + LENGTH_INFO_UNIT, LENGTH_INFO_DATA) + context.getString(R.string.room);
//                    str += " " + context.getString(R.string.room_machine);
                }
            } else { // 直呼版
                if (LENGTH_DOOR_NUMBER == info.length()) {
                    str = info.substring(1, LENGTH_DOOR_NUMBER) + context.getString(R.string.main_room_number);
                    if (info.substring(0, 1).equals("2")) {
                        str += context.getString(R.string.unit) + context.getString(R.string.door_machine);
                    }
//                    str += " " + context.getString(R.string.door_machine);
                }
            }
        }
        DPLog.print("xufan", "resolveInfo(QRCodeData.java:104)-->> " + str);
        return str;
    }

    /**
     * 判断字符串是否只包含数字
     * @param str
     * @return
     */
    public static boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }
}
