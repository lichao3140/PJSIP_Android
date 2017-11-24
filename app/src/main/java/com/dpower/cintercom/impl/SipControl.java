package com.dpower.cintercom.impl;

import android.os.RemoteException;

import com.dpower.cintercom.util.DPLog;
import com.dpower.dpsiplib.Core_Service;
import com.dpower.dpsiplib.message.CIMessageAdapter;

import java.util.HashMap;
import java.util.Map;

public class SipControl {
    /**
     * 绑定
     * @param service
     * @param remoteUser 目标账号
     * @param room  房间号
     * @param appacc 本账号
     * @return
     * @throws RemoteException
     */
    public static boolean bind(Core_Service service, String remoteUser, String room, String appacc) throws RemoteException {
        Map<String, String> map = new HashMap<>();
        map.put("room", room);
        map.put("appacc", appacc);
        DPLog.print("xufan", "bind(SipControl.java:22)-->>" + "bind");
        return service.sendMessage(remoteUser, CIMessageAdapter.FLAG_RESULT_BIND, CIMessageAdapter.MESSAGE_BIND, map, 5000);
    }

    /**
     * 解绑
     * @param service
     * @param remoteUser 目标账号
     * @param appacc 本账号
     * @return
     * @throws RemoteException
     */
    public static boolean unbind(Core_Service service, String remoteUser, String appacc) throws RemoteException {
        Map<String, String> map = new HashMap<>();
        map.put("appacc", appacc);
        DPLog.print("xufan", "unbind(SipControl.java:30)-->>" + "unbind");
        return service.sendMessage(remoteUser, CIMessageAdapter.FLAG_RESULT_UNBIND, CIMessageAdapter.MESSAGE_UNBIND, map, 5000);
    }

    /**
     * 解绑
     * @param service
     * @param remoteUser 目标账号
     * @param appacc 本账号
     * @param room 房号
     * @return
     * @throws RemoteException
     */
    public static boolean unbind(Core_Service service, String remoteUser, String appacc, String room) throws RemoteException {
        Map<String, String> map = new HashMap<>();
        map.put("appacc", appacc);
        map.put("room", room);
        DPLog.print("xufan", "unbind(SipControl.java:30)-->>" + "unbind");
        return service.sendMessage(remoteUser, CIMessageAdapter.FLAG_RESULT_UNBIND, CIMessageAdapter.MESSAGE_UNBIND, map, 5000);
    }

    /**
     * 开锁
     * @param service
     * @param remoteUser 目标账号
     * @param door 大门 or 小门
     * @return
     * @throws RemoteException
     */
    public static boolean openlock(Core_Service service, String remoteUser, String door) throws RemoteException {
        Map<String, String> map = new HashMap<>();
        map.put("door", door);
        return service.sendMessage(remoteUser, CIMessageAdapter.FLAG_RESULT_OPENLOCK, CIMessageAdapter.MESSAGE_OPENLOCK, map, 5000);
    }

    /**
     * 获取安防
     * @param service
     * @param remoteUser 目标账号
     * @param room 房号
     * @return
     * @throws RemoteException
     */
    public static boolean getSafeMode(Core_Service service, String remoteUser, String room) throws RemoteException {
        Map<String, String> map = new HashMap<>();
        map.put("room", room);
        return service.sendMessage(remoteUser, CIMessageAdapter.FLAG_RESULT_GET_SAFEMODE, CIMessageAdapter.MESSAGE_GET_SAFEMODE, map, 5000);
    }

    /**
     * 设置安防
     * @param service
     * @param remoteUser 目标账号
     * @param mode 模式
     * @param room 房号
     * @return
     * @throws RemoteException
     */
    public static boolean setSafeMode(Core_Service service, String remoteUser, String mode, String room) throws RemoteException {
        Map<String, String> map = new HashMap<>();
        map.put("mode", mode);
        map.put("room", room);
        return service.sendMessage(remoteUser, CIMessageAdapter.FLAG_RESULT_SET_SAFEMODE, CIMessageAdapter.MESSAGE_SET_SAFEMODE, map, 0);
    }

    /**
     * 检测设备在线状态
     * @param service
     * @param remoteUser 目标账号
     * @return
     * @throws RemoteException
     */
    public static boolean checkOnline(Core_Service service, String remoteUser) throws RemoteException {
        return service.sendMessage(remoteUser, CIMessageAdapter.FLAG_RESULT_CHECK_ONLINE, CIMessageAdapter.MESSAGE_CHECK_ONLINE, null, 5000);
    }
}
