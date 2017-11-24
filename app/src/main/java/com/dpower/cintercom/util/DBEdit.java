package com.dpower.cintercom.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.dpower.cintercom.domain.DeviceInfoMod;
import com.dpower.cintercom.domain.History;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DBEdit {

    private static final String DB_NAME = "CIntercom_db.db";
    public static final String DB_TABLE_STATUS = "tblLocalStatus";
    public static final String DB_TABLE_HISTORY = "tblLocalHistory";
    public static final String DB_TABLE_DEVICE = "tblLocalDevice";
    private static SQLiteDatabase db = null;

    private static Object lock = new Object();

    private static void DbOpen(Context context) {
        if (db == null)
            db = SQLiteDatabase.openOrCreateDatabase(context.getFilesDir()
                    + File.separator + DB_NAME, null);
    }

    private static void DbClose() {
        if (db != null && db.isOpen()) {
            db.close();
            db = null;
        }
    }

    public static void init(Context context) {
        try {
            DPLog.println("path: "
                    + Environment.getExternalStorageDirectory().getPath()
                    + ", " + Environment.getDataDirectory().getPath()
                    + File.separator + DB_NAME + ", " + context.getFilesDir());
            db = SQLiteDatabase.openOrCreateDatabase(context.getFilesDir()
                    + File.separator + DB_NAME, null);
        } catch(Exception e) {
            DPLog.println("#e=" + e.toString());
        }

        // create table if not exists
        if (!tabIsExist("tblLocalStatus")) {

            db.execSQL("create table if not exists tblLocalStatus("
                    + "statusname varchar(256) not null,statusvalue varchar(256) not null default 0)");

            DPLog.println("create table success");
        }
        // create table if not exists
        if (!tabIsExist(DB_TABLE_HISTORY)) {
            db.execSQL("create table if not exists " + DB_TABLE_HISTORY + "("
                    + "id int not null, h_from varchar(256) not null,h_time varchar(256) not null, h_status int not null)");
            DPLog.println("create table " + DB_TABLE_HISTORY  + " success");
        }

        if (!tabIsExist(DB_TABLE_DEVICE)) {
            db.execSQL("create table if not exists " + DB_TABLE_DEVICE + "("
                    + "devacc varchar(256) not null, devnote varchar(256) not null, svraddr varchar(256) not null, roomNumber varchar(256) not null, doorMachineNumber varchar(256) not null, isPrimaryDevice int defalut '1', primary key (devacc, roomNumber))");
            DPLog.println("create table " + DB_TABLE_DEVICE + " success");
        }

        if (db != null && db.isOpen()) {
            db.close();
            db = null;
        }
    }

    // 数据库表是否存在
    public static boolean tabIsExist(String tabName) {
        boolean result = false;
        if (tabName == null) {
            return false;
        }
        Cursor cursor = null;
        try {
            String sql = "select count(*) as c from sqlite_master where type ='table' and name ='"
                    + tabName.trim() + "' ";
            DPLog.println(sql);
            cursor = db.rawQuery(sql, null);
            if (cursor.moveToNext()) {
                int count = cursor.getInt(0);
                DPLog.println("cursor: " + cursor.getInt(0));
                if (count > 0) {
                    result = true;
                }
            }
        } catch(Exception e) {
            DPLog.println(e.toString());
        }
        cursor.close();
        return result;
    }

    public static String getStringStatus(Context context, String key) {
        synchronized (lock) {
            DbOpen(context);
            String ret = null;
            String sql = "select statusvalue from " + DB_TABLE_STATUS
                    + " where statusname='" + key + "'";
            DPLog.println(sql);
            Cursor cursor = db.rawQuery(sql, null);
            if (cursor.moveToFirst()) {
                ret = cursor.getString(0).trim();
            }
            cursor.close();
            return ret;
        }
    }

    public static int getIntStatus(Context context, String key) {
        synchronized (lock) {
            DbOpen(context);
            int ret = -1;
            String sql = "select statusvalue from " + DB_TABLE_STATUS
                    + " where statusname='" + key + "'";
            DPLog.println(sql);
            Cursor cursor = db.rawQuery(sql, null);
            if (cursor.moveToFirst()) {
                ret = cursor.getInt(0);
            }
            cursor.close();
            return ret;
        }
    }

    public static void setStatusValue(Context context, String key, String value) {
        synchronized (lock) {
            DbOpen(context);
            if (value != null) {
                String sql = "select statusvalue from " + DB_TABLE_STATUS
                        + " where statusname='" + key + "'";
                Cursor cursor = db.rawQuery(sql, null);
                if (!cursor.moveToFirst()) {
                    sql = "insert into " + DB_TABLE_STATUS + " values('" + key
                            + "','" + value + "')";
                    DPLog.println(sql);
                    db.execSQL(sql);
                } else {
                    sql = "update " + DB_TABLE_STATUS + " set statusvalue='"
                            + value + "' where statusname='" + key + "'";
                    DPLog.println(sql);
                    db.execSQL(sql);
                }
            }
        }
    }

    public static void setStatusValue(Context context, String key, int value) {
        synchronized (lock) {
            DbOpen(context);
//			if (value != 0) {
            String sql = "select statusvalue from " + DB_TABLE_STATUS
                    + " where statusname='" + key + "'";
            Cursor cursor = db.rawQuery(sql, null);
            if (!cursor.moveToFirst()) {
                sql = "insert into " + DB_TABLE_STATUS + " values('" + key
                        + "'," + value + ")";
                DPLog.println(sql);
                db.execSQL(sql);
            } else {
                sql = "update " + DB_TABLE_STATUS + " set statusvalue="
                        + value + " where statusname='" + key + "'";
                db.execSQL(sql);
                DPLog.println(sql);
            }
//			}
        }
    }

    // 获取历史记录列表
    public static List<History> getHistoryList(Context context) {
        List<History> ret = null;
        synchronized (lock) {
            DbOpen(context);
            ret = new ArrayList<>();
            String sql = "select * from " + DB_TABLE_HISTORY;
            DPLog.println(sql);
            Cursor cursor = db.rawQuery(sql, null);
            if (!cursor.moveToFirst()) {
                DPLog.println("there's no history");
                return null;
            }
            do {
                History history = new History();
                try {
                    history.setId(cursor.getInt(0));
                    history.setFrom(cursor.getString(1));
                    history.setTime(Long.parseLong(cursor.getString(2)));
                    history.setState(cursor.getString(3));
                    history.setState(cursor.getString(3));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                DPLog.println("find history " + history.toString());
                ret.add(history);
            } while(cursor.moveToNext());
            cursor.close();
            DbClose();
        }
        return ret;
    }

    // 删除所有历史记录
    public static void removeAllHistory(Context context) {
        synchronized(lock) {
            DbOpen(context);
            String sql = "delete from " + DB_TABLE_HISTORY;
            db.execSQL(sql);
            DPLog.println(sql);
            DbClose();
        }
    }

    // 增加历史记录
    public static void AddHistory(Context context, int id, String from, String time, boolean status) {
        synchronized(lock) {
            DbOpen(context);
            int db_status = status ? 1 : 0;
            String sql = "insert into " + DB_TABLE_HISTORY + " values(" + id + ",'" + from
                    + "', '" + time + "'," + db_status + ")";
            DPLog.println(sql);
            if (sql != null)
                db.execSQL(sql);
            DbClose();
        }
    }

    // 获取设备类列表
    public static List<DeviceInfoMod> getDeviceList(Context context) {
        List<DeviceInfoMod> ret = null;
        synchronized (lock) {
            DbOpen(context);
            ret = new ArrayList<>();
            String sql = "select * from " + DB_TABLE_DEVICE;
            DPLog.println(sql);
            Cursor cursor = db.rawQuery(sql, null);
            if (!cursor.moveToFirst()) {
                DPLog.println("there's no device");
                return null;
            }
            do {
                DeviceInfoMod device = new DeviceInfoMod();
                try {
                    device.setDevnote(cursor.getString(1));
                    device.setDevacc(cursor.getString(0));
                    device.setSvraddr(cursor.getString(2));
                    device.setRoomNumber(cursor.getString(3));
                    device.setDoorMachineNumber(cursor.getString(4));
                    if (0 != cursor.getInt(5))
                        device.setIsPrimaryDevice(true);
                    else
                        device.setIsPrimaryDevice(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                DPLog.println("find device " + device.toString());
                ret.add(device);
            } while(cursor.moveToNext());
            cursor.close();
            DbClose();
        }
        return ret;
    }

    // 删除所有设备
    public static void removeAllDevice(Context context) {
        synchronized(lock) {
            DbOpen(context);
            String sql = "delete from " + DB_TABLE_DEVICE;
            db.execSQL(sql);
            DPLog.println(sql);
            DbClose();
        }
    }

    // 添加设备类
    public static void AddDevice(Context context, DeviceInfoMod device) {
        synchronized(lock){
            DbOpen(context);
            // 删除相同的设备
            String sql = "delete from " + DB_TABLE_DEVICE + " where devacc='" + device.getDevacc() + "' and roomNumber='" +  device.getRoomNumber() + "'";
            DPLog.println(sql);
            if (sql != null)
                db.execSQL(sql);
            int isPrimaryDevice = device.getIsPrimaryDevice() ? 1 : 0;
            sql = "insert into " + DB_TABLE_DEVICE + " values('" + device.getDevacc() + "','" + device.getDevnote() + "','" + device.getSvraddr() + "','" + device.getRoomNumber() + "','" + device.getDoorMachineNumber()  + "','" +  isPrimaryDevice + "')";
            DPLog.println(sql);
            if (sql != null)
                db.execSQL(sql);
            DbClose();
        }
    }

    // 根据acc地址查看是否有该设备
    public static boolean checkDevice(Context context, String devacc) {
        synchronized(lock) {
            devacc = devacc.substring(6);
            DPLog.print("xufan", "devacc=" + devacc);
            DbOpen(context);
            String sql = "select * from " + DB_TABLE_DEVICE + " where devacc='" + devacc + "'";
            DPLog.println(sql);
            Cursor cursor = db.rawQuery(sql, null);
            if (!cursor.moveToFirst()) {
                DPLog.println("there's no device");
                return false;
            }
            do {
                DPLog.print("xufan", devacc + "==" +  cursor.getString(2));
                if (devacc.equals(cursor.getString(2))) {
                    return true;
                }
            } while(cursor.moveToNext());
            cursor.close();
            DbClose();
            return false;
        }
    }

    // 根据acc地址查看是否有该设备
    public static boolean checkDevice(Context context, String devacc, String roomNumber) {
        synchronized(lock) {
            devacc = devacc.substring(6);
            DPLog.print("xufan", "devacc=" + devacc);
            DbOpen(context);
            String sql = "select * from " + DB_TABLE_DEVICE + " where devacc='" + devacc + "' and roomNumber='" + roomNumber + "'";
            DPLog.println(sql);
            Cursor cursor = db.rawQuery(sql, null);
            if (!cursor.moveToFirst()) {
                DPLog.println("there's no device");
                return false;
            }
            do {
                DPLog.print("xufan", devacc + "==" +  cursor.getString(2));
                if (devacc.equals(cursor.getString(2))) {
                    return true;
                }
            } while(cursor.moveToNext());
            cursor.close();
            DbClose();
            return false;
        }
    }

    // 根据设备acc获取设备信息
    public static DeviceInfoMod getDevice(Context context, String devacc) {
        DeviceInfoMod ret = null;
        synchronized (lock) {
            DbOpen(context);
            ret = new DeviceInfoMod();
            String sql = "select * from " + DB_TABLE_DEVICE + " where devacc='" + devacc + "'";
            DPLog.println(sql);
            Cursor cursor = db.rawQuery(sql, null);
            if (!cursor.moveToFirst()) {
                DPLog.println("there's no device");
                return null;
            }

            ret.setDevnote(cursor.getString(1));
            ret.setDevacc(cursor.getString(0));
            ret.setSvraddr(cursor.getString(2));
            ret.setRoomNumber(cursor.getString(3));
            ret.setDoorMachineNumber(cursor.getString(4));
            if (0 != cursor.getInt(5))
                ret.setIsPrimaryDevice(true);
            else
                ret.setIsPrimaryDevice(false);

            cursor.close();
            DbClose();
        }
        return ret;
    }

    // 根据设备acc获取设备信息
    public static DeviceInfoMod getDevice(Context context, String devacc, String roomNumber) {
        DeviceInfoMod ret = null;
        synchronized (lock) {
            DbOpen(context);
            ret = new DeviceInfoMod();
            String sql = "select * from " + DB_TABLE_DEVICE + " where devacc='" + devacc + "' and roomNumber='" + roomNumber + "'";
            DPLog.println(sql);
            Cursor cursor = db.rawQuery(sql, null);
            if (!cursor.moveToFirst()) {
                DPLog.println("there's no device");
                return null;
            }

            ret.setDevnote(cursor.getString(1));
            ret.setDevacc(cursor.getString(0));
            ret.setSvraddr(cursor.getString(2));
            ret.setRoomNumber(cursor.getString(3));
            ret.setDoorMachineNumber(cursor.getString(4));
            if (0 != cursor.getInt(5))
                ret.setIsPrimaryDevice(true);
            else
                ret.setIsPrimaryDevice(false);

            cursor.close();
            DbClose();
        }
        return ret;
    }

    // 修改设备备注
    public static void updateDeviceNote(Context context, String devid, String devnote) {
        synchronized(lock) {
            DbOpen(context);
            String sql = "update " + DB_TABLE_DEVICE + " set devnote='" + devnote + "' where devid='" + devid + "'";
            DPLog.println(sql);
            if (sql != null)
                db.execSQL(sql);
            DbClose();
        }
    }

    // 修改设备WiFi
    public static void updateWiFiSsid(Context context, String devid, String ssid) {
        synchronized(lock) {
            DbOpen(context);
            String sql = "update " + DB_TABLE_DEVICE + " set ssid='" + ssid + "' where devid='" + devid + "'";
            DPLog.println(sql);
            if (sql != null)
                db.execSQL(sql);
            DbClose();
        }
    }

    // 获取房间号列表
    public static List<String> getRoomNumberList(Context context) {
        List<String> ret = null;
        synchronized (lock) {
            DbOpen(context);
            ret = new ArrayList<>();
            String sql = "select roomNumber from " + DB_TABLE_DEVICE;
            DPLog.println(sql);
            Cursor cursor = db.rawQuery(sql, null);
            if (!cursor.moveToFirst()) {
                DPLog.println("there's no device");
                return null;
            }
            do {
                String roomNumber = cursor.getString(0);
                DPLog.println("find roomNumber " + roomNumber);
                ret.add(roomNumber);
            } while(cursor.moveToNext());
            cursor.close();
            DbClose();
        }
        return ret;
    }

    // 根据房间号获取门口机列表
    public static List<DeviceInfoMod> getDeviceListByRoomNumber(Context context, String roomNumber) {
        List<DeviceInfoMod> ret = null;
        synchronized (lock) {
            DbOpen(context);
            ret = new ArrayList<>();
            String sql = "select * from " + DB_TABLE_DEVICE + " where roomNumber='" + roomNumber + "'";
            DPLog.println(sql);
            Cursor cursor = db.rawQuery(sql, null);
            if (!cursor.moveToFirst()) {
                DPLog.println("there's no device");
                return null;
            }
            do {
                DeviceInfoMod device = new DeviceInfoMod();
                try {
                    device.setDevnote(cursor.getString(1));
                    device.setDevacc(cursor.getString(0));
                    device.setSvraddr(cursor.getString(2));
                    device.setRoomNumber(cursor.getString(3));
                    device.setDoorMachineNumber(cursor.getString(4));
                    if (0 != cursor.getInt(5))
                        device.setIsPrimaryDevice(true);
                    else
                        device.setIsPrimaryDevice(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                DPLog.println("find device " + device.toString());
                ret.add(device);
            } while(cursor.moveToNext());
            cursor.close();
            DbClose();
        }
        return ret;
    }

    // 根据房号获取主设备
    public static DeviceInfoMod getPrimaryDevcie(Context context, String roomNumber) {
        DeviceInfoMod ret = null;
        synchronized (lock) {
            DbOpen(context);
            ret = new DeviceInfoMod();
            String sql = "select * from " + DB_TABLE_DEVICE + " where roomNumber='" + roomNumber + "'" + " and isPrimaryDevice=1";
            DPLog.println(sql);
            Cursor cursor = db.rawQuery(sql, null);
            if (!cursor.moveToFirst()) {
                DPLog.println("there's no device");
                return null;
            }

            ret.setDevnote(cursor.getString(1));
            ret.setDevacc(cursor.getString(0));
            ret.setSvraddr(cursor.getString(2));
            ret.setRoomNumber(cursor.getString(3));
            ret.setDoorMachineNumber(cursor.getString(4));
            if (0 != cursor.getInt(5))
                ret.setIsPrimaryDevice(true);
            else
                ret.setIsPrimaryDevice(false);

            cursor.close();
            DbClose();
        }
        return ret;
    }

    // 根据房号删除设备主设备
    public static void removeDeviceByRoomNumber(Context context, String roomNumber) {
        synchronized(lock) {
            DbOpen(context);
            String sql = "delete from " + DB_TABLE_DEVICE + " where roomNumber='" + roomNumber + "'";
            db.execSQL(sql);
            DPLog.println(sql);
            DbClose();
        }
    }
}