package com.dpower.cintercom.domain;

import com.dpower.cintercom.util.DPLog;
import com.dpower.utils.MyUtil;

public class History {
    private int id;
    private String from;
    private String time;
    private String state;

    public History(){}

//    public History(int id, String from, long time, boolean status){
//        this.id = id;
//        this.from = from;
//        this.time = MyUtil.getDeviceTime(time);
//        this.status = status;
//    }

    public History(int id, String from, String time, String state){
        this.id = id;
        this.from = from;
        this.time = time;
        this.state = state;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getFrom() {
        if (state != null)
            return resovleFrom(from);
        return from;
    }
    public void setFrom(String from) {
        this.from = from;
    }
    public String getTime() {
        return time;
    }
    // 时间做修改根据板子时间转换成时区时间
    public void setTime(long time) {
        this.time = MyUtil.getDeviceTime(time);
    }
    public String getState() {
        return state;
    }
    public void setState(String status) {
        this.state = state;
    }


    public String resovleFrom(String from) {
        String str = from.substring(1);
        DPLog.print("xufan", "resovleFrom(History.java:58)-->>" + str);
//        str = QRCodeData.resolveInfo(MainActivity.getInstance(), str);
        if (str.length() != 15)
            return from;
//        str = str.substring(0, 10) + MainActivity.getInstance().getString(R.string.door_machine);
        DPLog.print("xufan", "resvleFrom(History.java:58)-->>" + str);
        return str;
    }
}
