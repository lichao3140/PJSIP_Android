package com.dpower.cintercom.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dpower.cintercom.R;
import com.dpower.cintercom.app.UserConfig;
import com.dpower.cintercom.domain.DeviceInfoMod;
import com.dpower.cintercom.domain.QRCodeData;
import com.dpower.cintercom.util.DBEdit;
import com.dpower.cintercom.util.DPLog;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingActivity extends BaseActivity implements OnClickListener {
	private TextView mTvBack = null;
	private EditText mEdtDevnote = null;
	private EditText mEdtDevid = null;
	private RelativeLayout mLayoutDevnote = null;
	private RelativeLayout mLayoutHistory = null;
	private RelativeLayout mLayoutPhoto = null;
	private RelativeLayout mLayoutQrCode = null;
	
	private MyHandler handler;
	private String remoteUser;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_setting);
		init_view();
		handler = new MyHandler();

		Intent intent = getIntent();
		remoteUser = intent.getStringExtra("remoteUser");
		if (remoteUser != null) {
			mEdtDevid.setText(remoteUser);
			DBEdit.setStatusValue(this, "sip_target", remoteUser);
			if (remoteUser.length() == 24) {
				DeviceInfoMod dev = new DeviceInfoMod();
				remoteUser = dev.resolveDevaccGetDevnote(this, remoteUser.substring(0, QRCodeData.LENGTH_INFO_DATA));
			} else if (remoteUser.length() == 32) {
				List<DeviceInfoMod> list = DBEdit.getDeviceList(this);
				if (list != null && list.size() > 0) {
					for (int i = 0; i < list.size(); i++) {
						if (remoteUser.equals(list.get(i).getDevacc())) {
							String user = list.get(i).getDevnote();
							mEdtDevnote.setText(user);
							mEdtDevnote.setEnabled(false);
							return;
						}
					}
				}
			}
			mEdtDevnote.setText(remoteUser);
			Log.e("xufan", "--> " + remoteUser);
			mEdtDevnote.setEnabled(false);
		}
	}
	
	private void init_view() {
		mTvBack = (TextView) findViewById(R.id.setting_tv_back);
		mTvBack.setOnClickListener(this);
		
		mEdtDevid = (EditText) findViewById(R.id.setting_edt_devid);
		mEdtDevnote = (EditText) findViewById(R.id.setting_edt_devnote);
//		mEdtDevnote.setFilters(new InputFilter[] {chineseFilter()});
		
		mLayoutHistory = (RelativeLayout) findViewById(R.id.setting_layout_history);
		mLayoutHistory.setOnClickListener(this);
		mLayoutPhoto = (RelativeLayout) findViewById(R.id.setting_layout_photo);
		mLayoutPhoto.setOnClickListener(this);

		mLayoutQrCode = (RelativeLayout) findViewById(R.id.setting_layout_qrcode);
		mLayoutQrCode.setVisibility(View.VISIBLE);
		mLayoutQrCode.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.setting_tv_back:
			finish();
			break;
		case R.id.setting_layout_history:
//			Intent intent = new Intent(this, HistoryActivity.class);
//			startActivity(intent);
			break;
		case R.id.setting_layout_photo:
			Intent intent = new Intent(this, PhotoMessageActivity.class);
			startActivity(intent);
			break;
		case R.id.setting_layout_qrcode:
			intent = new Intent(this, QRInfoActivity.class);
			startActivity(intent);
			break;
		}
	}
	
	class  MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {

			default:
				break;
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	private InputFilter chineseFilter() {
		return new InputFilter() {

			@Override
			public CharSequence filter(CharSequence source, int start, int end,
					Spanned dest, int dstart, int dend) {
				if (isAllChinese(source.toString() + dest.toString())) {
	                if (source.toString().length() + dest.toString().length() > 10) {
	                    return "";
	                } else {
	                    return source;
	                }
				} else {
					if (source.toString().length() + dest.toString().length() > 24) {
	                    return "";
	                } else {
	                    return source;
	                }
				}
			}
			
			private boolean isAllChinese(String text) {
				// 包含中文
				Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
				Matcher m = p.matcher(text);
				if (m.find()) {
					return true;
				} else
					return false;
				// 全中文
//				String reg = "[\\u4e00-\\u9fa5]+";
//				return text.matches(reg);
			}
    	};
   }
}
