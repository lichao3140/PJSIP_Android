package com.dpower.cintercom.util;

import android.os.Environment;

import com.dpower.utils.MyFunction;

import java.io.File;

/**
 * 路径工具
 */

public class PathUtil {
    public static String getFilePath(String folder) {
        String path = (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED))
                ?(Environment.getExternalStorageDirectory().getAbsolutePath()):"/mnt/sdcard";//保存到SD卡
        DPLog.println("file path: " + path);
        String filepath = path + "/CIntercom/";
        if(folder != null)
            filepath = filepath + folder;
        File f = new File(filepath);
//        DPLog.println("get file path " + filepath + ", f.path= "
//                + f.getAbsolutePath() + ", f.name=" + f.getName());
        if (!f.exists()) {
            f.mkdirs();
        }
        return filepath;
    }

    public static String getCacheImageNameTmp(String key) {
        String ret = getFilePath("cache/") + key.replace("/", "-");
        ret = ret.replace(" ", "-");
        ret = ret.replace(",", "");
        ret = ret.replace(":", "_");
//        DPLog.println("get getCacheImageNameTmp " + ret);
        return ret;
    }

    public static String getImageNameTmp(String key) {
        String ret = getFilePath("tmp/") + key.replace("/", "-");
        ret = ret.replace(" ", "-");
        ret = ret.replace(",", "");
        ret = ret.replace(":", "_");
//        DPLog.println("get getImageNameTmp " + ret);
        return ret;
    }

    public static String getImageName(String key) {
        String ret  = null;
        if (key != null) {
            ret = getFilePath("d_images/" + key + "/")  + MyFunction.GetCurrentTime() + ".jpg";
        } else {
            ret = getFilePath("d_images/") + MyFunction.GetCurrentTime() + ".jpg";
        }
        ret = ret.replace(" ", "-");
        ret = ret.replace(",", "");
        ret = ret.replace(":", "_");
//        DPLog.print("xufan", "getImageName=" + ret);
        return ret;
    }

    public static String getImagePath(String key) {
        String ret = null;
        if (key != null) {
            ret = getFilePath("d_images" + File.separator + key);
        } else {
            ret = getFilePath("d_images");
        }
//        DPLog.print("xufan", "getImagePath=" + ret);
        return ret;
    }
}
