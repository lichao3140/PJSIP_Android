package com.dpower.cintercom.domain;

import android.content.Context;

import com.dpower.cintercom.util.DBEdit;
import com.dpower.cintercom.util.DPLog;

/**
 * 绑定后的设备类
 */

public class DeviceInfoMod {
    private String devnote = null; // 设备备注
    private String devacc = null; // 设备账号
    private String svraddr = null; /// 服务器地址
    private String roomNumber = null; // 房号
    private String doorMachineNumber = null; // 门口机编号
    private int isPrimaryDevice = 0; // 1为主门口机设备

    public DeviceInfoMod() {
        super();
    }

    public DeviceInfoMod(String devnote, String devacc, String svraddr) {
        super();
        this.devnote = devnote;
        this.devacc = devacc;
        this.svraddr = svraddr;
        roomNumber = "0000";
        isPrimaryDevice = 0;
    }

    public DeviceInfoMod(String devnote, String devacc, String svraddr, String roomNumber, String doorMachineNumber, boolean isPrimaryDevice) {
        super();
        this.devnote = devnote;
        this.devacc = devacc;
        this.svraddr = svraddr;
        this.roomNumber = roomNumber;
        this.doorMachineNumber = doorMachineNumber;
        if (isPrimaryDevice)
            this.isPrimaryDevice = 1;
        else
            this.isPrimaryDevice = 0;
    }

    public void setDevnote(String devnote) {
        this.devnote = devnote;
    }
    public String getDevnote() {
        return devnote;
    }

    public void setDevacc(String devacc) {
        this.devacc = devacc;
    }
    public String getDevacc() {
        return devacc;
    }

    public void setSvraddr(String svraddr) {
        this.svraddr = svraddr;
    }
    public String getSvraddr() {
        return svraddr;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }
    public String getRoomNumber() {
        return roomNumber;
    }

    public void setDoorMachineNumber(String doorMachineNumber) {
        this.doorMachineNumber = doorMachineNumber;
    }
    public String getDoorMachineNumber() {
        return doorMachineNumber;
    }
    public void setIsPrimaryDevice(boolean isPrimaryDevice) {
        if (isPrimaryDevice)
            this.isPrimaryDevice = 1;
        else
            this.isPrimaryDevice = 0;
    }
    public boolean getIsPrimaryDevice() {
        if (isPrimaryDevice == 0)
            return false;
        else
            return true;
    }

    public void setDevnoteByDevacc(Context context, String devacc) {
        this.devnote = resolveDevaccGetDevnote(context, devacc);
    }

    // 检查设备信息 并保存配置
    public void checkAll(Context context) {
        DPLog.print("xufan", "DeviceInfoMode:");
        DPLog.print("xufan", "---devnote:	        " + devnote);
        DPLog.print("xufan", "---devacc:	        " + devacc);
        DPLog.print("xufan", "---svraddr:           " + svraddr);
        DPLog.print("xufan", "---roomNumber:	    " + roomNumber);
        DPLog.print("xufan", "---doorMachineNumber: " + doorMachineNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof DeviceInfoMod) {
            DeviceInfoMod mod = (DeviceInfoMod) o;
            return mod.devacc != null && mod.roomNumber !=null && mod.devacc.equals(devacc) && mod.roomNumber.equals(roomNumber);
        }
        return super.equals(o);
    }

    // 直呼版不能通过账号来得到备注，但是转呼版可以通过账号来得到备注
    public String resolveDevaccGetDevnote(Context context, String devacc) {
        return QRCodeData.resolveInfo(context, devacc);
    }
}