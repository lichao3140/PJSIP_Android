package com.dpower.cintercom.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import com.dpower.cintercom.R;
import com.dpower.cintercom.SDLMainActivity;
import com.dpower.cintercom.domain.DeviceInfoMod;
import com.dpower.cintercom.util.DBEdit;
import com.dpower.utils.OpenMsg;
import com.dpower.cintercom.util.ShareUtil;
import com.dpower.cintercom.util.TextUtil;
import com.dpower.cintercom.widget.RegionNumberEditText;
import com.dpower.cintercom.widget.TimeSelector;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 二维码授权界面 填写结束时间和开锁次数 用来生成二维码
 * 二维码界面 用来显示生成的二维码
 */
public class QRInfoActivity extends BaseActivity implements OnClickListener {
	private TextView mTvBack;
	private TextView mTvGenerate;
	private TextView mTvEndTime;
	private RegionNumberEditText mEdtUnlockCount;
	private LinearLayout mLayoutInfo;
	private LinearLayout mLayoutQrCode;
	private RelativeLayout mLayoutEndTime;
	private ImageView mIvQR;
	private Button mBtnSaved;
	private Button mBtnShare;
	private ShareUtil mTencent;
	private ShareUtil mWechat;
	
	private boolean isShowQR = false;
	int mYear, mMonth, mDay;
	private TimeSelector timeSelector;
	private Bitmap mBitmap;
	private String picName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // 设置无标题
		setContentView(R.layout.activity_qrinfo);
		init_view();
		mTencent = new ShareUtil(this, ShareUtil.QQ);
		mWechat = new ShareUtil(this, ShareUtil.WECHAT);
	}
	
	private void init_view() {
		mTvBack = (TextView) findViewById(R.id.qrinfo_tv_back);
		mTvBack.setOnClickListener(this);
		mTvGenerate = (TextView) findViewById(R.id.qrinfo_tv_generate);
		mTvGenerate.setOnClickListener(this);
		mTvEndTime = (TextView) findViewById(R.id.qrinfo_tv_endtime);
		mEdtUnlockCount = (RegionNumberEditText) findViewById(R.id.qrinfo_edt_unlockcount);
		mLayoutInfo = (LinearLayout) findViewById(R.id.qrinfo_layout_info);
		mLayoutQrCode = (LinearLayout) findViewById(R.id.qrinfo_layout_qrcode);
		mLayoutEndTime = (RelativeLayout) findViewById(R.id.qrinfo_layout_endtime);
		mLayoutEndTime.setOnClickListener(this);
 		mIvQR = (ImageView) findViewById(R.id.qrinfo_iv_qr);
		mBtnSaved = (Button) findViewById(R.id.qrinfo_btn_saved);
		mBtnSaved.setOnClickListener(this);
		mBtnShare = (Button) findViewById(R.id.qrinfo_btn_share);
		mBtnShare.setOnClickListener(this);
		mTvGenerate.setVisibility(View.VISIBLE);
		mTvGenerate.setText(R.string.qrinfo_generate);
		
		SimpleDateFormat formatter = new SimpleDateFormat ("yyyy-MM-dd HH:mm");
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_WEEK, 1);
		Date start = c.getTime();
		String startDate = formatter.format(start);
		mTvEndTime.setText(startDate);
		
		mEdtUnlockCount.setRegion(10, 1);
		mEdtUnlockCount.setTextWatcher();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.qrinfo_tv_back: // 返回
			if (isShowQR) {
				isShowQR = false;
				mLayoutInfo.setVisibility(View.VISIBLE);
				mTvGenerate.setVisibility(View.VISIBLE);
				mLayoutQrCode.setVisibility(View.GONE);
				mIvQR.setVisibility(View.GONE);
			} else
				onBackPressed();
			break;
		case R.id.qrinfo_layout_endtime: // 选择结束时间
			showDialogTime();
			break;
		case R.id.qrinfo_tv_generate: // 生成二维码
			if (!isShowQR) {
				String unlockcount =  mEdtUnlockCount.getText().toString().trim();
				if (unlockcount == null || TextUtil.isEmpty(unlockcount)) {
					toastShow(this, getString(R.string.qrinfo_unlockcount_is_null));
					return;
				}
				isShowQR = true;
				mLayoutInfo.setVisibility(View.GONE);
				mLayoutQrCode.setVisibility(View.VISIBLE);
				InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);       				  
			    imm.hideSoftInputFromWindow(mEdtUnlockCount.getWindowToken(), 0);
				mTvGenerate.setVisibility(View.GONE);
				mIvQR.setVisibility(View.VISIBLE);
				generateQRCode();
			}
			break;
		case R.id.qrinfo_btn_saved:
			if (mBitmap != null) {
				String dirPath = Environment.getExternalStorageDirectory() + File.separator + "doorphone";
				saveQRToAlbum(dirPath, mBitmap, false);
			} else {
				toastShow(this, getString(R.string.qrinfo_qr_save_fail));
			}
			break;
		case R.id.qrinfo_btn_share:
			showShareDialog();
			break;
		}
	}

	// 日期时间选择器
	private void showDialogTime() {
		SimpleDateFormat formatter = new SimpleDateFormat ("yyyy-MM-dd HH:mm");
		Calendar c = Calendar.getInstance();
		Date start = c.getTime();
		String startDate = formatter.format(start);
		c.add(Calendar.DAY_OF_WEEK, 1);
		Date end = c.getTime();
		String endDate = formatter.format(end);
		timeSelector = new TimeSelector(this, new TimeSelector.ResultHandler() {
			@Override
			public void handle(String time) {
				mTvEndTime.setText(time);
			}
		}, startDate, endDate);
		timeSelector.setIsLoop(false);
		timeSelector.show();
	}

	// 生成二维码
	private void generateQRCode() {
		String endtime = mTvEndTime.getText().toString().trim();
		String unlockcount =  mEdtUnlockCount.getText().toString().trim();
		// 准备二维码数据
		OpenMsg msg = new OpenMsg();
		String dsip = DBEdit.getStringStatus(this, "sip_target");
		DeviceInfoMod device = null;
		device = DBEdit.getDevice(this, dsip, SDLMainActivity.getInstance().mRoomNumber);
		String roomNumber = device.getRoomNumber();
		msg.setRoom(roomNumber);
		String sip = DBEdit.getStringStatus(this, "sip_username");
		msg.setSip(sip);
		// 获取当前时间
		long time = System.currentTimeMillis();
		Date currentTime = new Date(time); // 保存当前时间作为文件名
		SimpleDateFormat formatter = new SimpleDateFormat ("yyyyMMddHHmmss");
		picName = formatter.format(currentTime);
		msg.setId(time);
		endtime = endtime.replace("-", "");
		endtime = endtime.replace(" ", "");
		endtime= endtime.replace(":", "");
		msg.setEndTime(endtime);
		msg.setUnlockCount(unlockcount);
		String str = msg.getMsg();
		showQRCode(str);
	}
	
	private Bitmap createQRCode(String content) throws WriterException {
		// 解决中文显示乱码 添加utf-8转码
		Hashtable hints = new Hashtable();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        int dWidth = wm.getDefaultDisplay().getWidth() * 300 / 540;
		// 生成二维矩阵
		BitMatrix matrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, dWidth, dWidth, hints);
		int width = matrix.getWidth();
		int height = matrix.getHeight();
		// 二维矩阵转一维像素数组
		int[] pixels = new int[width * height];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (matrix.get(x, y)) { // 黑色像素点
					pixels[y * width + x] = 0xff000000;
				} else { // 白色背景，防止保存图片时出现黑色背景
					pixels[y * width + x] = 0xffffffff;
				}
			}
		}
		
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixels, 0,	width, 0, 0, width, height);
		return bitmap;
	}
	
	private void showQRCode(String str) {
        Bitmap bmp = null;
        try {
            if (str != null && !"".equals(str)) {
                bmp = createQRCode(str);
            }
        } catch (WriterException e) {
            e.printStackTrace();
        }
        if (bmp != null) {
            mIvQR.setImageBitmap(bmp);
            mBitmap = bmp;
        }
	}

	// 保存二维码到相册
	private void saveQRToAlbum(String dirPath, Bitmap bm, boolean isCache) {
		String filePath = dirPath + File.separator + picName + ".jpg";
		File dirFile = new File(dirPath);
		if (!dirFile.exists()) {
			dirFile.mkdir();
		}
		File picFile = new File(filePath);
		try {
			FileOutputStream fos = new FileOutputStream(picFile);
			bm.compress(Bitmap.CompressFormat.JPEG, 100, fos);
			Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
			Uri uri = Uri.fromFile(picFile);
			intent.setData(uri);
			sendBroadcast(intent);
			fos.flush();
			fos.close();
			if (!isCache)
				toastShow(this, getString(R.string.qrinfo_qr_save_ok));
		} catch (FileNotFoundException e) {
			if (!isCache)
				toastShow(this, getString(R.string.qrinfo_qr_save_fail));
			e.printStackTrace();
		} catch (IOException e1) {
			if (!isCache)
				toastShow(this, getString(R.string.qrinfo_qr_save_fail));
			e1.printStackTrace();
		}	
	}

	// 显示分享弹框
    private void showShareDialog() { 
        View view = LayoutInflater.from(this).inflate(R.layout.share, null);
        final Dialog dialog = new Dialog(this, R.style.common_dialog);
        dialog.setContentView(view);
        dialog.show();
        OnClickListener listener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.view_share_qq:
						// 分享到QQ
                    	String dirPath = getApplicationContext().getExternalCacheDir() + "";
//                    	String dirPath = getApplicationContext().getCacheDir() + "";
                    	saveQRToAlbum(dirPath, mBitmap, true);
                    	String filePath = dirPath + File.separator + picName + ".jpg";
                    	mTencent.sharePic(filePath);
                        break;
                    case R.id.view_share_wechat:
						// 分享到微信
                    	if (true == mWechat.isWeChatAppInstalled()) {
                    		dirPath = getApplicationContext().getExternalCacheDir() + "";
                    		saveQRToAlbum(dirPath, mBitmap, true);
                    		filePath = dirPath + File.separator + picName + ".jpg";
                    		mWechat.sharePic(filePath);
                    	}
                    	else {
							toastShow(QRInfoActivity.this, getString(R.string.wechat_not_installed));
                    	}
                        break;
                    case R.id.share_cancel:
                        break;
                }
                dialog.dismiss();
            }
        };
        ViewGroup mViewWeixin = (ViewGroup) view.findViewById(R.id.view_share_qq);
        ViewGroup mViewPengyou = (ViewGroup) view.findViewById(R.id.view_share_wechat);
        Button mBtnCancel = (Button) view.findViewById(R.id.share_cancel);
        mViewWeixin.setOnClickListener(listener);
        mViewPengyou.setOnClickListener(listener);
        mBtnCancel.setOnClickListener(listener);

		// 设置相关位置
        Window window = dialog.getWindow();
        window.getDecorView().setPadding(0, 0, 0, 0);
        LayoutParams params = window.getAttributes();
        params.width = LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
    }
    
    @Override
    protected void onDestroy() {
		// 删除缓存图片
//    	deleteFilesByDirectory(getApplicationContext().getCacheDir());
    	deleteFilesByDirectory(getApplicationContext().getExternalCacheDir());
    	super.onDestroy();
    }

	// 删除目录下的文件
    private void deleteFilesByDirectory(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            for (File item : directory.listFiles()) {
                item.delete();
            }
        }
    }
}