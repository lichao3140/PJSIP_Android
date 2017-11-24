package com.dpower.cintercom.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dpower.cintercom.R;
import com.dpower.cintercom.util.DBEdit;
import com.dpower.cintercom.util.DPLog;
import com.dpower.cintercom.util.PathUtil;
import com.dpower.cintercom.widget.MyGridView;

public class PhotoAdapter extends BaseAdapter {
	private class PhotoMod {
		Photo photo = null;
		boolean isSelected = false;
	}
//	private List<Photo> photos;
	private List<PhotoMod> photos;
	private LayoutInflater inflater;
	private boolean isEditMode = false;
	private Context mContext;
	
	public PhotoAdapter(Context context, List<Map<String, Object>> data) {
//		photos = new ArrayList<Photo>();
		mContext = context;
		photos = new ArrayList<PhotoMod>();
		inflater = LayoutInflater.from(context);
		for (int i = 0; i < data.size(); i++) {
			String imgPath = data.get(i).get("imgPath").toString();
			String time = data.get(i).get("time").toString();
//			Photo photo = new Photo(i, imgPath, time);
//			photos.add(photo);
			PhotoMod mod = new PhotoMod();
			Photo photo = new Photo(i, imgPath, time);
			mod.photo = photo;
			photos.add(mod);
		}
	}
	
	@Override
	public int getCount() {
		return photos.size();
	}

	@Override
	public Object getItem(int position) {
		return photos.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void setIsSelected(int position, boolean isSelected) {
		photos.get(position).isSelected = isSelected;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.photo_info_item, null);
			viewHolder = new ViewHolder();
			viewHolder.image = (ImageView) convertView.findViewById(R.id.photo_image);
			viewHolder.time = (TextView) convertView.findViewById(R.id.photo_time);
			viewHolder.isSelected = (ImageView) convertView.findViewById(R.id.photo_isselected);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
//		viewHolder.image.setImageResource(photos.get(position).getImageId());
//		Bitmap bmp= BitmapFactory.decodeFile(photos.get(position).getImagePath());
		PhotoMod mod = photos.get(position);
		Bitmap bmp= BitmapFactory.decodeFile(mod.photo.getImagePath());
		viewHolder.image.setImageBitmap(bmp);
//		viewHolder.time.setText(photos.get(position).getTime());
		viewHolder.time.setText(mod.photo.getTime());
		if (isEditMode) {
			if (mod.isSelected) {
				viewHolder.isSelected.setImageResource(R.mipmap.png_pic_selected);
			} else {
				viewHolder.isSelected.setImageResource(R.mipmap.png_pic_unselected);
			}
		} else {
			viewHolder.isSelected.setImageResource(-1);
		}
		viewHolder.position = position;
		return convertView;
	}
	
	public void setEditMode(boolean isEditMode, MyGridView gridview) {
		this.isEditMode = isEditMode;
		int end = gridview.getLastVisiblePosition();
		for (int i = gridview.getFirstVisiblePosition(); i <= end; i++) {
			View convertView = gridview.getChildAt(i);
			if (convertView != null) {
				ViewHolder holder = (ViewHolder) convertView.getTag();
				if (holder != null) {
					if (isEditMode) {
						photos.get(holder.position).isSelected = false;
						holder.isSelected.setImageResource(R.mipmap.png_pic_unselected);
					} else {
						holder.isSelected.setImageResource(-1);
					}
				}
			}
		}
	}
	
	public void setPicStatus(int position, View view) {
		ViewHolder holder = (ViewHolder) view.getTag();
		photos.get(position).isSelected = !photos.get(position).isSelected;
		if (photos.get(position).isSelected) {
			holder.isSelected.setImageResource(R.mipmap.png_pic_selected);
		} else{
			holder.isSelected.setImageResource(R.mipmap.png_pic_unselected);
		}
	}
	
	public void deleAll() {
		String device = DBEdit.getStringStatus(mContext, "sip_target");
        String filePath = PathUtil.getImagePath(device);
		deleteDirectory(filePath);
		photos.removeAll(photos);
		notifyDataSetChanged();
	}
	
	public void deleSelected() {
		List<PhotoMod> selected = new ArrayList<PhotoMod>();
		for(int i = 0; i < photos.size(); i++) {
			if (photos.get(i).isSelected) {
				DPLog.print("xufan", "被删除的是第" + (i + 1) + "个");
				deleteFile(photos.get(i).photo.getImagePath());
				selected.add(photos.get(i));
			}
		}
		photos.removeAll(selected);
		DPLog.print("xufan", "deleSelected " + photos.size());
		notifyDataSetChanged();
	}
	
	/**
     * 删除单个文件
     * @param   filePath    被删除文件的文件名
     * @return 文件删除成功返回true，否则返回false
     */
    public boolean deleteFile(String filePath) {
    File file = new File(filePath);
        if (file.isFile() && file.exists()) {
        return file.delete();
        }
        return false;
    }

    /**
     * 删除文件夹以及目录下的文件
     * @param   filePath 被删除目录的文件路径
     * @return  目录删除成功返回true，否则返回false
     */
    public boolean deleteDirectory(String filePath) {
    	boolean flag = false;
        //如果filePath不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        File[] files = dirFile.listFiles();
        //遍历删除文件夹下的所有文件(包括子目录)
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
            //删除子文件
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag) break;
            } else {
            //删除子目录
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag) break;
            }
        }
        if (!flag) return false;
        //删除当前空目录
        return dirFile.delete();
    }
}

class ViewHolder {
	public ImageView image;
	public TextView time;
	public ImageView isSelected;
	
	public int position = -1;
}

class Photo {
	private int imageId;
	private String imagePath;
	private String time;
	
	public Photo() {
		super();
	}
	
	public Photo(int imageId, String imagePath, String time) {
		super();
		this.imageId = imageId;
		this.imagePath = imagePath;
		this.time = time;
	}
	
	public void setImageId(int id) {
		imageId = id;
	}

	public int getImageId() {
		return imageId;
	}
	
	public void setImagePath(String path) {
		imagePath = path;
	}
	
	public String getImagePath() {
		return imagePath;
	}
	
	public void setTime(String time) {
		this.time = time;
	}
	
	public String getTime() {
		return time;
	}
}
