package com.dpower.cintercom.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dpower.cintercom.R;
import com.dpower.cintercom.app.UserConfig;
import com.dpower.cintercom.util.DBEdit;
import com.dpower.cintercom.util.DPLog;

import scanner.CaptureActivity;

/**
 * 选择模式
 */
public class ChooseModeActivity extends BaseActivity implements OnClickListener {
	private RelativeLayout mLayoutQR = null;
	private TextView mTvBack = null;
	
	public static ChooseModeActivity instance = null;
	public static final int MSG_START_CAPTURE = 1001;

    private MyHandler myHandler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_choosemode);
		init_view();
		instance = this;
		myHandler = new MyHandler();
	}

	private void init_view() {
		mLayoutQR = (RelativeLayout) findViewById(R.id.choosemode_layout_qr);
		mLayoutQR.setOnClickListener(this);
		
		mTvBack = (TextView) findViewById(R.id.choosemode_tv_back);
		mTvBack.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.choosemode_layout_qr:
			myHandler.sendEmptyMessage(MSG_START_CAPTURE);
			break;
		case R.id.choosemode_tv_back:
			finish();
			break;
		}
	}
	
	private class MyHandler extends  Handler {
		 @Override
	        public void handleMessage(Message msg) {
		        switch (msg.what) {
					case MSG_START_CAPTURE:
						Intent intent = new Intent(instance, CaptureActivity.class);
						startActivity(intent);
						break;
				}
	        }
	}

	public Handler getHandler() {
		return myHandler;
	}
    
    @Override
    protected void onDestroy() {
		instance = null;
    	super.onDestroy();
    	DPLog.print("xufan", "choosemode ondestroy");
    }
}
