package com.dpower.cintercom.activity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.view.WindowManager;

import com.dpower.cintercom.R;
import com.dpower.cintercom.SDLMainActivity;
import com.dpower.cintercom.app.UserConfig;
import com.dpower.cintercom.util.DBEdit;

public class LaunchActivity extends BaseActivity {
	private final static int START_TO_MAINACTIVITY = 100; // 跳转到主界面
	private Handler handler = null;
	private SQLiteDatabase db = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_launch);
		DBEdit.init(this);
		handler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case START_TO_MAINACTIVITY:
					startActivity(new Intent(LaunchActivity.this, SDLMainActivity.class));
					finish();
					break;
				}
				super.handleMessage(msg);
			}
		};
		handler.sendEmptyMessageDelayed(START_TO_MAINACTIVITY, 2000);
	}
}
