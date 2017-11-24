package com.dpower.cintercom.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;

import com.dpower.cintercom.R;
import com.dpower.cintercom.activity.ImagePagerActivity;
import com.dpower.cintercom.domain.PhotoInfo;
import com.dpower.cintercom.util.DPLog;
import com.dpower.cintercom.widget.MyGridView;

/**
 * 相册图片设配器 用日期来分隔
 */
public class PhotoListAdapter extends BaseAdapter {
	private List<PhotoInfo> photoInfo;
	private LayoutInflater inflater;
	private Context mContext;
	private boolean isEditMode = false;
	private PhotoAdapter adapter;
	private MyGridView gridview;

	private ArrayList<String> imagePaths;
	private ArrayList<String> timeList;
	
	public PhotoListAdapter(Context context) {
		photoInfo = new ArrayList<PhotoInfo>();
		mContext = context;
		inflater = LayoutInflater.from(context);
	}
	
	public void clearAll() {
		photoInfo.clear();
	}
	
	public void addItem(PhotoInfo info) {
		photoInfo.add(info);
	}
	
	@Override
	public int getCount() {
		return photoInfo.size();
	}

	@Override
	public Object getItem(int position) {
		return photoInfo.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	private List<Map<String, Object>> getData(int position) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		PhotoInfo item = photoInfo.get(position);
		for (int i = 0; i < item.getImgPath().size(); i++) {
			Map<String, Object> mp = new HashMap<String, Object>();
			String imgPath =item.getImgPathItem(i);
			mp.put("imgPath", imgPath);
			mp.put("time", item.getTimeItem(i));
			list.add(mp);
		}
		return list;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = inflater.inflate(R.layout.photo_info_list, null);
//		TextView date = (TextView) convertView.findViewById(R.id.photo_date);
		gridview = (MyGridView) convertView.findViewById(R.id.photo_content);
		PhotoInfo item = photoInfo.get(position);
//		date.setText(item.getDate());
		imagePaths = item.getImgPath();
		timeList = item.getTime();
		DPLog.println("getView item.getImgPath().size():" + item.getImgPath().size());
		adapter = new PhotoAdapter(mContext, getData(position));
		gridview.setAdapter(adapter);
		gridview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
//				PhotoInfo pInfo = photoInfo.get(position);
				if (!isEditMode)
					imageBrower(position, imagePaths, timeList);
				else
					adapter.setPicStatus(position, view);
			}
			
		});
		return convertView;
	}
	
    /**
     * 打开图片浏览
     * @param position 起始位置
     * @param urls 绝对路径
     */
	private void imageBrower(int position, ArrayList<String> urls, ArrayList<String> timeList) {
		Intent intent = new Intent(mContext, ImagePagerActivity.class);
		intent.putExtra(ImagePagerActivity.EXTRA_IMAGE_INDEX, position);
		intent.putExtra(ImagePagerActivity.EXTRA_IMAGE_URLS, urls);
		intent.putExtra(ImagePagerActivity.EXTRA_IMAGE_TIMELIST, timeList);
		mContext.startActivity(intent);
	}
	
	public void setEditMode(boolean isEditMode) {
		this.isEditMode = isEditMode;
		adapter.setEditMode(isEditMode, gridview);
	}
	
	public void deleAll() {
		adapter.deleAll();
	}
	
	public void deleSelected() {
		adapter.deleSelected();
	}
	
	public int getPicNum() {
		if (adapter != null) {
			return adapter.getCount();
		} else
			return 0;
	}
}
