package com.dpower.cintercom.domain;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

/**
 * 相册，显示信息有缩略图、日期、时间
 */

public class PhotoInfo {
    private ArrayList<String> imgId; // 图片的id
    private ArrayList<String> imgPath; // 图片路径
    private List<Bitmap> bitmap; // 缩略图数据
    private String date; // 日期
    private ArrayList<String> time; // 时间

    public PhotoInfo() {
        imgId = new ArrayList<String>();
        imgPath = new ArrayList<String>();
        bitmap = new ArrayList<Bitmap>();
        date = null;
        time = new ArrayList<String>();
    }

    public void addImgId(String id) {
        imgId.add(id);
    }

    public ArrayList<String> getImgId() {
        return imgId;
    }

    public String getImgIdItem(int pos) {
        return imgId.get(pos);
    }

    public void addImgPath(String path) {
        imgPath.add(path);
    }

    public ArrayList<String> getImgPath() {
        return imgPath;
    }

    public String getImgPathItem(int pos) {
        return imgPath.get(pos);
    }

    public void addBitmap(Bitmap bmp) {
        bitmap.add(bmp);
    }

    public List<Bitmap> getBitmap() {
        return bitmap;
    }

    public Bitmap getBitmapItem(int pos) {
        return bitmap.get(pos);
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void addTime(String time) {
        this.time.add(time);
    }

    public ArrayList<String> getTime() {
        return time;
    }

    public String getTimeItem(int pos) {
        return time.get(pos);
    }
}
