package com.dpower.cintercom.activity;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dpower.cintercom.R;
import com.dpower.cintercom.adapter.PhotoListAdapter;
import com.dpower.cintercom.domain.PhotoInfo;
import com.dpower.cintercom.util.DBEdit;
import com.dpower.cintercom.util.DPLog;
import com.dpower.cintercom.util.PathUtil;

/**
 * 相册
 */

public class PhotoMessageActivity extends BaseActivity implements OnClickListener {
	private TextView mTvBack;
	private TextView mTvEdit;
	private ListView listView;
	private PhotoListAdapter adapter;
	private RelativeLayout mLayoutConsole;
	private TextView mTvDeleAll;
	private TextView mTvDele;
	
	private boolean isShowConsole = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_photomessage);
		init_view();

		updateInfo();
	}
	
	private void init_view() {
		mTvBack = (TextView) findViewById(R.id.photomessage_tv_back);
		mTvBack.setOnClickListener(this);
		mTvEdit = (TextView) findViewById(R.id.photomessage_tv_edit);
		mTvEdit.setOnClickListener(this);
		
		listView = (ListView) findViewById(R.id.photo_list);
		
		mLayoutConsole = (RelativeLayout) findViewById(R.id.photomessage_layout_console);
		mTvDeleAll = (TextView) findViewById(R.id.photomessage_tv_deleall);
		mTvDeleAll.setOnClickListener(this);
		mTvDele = (TextView) findViewById(R.id.photomessage_tv_deleselected);
		mTvDele.setOnClickListener(this);
 	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.photomessage_tv_back:
			finish();
			break;
		case R.id.photomessage_tv_edit: //编辑
			if (isShowConsole)
				finishEdit();
			else
				startEdit();
			break;
		case R.id.photomessage_tv_deleall: //全部删除
			adapter.deleAll();
			finishEdit();
			updateInfo();
			break;
		case R.id.photomessage_tv_deleselected: //删除
			adapter.deleSelected();
			finishEdit();
			updateInfo();
			break;
		}
	}
	
	/**
     * 从sd卡获取图片资源
     * @return
     */
    private List<String> getImagePathFromSD() {
        // 图片列表
        List<String> imagePathList = new ArrayList<String>();
        String device = DBEdit.getStringStatus(this, "sip_target");
        String filePath = PathUtil.getImagePath(device);
        // 得到该路径文件夹下所有的文件
        File fileAll = new File(filePath);
        File[] files = fileAll.listFiles();
        try {
	        // 将所有的文件存入ArrayList中,并过滤所有图片格式的文件
	        for (int i = 0; i < files.length; i++) {
	            File file = files[i];
	            if (checkIsImageFile(file.getPath())) {
	                imagePathList.add(file.getPath());
	            }
	        }
	        // 返回得到的图片列表
	        return imagePathList;
        } catch (Exception e) {
        	return null;
        }
    }
  
    /**
     * 检查扩展名，得到图片格式的文件
     * @param fName  文件名
     * @return
     */
    private boolean checkIsImageFile(String fName) {
        boolean isImageFile = false;
        // 获取扩展名
        String FileEnd = fName.substring(fName.lastIndexOf(".") + 1, fName.length()).toLowerCase();
        if (FileEnd.equals("jpg") || FileEnd.equals("png") || FileEnd.equals("gif")
                || FileEnd.equals("jpeg")|| FileEnd.equals("bmp") ) {
            isImageFile = true;
        } else {
            isImageFile = false;
        }
        return isImageFile;
    }
    
    private void startEdit() {
    	DPLog.print("xufan", "pic num" + adapter.getPicNum());
    	if (adapter.getPicNum() > 0) {
	    	mLayoutConsole.setVisibility(View.VISIBLE);
	    	mTvEdit.setText(R.string.devicelist_cancel);
	    	adapter.setEditMode(true);
	    	isShowConsole = true;
    	} 
    }
    
    private void finishEdit() {
    	mLayoutConsole.setVisibility(View.GONE);
    	mTvEdit.setText(R.string.devicelist_edit);
    	adapter.setEditMode(false);
    	isShowConsole = false;
    	
    	if (adapter.getPicNum() > 0) {
    		mTvEdit.setEnabled(true);
    	} else {
    		mTvEdit.setEnabled(false);
    	}
    }

	public class FileComparator implements Comparator<String> {
		public int compare(String file1, String file2) {
			if(file1.compareTo(file2) < 0) {
				return -1;
			} else {
				return 1;
			}
		}
	}

	private void updateInfo() {
		List<String> pathList = getImagePathFromSD();
		if (pathList == null)
			return;
		adapter = new PhotoListAdapter(this);
		PhotoInfo photoInfo = new PhotoInfo();
		// 文件排序
		Collections.sort(pathList, new FileComparator());
		for (int i = 0; i < pathList.size(); i++) {
			String path = pathList.get(i);
			photoInfo.addImgPath(path);
			photoInfo.addImgId(i + "");

			DPLog.print("xufan", "i=" + i + " path=" + path);

			int end = path.indexOf(".");
			int start = end - 14;
			if (start >= 0) {
				// 截取文件名中的时间字段
				String time = path.substring(start, end);
				DPLog.print("xufan", "time=" + time);
				try {
					// 转换显示格式
					SimpleDateFormat sfOld = new SimpleDateFormat("yyyyMMddHHmmss");
					Date date = sfOld.parse(time);
					SimpleDateFormat sfNew = new SimpleDateFormat("yyyy.MM.dd  HH:mm:ss");
					time = sfNew.format(date);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				photoInfo.addTime(time);
			}
		}
		if (pathList.size() > 0) {
			adapter.addItem(photoInfo);
			listView.setAdapter(adapter);
			mTvEdit.setEnabled(true);
		} else {
			mTvEdit.setEnabled(false);
			mTvEdit.setVisibility(View.GONE);
		}
	}
}
