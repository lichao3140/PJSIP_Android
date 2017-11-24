package com.dpower.cintercom.activity;

import android.annotation.SuppressLint;;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.dpower.cintercom.R;
import com.dpower.cintercom.app.UserConfig;
import com.dpower.cintercom.domain.DeviceInfoMod;
import com.dpower.cintercom.domain.QRCodeData;
import com.dpower.cintercom.impl.SipControl;
import com.dpower.cintercom.util.DBEdit;
import com.dpower.cintercom.util.DPLog;
import com.dpower.cintercom.util.PathUtil;
import com.dpower.cintercom.util.TaskStackUtil;
import com.dpower.dpsiplib.BinderFactoryAIDL;
import com.dpower.dpsiplib.BinderPool;
import com.dpower.dpsiplib.CallConfig;
import com.dpower.dpsiplib.CoreService;
import com.dpower.dpsiplib.Core_Service;
import com.dpower.dpsiplib.SipCallback;
import com.dpower.dpsiplib.SipClient;
import com.dpower.dpsiplib.SipUser;
import com.dpower.dpsiplib.bean.AIDLParamBean;
import com.dpower.dpsiplib.message.CIMessageAdapter;
import com.dpower.dpsiplib.service.DPSipService;
import com.dpower.dpsiplib.utils.MediaPlayerUtil;
import com.dpower.dpsiplib.utils.VibratorUtil;
import com.google.gson.Gson;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class VideoActivity extends BaseActivity implements OnClickListener, SensorEventListener {
	public static VideoActivity instance = null;

	private TextView mTvBack = null;
	private TextView mTvTitle = null;
	private TextView mTvSettings = null;
	private TextView mTvDevnote = null;
	private ProgressBar mPbOffline = null;
	private SurfaceView mSurfaceView = null; // 显示视频
	private Button mBtnAccept; // 接听
	private Button mBtnUnLock = null; // 开锁
	private Button mBtnSnapshot = null; // 截图
	private Button mBtnSpeaker = null; // 扬声器
	private Button mBtnMicrophone = null; // 麦克风
	private MediaPlayer mRingbell = null; // 振铃
	private AudioManager audioManager = null;
//	private BottomMenuDialog mBottomMenuDialog = null; // 底部弹框

	//----------
	public static final String INTENT_VIDEOMODE = "video_mode";
	public static final String INTENT_REMOTEUSER = "remote_user";
	public static final String INTENT_POSITION = "remote_pos";
	private KeyguardManager mKeyguardManager = null;
	private boolean isCallout = false;
	private boolean isAcceptMode = false;
	private boolean isVoicePermit = true;
	private boolean isMutePermit = true;
	private SipClient mSipClient = null;
	private CallConfig mCallConfig = null;
	private AudioManager mAudioManager = null;
	private int mLastMusicVolume = 0;
	private BroadcastReceiver mHomeKeyEventReceiver = null;

	private SipServiceConnection conn = null;
	private Core_Service mCoreService = null;
	private final Handler mHandler = new Handler();
	private long[] mVibrateTime = { 0, 1000, 1000 };

	private long mLastUnlockMillis = 0;
	private final long CALL_TIMEOUT = 3 * 60 * 1000;
//  private final long CALL_TIMEOUT = 5 * 1000;
	private boolean isStartActivity =  true;

	private SensorManager mManager; // 传感器管理对象，调用距离传感器
	private PowerManager localPowerManager = null; // 电源管理对象，控制屏幕暗亮
	private PowerManager.WakeLock localWakeLock = null; // 电源锁
	private boolean saveSpeakerStatus = false;

	private Timer timerOfCheckPacketNumber = null;
	private TimerTask timerTaskOfCheckPacketNumber = null;
	private long lastPacketNumber = -1;
	private boolean checkPacketNumberFlag = true;

	private boolean isHideSensor = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DPLog.println("video oncreate");
		if (init_mode())  {
//			setImmersiveStatusBar();
			addWindowFlag();
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(R.layout.activity_monitor);
		}
		mManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		localPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		localWakeLock = this.localPowerManager.newWakeLock(32, "MyPower"); // 第一个参数为电源锁级别，第二个是日志TAG
		if (isStartActivity) {
			init_view();
			instance = this;
			specifyViewParam();
			if (isCallout) {
				mSipClient.callout(mCallConfig);
			} else {
				showMedia();
			}
		}

		mHomeKeyEventReceiver = new HomeKeyReceiver();
		registerReceiver(mHomeKeyEventReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
		Intent i = new Intent(VideoActivity.this, CoreService.class);
		conn = new SipServiceConnection();
		bindService(i, conn, Context.BIND_AUTO_CREATE);

		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				isHideSensor = false;
			}
		}, 2 * 1000);
		DPLog.print("yying", "onCreate(VideoActivity.java:161)-->>" + "onCreate");
	}

	private void specifyViewParam() {
		String str = getIntent().getStringExtra(CoreService.VIDEO_PARAM);
		if (str != null && !str.equals("")) {
			AIDLParamBean bean = new Gson().fromJson(str, AIDLParamBean.class);
//			FrameLayout layout = (FrameLayout) findViewById(R.id.monitor_layout_title);
//			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//				LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout.getLayoutParams();
//				params.topMargin = bean.statusBarHeight;
//				layout.setLayoutParams(params);
//			}
//			FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mSurfaceView.getLayoutParams();
//			params.width = bean.screenWidth;
//			params.height = bean.screenWidth / 4 * 3;
//			mSurfaceView.setLayoutParams(params);
			DisplayMetrics dm = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(dm);
			android.view.ViewGroup.LayoutParams lp = mSurfaceView.getLayoutParams();
			lp.width = dm.widthPixels;
			lp.height =  lp.width*3/4;
			mSurfaceView.setLayoutParams(lp);
//			mSurfaceView.setVisibility(View.VISIBLE);
		}
	}

	private void addWindowFlag() {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
	}

	@Override
	protected void onStart() {
		DPLog.print("yying", "onStart(VideoActivity.java:203)-->>" + "----------------------------onStart");
		startTimerOfPacketNumber();
		super.onStart();
	}

	@Override
	protected void onRestart() {
		DPLog.print("yying", "onRestart(VideoActivity.java:214)-->>" +  "----------------------------onRestart");
		super.onRestart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mManager.registerListener(this, mManager.getDefaultSensor(Sensor.TYPE_PROXIMITY), SensorManager.SENSOR_DELAY_NORMAL);
		// 注册传感器，第一个参数为距离监听器，第二个是传感器类型，第三个是延时类型
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		DPLog.print("yying", "onStop(VideoActivity.java:225)-->>" + "----------------------------onStop");
		super.onStop();
		if (mManager != null) {
			localWakeLock.setReferenceCounted(false);
			localWakeLock.release(); // 释放电源锁
			mManager.unregisterListener(this); // 注销传感器监听
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		DPLog.print("yying", "onDestroy(VideoActivity.java:232)-->>" + "----------------------------onDestroy");
		hideMedia(false);
		DPLog.print("xufan", "onDestroy(VideoActivity.java:184)-->>" + "取消超时");
		stopTimerOfPacketNumber();
		mHandler.removeCallbacks(callTimeoutThread);
		if (conn != null) {
			unbindService(conn);
		}
		conn = null;
		if (mHomeKeyEventReceiver != null) {
			unregisterReceiver(mHomeKeyEventReceiver);
		}
		mHomeKeyEventReceiver = null;
		if (!mKeyguardManager.inKeyguardRestrictedInputMode()) {
//			hangup();
		}
		if (mSipClient.isOnCall()) {
			hangup();
		}
	}
	
	private void setImmersiveStatusBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		}
	}

	private void init_view() {
		DPLog.print("xufan", "init_view(VideoActivity.java:260)-->>" + "----------------------------init_view()");
		mTvBack = (TextView) findViewById(R.id.monitor_tv_back);
		mTvBack.setOnClickListener(this);
		mTvTitle = (TextView) findViewById(R.id.monitor_tv_title);
		mTvSettings = (TextView) findViewById(R.id.monitor_tv_settings);
		mTvSettings.setOnClickListener(this);
		mTvDevnote = (TextView) findViewById(R.id.monitor_tv_devicenote);
		mPbOffline = (ProgressBar) findViewById(R.id.monitor_tv_offline);
		mSurfaceView = (SurfaceView) findViewById(R.id.monitor_surface_video);
		mBtnAccept = (Button) findViewById(R.id.monitor_btn_accept);
		mBtnAccept.setOnClickListener(this);
		mBtnUnLock = (Button) findViewById(R.id.monitor_btn_openlock);
		mBtnUnLock.setOnClickListener(this);
		mBtnSnapshot = (Button) findViewById(R.id.monitor_btn_snapshot);
		mBtnSnapshot.setOnClickListener(this);
		mBtnMicrophone = (Button) findViewById(R.id.monitor_btn_microphone);
		mBtnMicrophone.setOnClickListener(this);
		mBtnSpeaker = (Button) findViewById(R.id.monitor_btn_speaker);
		mBtnSpeaker.setOnClickListener(this);
		DPLog.print("xufan", "init_view(VideoActivity.java:205)-->>" + "设置按键监听");
		mCallConfig.setSurfaceView(mSurfaceView);

		setSnapShotClickable(false);
		if (isCallout) {
			DPLog.print("xufan", "init_view(VideoActivity.java:206)-->>" + "监视");
			String info = DBEdit.getStringStatus(this, "target_info");
			mTvDevnote.setText(info);
			setAcceptMode(false);
			setButtonClickable(false);
//			if (UserConfig.isCallTransfer)
				setUnlockClickable(false);
		} else {
			DPLog.print("xufan", "init_view(VideoActivity.java:208)-->>" + "新呼入");
			mTvTitle.setText(R.string.monitor_callin);
			setAcceptMode(true);
			setButtonClickable(false);
			String remoteUser = mCallConfig.mRemoteUser.getName();
			DPLog.print("xufan", "init_view(VideoActivity.java:230)-->>" + "remoteUser=" + remoteUser);
			if (remoteUser.length() == 24) {
				DeviceInfoMod device = new DeviceInfoMod();
				DPLog.print("xufan", "init_view(VideoActivity.java:233)-->>" + "调用解析");
				remoteUser = device.resolveDevaccGetDevnote(this, remoteUser.substring(0, QRCodeData.LENGTH_INFO_DATA));
				DPLog.print("xufan", "init_view(VideoActivity.java:235)-->>" + "解析结果:" + remoteUser);
			} else if (remoteUser.length() == 32) {
				List<DeviceInfoMod> list = DBEdit.getDeviceList(this);
				if (list != null && list.size() > 0) {
					for (int i = 0; i < list.size(); i++) {
						if (remoteUser.equals(list.get(i).getDevacc())) {
							String user = list.get(i).getDevnote();
							mTvDevnote.setText(user);
							return;
						}
					}
				}
			}
			mTvDevnote.setText(remoteUser);
		}

		if (UserConfig.isTestMode) {
			findViewById(R.id.test_layout).setVisibility(View.VISIBLE);
			final TextView mTvRx = (TextView) findViewById(R.id.test_tv_rx);
			final TextView mTvTx = (TextView) findViewById(R.id.test_tv_tx);
			final SeekBar mSbRx = (SeekBar) findViewById(R.id.test_sb_rx);
			mSbRx.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					float number = (float) progress / 10;
					mTvRx.setText(number + "");
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {

				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {

				}
			});
			final SeekBar mSbTx = (SeekBar) findViewById(R.id.test_sb_tx);
			mSbTx.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					float number = (float) progress / 10;
					mTvTx.setText(number + "");
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {

				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {

				}
			});
			findViewById(R.id.test_btn_ok).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					float rx = (float) mSbRx.getProgress() / 10;
					float tx = (float) mSbTx.getProgress() / 10;
					DPSipService.getInstance().setRx(rx);
					DPSipService.getInstance().setTx(tx);
					toastShow(instance, "RX:" + rx + " TX:" + tx + " 设置成功");
				}
			});
		}
	}

	private boolean init_mode() {
		mSipClient = SipClient.getInstance();
		mCallConfig = new CallConfig(this);
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);

		Intent intent = getIntent();
		int mode = intent.getIntExtra(INTENT_VIDEOMODE, SipClient.CALL_RING_CALLIN);
		isCallout = mode == SipClient.CALL_RING_CALLOUT;
		String remoteUser = intent.getStringExtra(INTENT_REMOTEUSER);
        DPLog.print("xufan", "init_mode(VideoActivity.java:223)-->>" + remoteUser);
		if (remoteUser == null) {
			if (mSipClient.isOnCall()) {
				hangup();
			}
//			finish();
			return false;
		}
		mCallConfig.mRemoteUser = new SipUser(remoteUser, null, SipUser.TYPE_PHONE);
		return true;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
			if (mSipClient.isOnCall()) {
				hangup();
			}
			return true;
		}
		return super.dispatchKeyEvent(event);
	}
	
	@Override
	public void onClick(View v) {
		DPLog.println("click button " + v.getId());
		final long UNLOCK_INTERVAL_MILLIS = 3000;
		DPLog.print("xufan", "onClick(VideoActivity.java:246)-->>" + "click button " + v.getId());
		switch (v.getId()) {
		case R.id.monitor_tv_back:
			// 如果在通话过程中就挂断，如果没有就忽略
			if (mSipClient.isOnRing() && !mSipClient.isOnCall()) {
				hideMedia(false);
			} else {
				hangup();
			}
			finish();
//			overridePendingTransition(R.anim.out_to_left, R.anim.in_from_right);
//			overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
			overridePendingTransition(Animation.INFINITE, Animation.INFINITE);
			break;
		case R.id.monitor_tv_settings:
			String remoteUser = null;
			if (isCallout) {
				remoteUser = mCallConfig.mRemoteUser.getName();
			} else {
				remoteUser = mSipClient.getRemoteAccount();
			}
			if (mSipClient.isOnRing() && !mSipClient.isOnCall()) {
				hideMedia(false);
				if (mSipClient.isOnCall()) {
					hangup();
				}
			}
			Intent intent = new Intent(this, SettingActivity.class);
			intent.putExtra("remoteUser", remoteUser);
			startActivity(intent);
			break;
		case R.id.monitor_btn_accept: // 接听
			if (isAcceptMode) {
				mCallConfig.isVideoOn = true;
				if (mSipClient.accept(mCallConfig)) {
//				mBtnVideo.setClickable(false);
//				mBtnSpeaker.setClickable(false);
					setAcceptMode(false);
					hideMedia(false);
					DPLog.print("xufan", "onClick(VideoActivity.java:266)-->>" + "接听成功");
				}
			} else {
				DPLog.print("xufan", "onClick(VideoActivity.java:297)-->>" + "点击挂断");
//				if (mSipClient.isOnCall()) {
					hangup();
//				}
				finish();
			}
			break;
		case R.id.monitor_btn_openlock: // 开锁
			long millis = System.currentTimeMillis();
			if (millis - mLastUnlockMillis > UNLOCK_INTERVAL_MILLIS) {
				if (isCallout) {
//					mSipClient.openlock(mCallConfig.mRemoteUser.getRemoteUserName(), 1);
					try {
						SipControl.openlock(mCoreService, mCallConfig.mRemoteUser.getName(), "1");
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				} else {
//					mSipClient.openlock(mSipClient.getRemoteAccount(), 1);
					try {
						SipControl.openlock(mCoreService, mSipClient.getRemoteAccount(), "1");
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
				mLastUnlockMillis = millis;
			}
			if (mSipClient.isOnRing() && !mSipClient.isOnCall()) {
//				mBtnAccept.setEnabled(false);
//				mBtnVideo.setEnabled(false);
//				mBtnSpeaker.setEnabled(false);
			}
			break;
		case R.id.monitor_btn_snapshot: // 截屏 录像
			remoteUser = null;
			if (isCallout) {
				remoteUser = mCallConfig.mRemoteUser.getName();
			} else {
				remoteUser = mSipClient.getRemoteAccount();
			}
			String imagePath = PathUtil.getImageName(remoteUser);
			if (mSipClient.requestCaptureOnVideoStream((imagePath))) {
				intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
				Uri uri = Uri.fromFile(new File(imagePath));
				intent.setData(uri);
				sendBroadcast(intent);
				toastShow(this, getString(R.string.monitor_shot_photo));
			} else {
				toastShow(this, getString(R.string.monitor_shot_photo_savefail));
			}
			break;
		case R.id.monitor_btn_speaker: // 扬声器
			isVoicePermit = !isVoicePermit;
			setSpeakerIcon(isVoicePermit);
			mCallConfig.isSpeakerOn = isVoicePermit;
			mCallConfig.notifyDataSetChanged();
			break;
		case R.id.monitor_btn_microphone: // 麦克风
			isMutePermit = !isMutePermit;
			setMicIcon(isMutePermit);
			mCallConfig.isMicrophoneOn = isMutePermit;
			mCallConfig.notifyDataSetChanged();
			break;
		}
	}

	private void hangup() {
		mSipClient.hangup();
	}

	private void showMedia() {
		DPLog.print("xufan", "showMedia(VideoActivity.java:375)-->>" + "----------------------------showMedia");
		if (mSipClient.isOnRing() && !mSipClient.isOnCall()) {
			if (MediaPlayerUtil.isPlaying()) {
				return;
			}
			mLastMusicVolume = MediaPlayerUtil.getStreamVolume(mAudioManager, AudioManager.STREAM_MUSIC);
			DPLog.print("xufan", "showMedia(VideoActivity.java:379)-->>" + "----------------------------STREAM_MUSIC " + mLastMusicVolume);
			float percentage = MediaPlayerUtil.getStreamVolumePercentage(mAudioManager, AudioManager.STREAM_RING);
			MediaPlayerUtil.setStreamVolumePercentage(mAudioManager, AudioManager.STREAM_MUSIC, percentage);
			VibratorUtil.openVibrator(VideoActivity.this, mVibrateTime, 0);
			MediaPlayerUtil.play(VideoActivity.this, R.raw.ringin, true);
		} else if (!mSipClient.isOnCall()) {
			finish();
		}
	}

	private void hideMedia(boolean isReserve) {
		DPLog.print("xufan", "hideMedia(VideoActivity.java:390)-->>" + "----------------------------hideMedia");
		if (isReserve) {
			DPLog.print("xufan", "hideMedia(VideoActivity.java:392)-->>" + "----------------------------STREAM_MUSIC " + mLastMusicVolume);
			MediaPlayerUtil.setStreamVolume(mAudioManager, AudioManager.STREAM_MUSIC, mLastMusicVolume);
		}
		if (!MediaPlayerUtil.isPlaying()) {
			return;
		}
		VibratorUtil.closeVibrator();
		MediaPlayerUtil.stop();
	}

	private class SipServiceConnection implements ServiceConnection {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			DPLog.print("xufan", "onServiceDisconnected(VideoActivity.java:412)-->>" + "----------------------------onServiceDisconnected");
			try {
				mCoreService.unbindSipCallback(mSipCallback);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			DPLog.print("xufan", "onServiceConnected(VideoActivity.java:417)-->>" + "----------------------------onServiceConnected");
			BinderFactoryAIDL factory = BinderFactoryAIDL.Stub.asInterface(service);
			try {
				mCoreService = Core_Service.Stub.asInterface(factory.queryBinder(BinderPool.BINDERCODE_SIP));
				mCoreService.bindSipCallback(mSipCallback);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	private SipCallback.Stub mSipCallback = new SipCallback.Stub() {

		@Override
		public void onSipLoginState(final int state, final String reason) throws RemoteException {
			runOnUiThread(new Runnable() {
				public void run() {
					switch (state) {
						case SipClient.ACCOUNT_LOGIN_SUCCESS:
							DPLog.print("xufan", "run(VideoActivity.java:438)-->>" + "FLAG_STATE_ACCOUNT ACCOUNT_LOGIN_SUCCESS");
							break;
						case SipClient.ACCOUNT_LOGIN_START:
							DPLog.print("xufan", "run(VideoActivity.java:441)-->>" + "FLAG_STATE_ACCOUNT ACCOUNT_LOGIN_START");
							break;
						case SipClient.ACCOUNT_LOGIN_FAIL:
							DPLog.print("xufan", "run(VideoActivity.java:444)-->>" + "FLAG_STATE_ACCOUNT ACCOUNT_LOGIN_FAIL " + reason);
							break;
						case SipClient.ACCOUNT_LOGOUT:
							DPLog.print("xufan", "run(VideoActivity.java:446)-->>" + "FLAG_STATE_ACCOUNT ACCOUNT_LOGOUT");
							break;
					}
				}
			});
		}

		@Override
		public void onDispatch(int flag, int code, int result, String reason) throws RemoteException {
			DPLog.print("xufan", "=======================================>onDispatch flag=" + flag + " code=" + code + " result=" + result + " reason=" + reason);
			switch (flag) {
				case SipClient.FLAG_STATE_CALL:
					switch (code) {
						case SipClient.CALL_RING_CALLOUT:
							DPLog.print("xufan", "onSipCall(VideoActivity.java:461)-->>" + "----------------------------CALL_RING_CALLOUT");
							break;
						case SipClient.CALL_RING_CALLIN:

							DPLog.print("xufan", "onSipCall(VideoActivity.java:464)-->>" + "----------------------------CALL_RING_CALLIN");
							if (!mSipClient.isOnRing()) {
								if (mSipClient.isOnCall()) {
									hangup();
								}
								break;
							}
							DPLog.print("xufan", "mSipClient.isOnRing " + mSipClient.getRemoteAccount() + " " + mSipClient.isOnCall());
							isCallout = false;
							DPLog.print("xufan", "onSipCall(VideoActivity.java:435)-->>" + "SipClient getRemoteAccount= " + mSipClient.getRemoteAccount());
							mCallConfig.mRemoteUser = new SipUser(mSipClient.getRemoteAccount(), null, SipUser.TYPE_PHONE);
//							init_view();
							showMedia();
							TaskStackUtil.moveTaskToFront(VideoActivity.this);
							break;
						case SipClient.CALL_START:
							DPLog.print("xufan", "onSipCall(VideoActivity.java:487)-->>" + "----------------------------CALL_START");
							if (mCallConfig.isVideoOn) {
//								if (mSipClient.isOnCall()) {
									mSurfaceView.setVisibility(View.VISIBLE);
//								}
								mPbOffline.setVisibility(View.GONE);
								setButtonClickable(true);

								if (isCallout) {
									//  关闭麦克风
									isMutePermit = !isMutePermit;
									setMicIcon(isMutePermit);
									mCallConfig.isMicrophoneOn = isMutePermit;
									mCallConfig.notifyDataSetChanged();
								}
								DPLog.print("xufan", "onSipCall(VideoActivity.java:524)-->>" + "开始计时");
								mHandler.removeCallbacks(callTimeoutThread);
								mHandler.postDelayed(callTimeoutThread, CALL_TIMEOUT);
							} else {

							}
							addWindowFlag();
							break;
						case SipClient.CALL_BUSY:
						case SipClient.CALL_FINISH:
						case SipClient.CALL_UNKNOW:
							DPLog.print("xufan", "onSipCall(VideoActivity.java:521)-->>" + "----------------------------CALL_HANGUP");
							if (mSurfaceView != null) {
								mSurfaceView.setVisibility(View.INVISIBLE);
							}

							if (!isFinishing()) {
								DPLog.print("xufan", "onSipCall(VideoActivity.java:448)-->>" + getString(R.string.monitor_offline));
							}
							hideMedia(true);
							if (mSipClient.isOnCall()) {
								hangup();
							}
							finish();
							break;
					}
					break;
				case CIMessageAdapter.FLAG_RECEIVE_CAPTURE:
					DPLog.print("xufan", "onSipCall(VideoActivity.java:547)-->>" + "----------------------------FLAG_RECEIVE_CAPTURE");
					break;
				case CIMessageAdapter.FLAG_RESULT_OPENLOCK:
					DPLog.print("xufan", "onSipCall(VideoActivity.java:550)-->>" + "----------------------------FLAG_RESULT_UNLOCK");
					if (result == 1 && !isFinishing()) {
						// FIXME 传过来的reason;

						DPLog.print("xufan", "onSipCall(VideoActivity.java:467)-->>" + getString(R.string.opendoor_success));

						toastShow(instance, getString(R.string.opendoor_success));
					}
					break;
			}
		}

		@Override
		public int getCallbackId() throws RemoteException {
			return 1;
		}
	};

	private class HomeKeyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
				DPLog.print("xufan", "onReceive(VideoActivity.java:583)-->>" + "----------------------------ACTION_CLOSE_SYSTEM_DIALOGS");
				if (mKeyguardManager.inKeyguardRestrictedInputMode()) {
					if (mSipClient.isOnCall()) {
						hangup();
					}
				}
			}
		}
	}

	private void setButtonClickable(boolean clickable) {
		mBtnSnapshot.setClickable(clickable);
		mBtnSpeaker.setClickable(clickable);
		mBtnMicrophone.setClickable(clickable);
		if (clickable) {
			changeBtnIcon(mBtnSnapshot, R.mipmap.png_monitor_icon_snapshot);
			changeBtnIcon(mBtnSpeaker, R.mipmap.png_monitor_icon_speaker);
			changeBtnIcon(mBtnMicrophone, R.mipmap.png_monitor_icon_microphone);
		} else {
			changeBtnIcon(mBtnSnapshot, R.mipmap.png_monitor_icon_snapshot_disabled);
			changeBtnIcon(mBtnSpeaker, R.mipmap.png_monitor_icon_speaker_disabled);
			changeBtnIcon(mBtnMicrophone, R.mipmap.png_monitor_icon_microphone_disabled);
		}
	}

	private void setUnlockClickable(boolean clickable) {
		mBtnUnLock.setClickable(clickable);
		if (clickable) {
			changeBtnIcon(mBtnUnLock, R.mipmap.png_monitor_icon_openlock);
		} else {
			changeBtnIcon(mBtnUnLock, R.mipmap.png_monitor_icon_openlock_disabled);
		}
	}

	private void setSnapShotClickable(boolean clickable) {
		mBtnSnapshot.setClickable(clickable);
		if (clickable) {
			changeBtnIcon(mBtnSnapshot, R.mipmap.png_monitor_icon_snapshot);
		} else {
			changeBtnIcon(mBtnSnapshot, R.mipmap.png_monitor_icon_snapshot_disabled);
		}
	}

	private void setSpeakerIcon(boolean isVoicePermit) {
		this.isVoicePermit = isVoicePermit;
		if (isVoicePermit) {
			changeBtnIcon(mBtnSpeaker, R.mipmap.png_monitor_icon_speaker);
		} else {
			changeBtnIcon(mBtnSpeaker, R.mipmap.png_monitor_icon_speaker_closed);
		}
	}

	private void setMicIcon(boolean isMutePermit) {
		this.isMutePermit = isMutePermit;
		if (isMutePermit) {
			changeBtnIcon(mBtnMicrophone, R.mipmap.png_monitor_icon_microphone);
		} else {
			changeBtnIcon(mBtnMicrophone, R.mipmap.png_monitor_icon_microphone_closed);
		}
	}

	private void setAcceptMode(boolean isAcceptMode) {
		this.isAcceptMode = isAcceptMode;
		if (isAcceptMode == true) {
			changeBtnIcon(mBtnAccept, R.mipmap.png_monitor_icon_accept);
			mBtnAccept.setText(getString(R.string.monitor_accept));
		} else {
			changeBtnIcon(mBtnAccept, R.mipmap.png_monitor_icon_hangup);
			mBtnAccept.setText(getString(R.string.monitor_hangup));
		}
	}

	@SuppressLint("NewApi") private void changeBtnIcon(Button btn, int drawableId){
		Drawable drawable=this.getResources().getDrawable(drawableId);
		btn.setCompoundDrawablesRelativeWithIntrinsicBounds(null,drawable,null,null);
	}

	// 监视超时 通话超时
	Runnable callTimeoutThread = new Runnable() {

		@Override
		public void run() {
			if (mSipClient.isOnCall()) {
				DPLog.println("hangupCall 超时挂断");
				hangup();
				finish();
			}
			if (isCallout)
				toastShow(VideoActivity.this, getString(R.string.monitor_callout_timeout));
			else
				toastShow(VideoActivity.this, getString(R.string.monitor_callin_timeout));
		}
	};

	@Override
	public void onSensorChanged(SensorEvent event) {
		if ((mSipClient.isOnRing() && !mSipClient.isOnCall()) || isHideSensor)
			return;
		float[] its = event.values;
		if (its != null && event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
			// 小米5测试时，当手贴近距离传感器的时候its[0]返回值为0.0，当手离开时返回5.000305
			if (its[0] == 0) { // 贴近手机
				if (localWakeLock.isHeld()) // 判断屏幕当前是否是休眠状态
					return;
				else {
					localWakeLock.acquire(); // 申请设备电源锁，在屏幕休眠的状态下唤醒屏幕
					DPLog.print("xufan", "onSensorChanged-->>" + "贴近");
					saveSpeakerStatus = isVoicePermit;
					if (isVoicePermit) {
						isVoicePermit = !isVoicePermit;
						setSpeakerIcon(isVoicePermit);
						mCallConfig.isSpeakerOn = isVoicePermit;
						mCallConfig.notifyDataSetChanged();
					}
				}
			} else { // 远离手机
				if (!localWakeLock.isHeld())
					return;
				else {
					localWakeLock.setReferenceCounted(false); // 设置为false时，在release的时候，不管你acquire()了多少回，可以一次releaseWakeLock掉
					localWakeLock.release(); // 释放设备电源锁，在屏幕点亮的状态下，使屏幕休眠
					DPLog.print("xufan", "onSensorChanged-->>" + "远离");
					if (saveSpeakerStatus) {
						isVoicePermit = true;
						setSpeakerIcon(isVoicePermit);
						mCallConfig.isSpeakerOn = isVoicePermit;
						mCallConfig.notifyDataSetChanged();
					}
				}
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	private void checkPacketNumber() {
		if (DPSipService.getInstance() == null)
			return;
		long packetNumber = DPSipService.getInstance().getPacketNumber();
		DPLog.print("xufan", "checkPacketNumber(VideoActivity.java:832)-->>" + "packetNumber= " + packetNumber);
		if (lastPacketNumber > 0 && lastPacketNumber == packetNumber) {
			finish();
		} else
			lastPacketNumber = packetNumber;
	}

	private void startTimerOfPacketNumber() {
		stopTimerOfPacketNumber();
		DPLog.print("xufan", "startTimerOfPacketNumber(VideoActivity.java:843)-->>" + "startTimerOfPacketNumber()");
		if (timerOfCheckPacketNumber == null)
			timerOfCheckPacketNumber = new Timer();
		if (timerTaskOfCheckPacketNumber == null) {
			timerTaskOfCheckPacketNumber = new TimerTask() {
				@Override
				public void run() {
					checkPacketNumber();
				}
			};
		}
		timerOfCheckPacketNumber.schedule(timerTaskOfCheckPacketNumber, 3 * 1000, 1 * 1000);
	}

	private void stopTimerOfPacketNumber() {
		DPLog.print("xufan", "stopTimerOfPacketNumber(VideoActivity.java:858)-->>" + "stopTimerOfPacketNumber()");
		if (timerOfCheckPacketNumber != null) {
			DPLog.print("xufan", "stopTimerOfPacketNumber(VideoActivity.java:860)-->>" + "timer cancel");
			timerOfCheckPacketNumber.cancel();
			timerOfCheckPacketNumber = null;
		}
		if (timerTaskOfCheckPacketNumber != null) {
			DPLog.print("xufan", "stopTimerOfPacketNumber(VideoActivity.java:865)-->>" + "timerTask cancel");
			timerTaskOfCheckPacketNumber.cancel();
			timerTaskOfCheckPacketNumber = null;
		}
	}

	private void setRxTx(float rx, float tx) {
		DPSipService.getInstance().setRx(rx);
		DPSipService.getInstance().setTx(tx);
		((TextView)findViewById(R.id.test_tv_rx)).setText(rx + "");
		((TextView)findViewById(R.id.test_tv_tx)).setText(tx + "");
	}
}