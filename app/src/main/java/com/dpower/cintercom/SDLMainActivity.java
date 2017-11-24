package com.dpower.cintercom;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;


import com.dpower.cintercom.activity.AlarmDialog;
import com.dpower.cintercom.activity.BaseActivity;
import com.dpower.cintercom.activity.ChooseModeActivity;
import com.dpower.cintercom.activity.VideoActivity;
import com.dpower.cintercom.app.MyApplication;
import com.dpower.cintercom.app.UserConfig;
import com.dpower.cintercom.domain.DeviceInfoMod;
import com.dpower.cintercom.fragment.DeviceListFragment;
import com.dpower.cintercom.fragment.SafeModeFrament;
import com.dpower.cintercom.impl.SipControl;
import com.dpower.cintercom.util.DBEdit;
import com.dpower.cintercom.util.DPLog;
import com.dpower.utils.MyUtil;
import com.dpower.cintercom.util.PowerUtils;
import com.dpower.cintercom.util.ThreadManager;
import com.dpower.cintercom.widget.ChoiceView;
import com.dpower.cintercom.widget.GuideView;
import com.dpower.cintercom.widget.NoScrollViewPager;
import com.dpower.cintercom.widget.ViewPagerIndicator;
import com.dpower.dpsiplib.BinderFactoryAIDL;
import com.dpower.dpsiplib.BinderPool;
import com.dpower.dpsiplib.CoreService;
import com.dpower.dpsiplib.Core_Service;
import com.dpower.dpsiplib.SipCallback;
import com.dpower.dpsiplib.SipClient;
import com.dpower.dpsiplib.bean.AIDLParamBean;
import com.dpower.dpsiplib.message.CIMessageAdapter;
import com.dpower.dpsiplib.message.CIMessageFactory;
import com.dpower.dpsiplib.utils.NetWorkUntil;
import com.dpower.push.PushConfig;
import com.dpower.push.jpush.broadcast.JPushReceiver;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.jpush.android.api.JPushInterface;
import scanner.CaptureActivity;

/**
 * 无显示版本号，选择房号
 * 切换房号门口机列表和安防模式设置
 */

public class SDLMainActivity extends BaseActivity implements OnClickListener, Handler.Callback {
    private ImageButton mBtnAddDevice = null; // 添加设备

    private static SDLMainActivity instance = null;
    public static Handler handler = null;
    private String mUserName = null, mServerIP = null;

    public final static int MSG_START_LOGININ = 100; // 开始登录
    public final static int MSG_START_LOGINOUT = 5001; // 开始登出
    private final static int MSG_INIT_ACTICITY = 5003; // 初始化弹出界面
    public final static int MSG_START_MONITOR = 5004; // 监视
    public final static int MSG_BIND_DEVICE = 5006; // 绑定设备
    private final static int MSG_GET_IP = 5007; // 解析域名
    public final static int MSG_GET_SAFEMODE = 5008; // 获取安防模式
    public final static int MSG_SET_SAFEMODE = 5009; // 设置安防模式
    public final static int MSG_START_GUIDEVIEW = 2017; // 新手引导
    public final static int MSG_UNBIND_DEVICE = 2019; // 解绑设备

    //-------
    public static Core_Service mCoreService = null;
    private Messenger mMessenger = null;
    private Messenger mTargetMessenger = null;
    private Button mBtnDoorNumber = null;
    private ImageView mIvDeviceOnline = null;

    private int mSafeModeNumber = -1; // 安防模式
    private boolean isFirstUpdate = true;

    private static PowerUtils phone = new PowerUtils();

    public static String mRoomNumber = null;
    private Timer timerOfCheckDeviceOnline = null;
    private TimerTask timerTaskOfCheckDeviceOnline = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        smoothSwitchScreen();
        setContentView(R.layout.activity_sdlmain);

        PushConfig.isActivityDestory = false;
        handler = new Handler(this);
        mMessenger = new Messenger(handler);
        instance = this;
        MyApplication apl = (MyApplication) getApplicationContext();
        mUserName = UserConfig.APP_IDENTIFIER + apl.getUUID(this);
        if (mUserName != null)
            DBEdit.setStatusValue(this, "sip_username", mUserName);
        String roomNumber = DBEdit.getStringStatus(this, "roomNumber");
        if (roomNumber != null)
            mRoomNumber = roomNumber;
        init_view();
        DPLog.println("SDLMainActivity oncreate!");

        String from = getIntent().getStringExtra(CoreService.INTENT_FROM);
        String msg = getIntent().getStringExtra(JPushInterface.EXTRA_MESSAGE);
        String extras = getIntent().getStringExtra(JPushInterface.EXTRA_EXTRA);
        int id = getIntent().getIntExtra(JPushReceiver.NOTIFY_ID, -1);
        if (id != -1)
            JPushReceiver.cancelNotification(this, id);
        if (from != null && msg != null) {
            Intent intent = new Intent(this, AlarmDialog.class);
            intent.putExtra(CoreService.INTENT_FROM, from);
            intent.putExtra(JPushInterface.EXTRA_MESSAGE, msg);
            Log.e("xufan", "打开Main msg=" + msg);
            intent.putExtra(JPushInterface.EXTRA_EXTRA, extras);
            startActivity(intent);
        }

        initViews();
        initDatas();

        //动态设置tab
        mViewPagerIndicator.setVisibleTabCount(2);
        mViewPagerIndicator.setTabItemTitles(mTitles);

        mViewpager.setAdapter(mFAdapter);
        mViewPagerIndicator.setViewPager(mViewpager, 0);
        mViewpager.setNoScroll(true);
    }

    private void init_view() {
        mBtnAddDevice = (ImageButton) findViewById(R.id.main_btn_adddevice);
        mBtnAddDevice.setOnClickListener(SDLMainActivity.this);
        mBtnDoorNumber = (Button) findViewById(R.id.main_btn_doornumber);
        mBtnDoorNumber.setOnClickListener(this);
        mIvDeviceOnline = (ImageView) findViewById(R.id.main_iv_online);
    }

    // 加载数据库中的设备列表
    private void init_data() {

    }

    @Override
    protected void onStart() {
        // 启动SIP服务
        Intent intent = new Intent(this, CoreService.class);
        startService(intent);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        init_data();
        initRoomNumberList();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
//		DPLog.print(DPLog.LOG_ERROR, "app exit!");
        stopTimerOfCheckDeviceOnline();
        // 关闭SIP服务
        unbindService(conn);
        try {
            mCoreService.logout();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        instance = null;
//		if (!mIsRestartApp) {
//        Runtime.getRuntime().gc();
//        android.os.Process.killProcess(android.os.Process.myPid());
//		}
        Intent intent = new Intent(this, CoreService.class);
        stopService(intent);
        PushConfig.isActivityDestory = true;

        Runtime.getRuntime().gc();
        android.os.Process.killProcess(android.os.Process.myPid());
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_btn_adddevice: //  选择模式界面
                try {
                    if (!mCoreService.isLogin()) {
                        toastShow(this, getString(R.string.main_unlogin));
                        handler.sendEmptyMessage(MSG_START_LOGININ);
                        return;
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                startActivity(new Intent(this, ChooseModeActivity.class));;
                break;
            case R.id.main_tv_title:
                showDialog(getString(R.string.main_app_info), getString(R.string.main_app_version) + "  " + MyUtil.getApplicationVersion(this));
                break;
            case R.id.monitor_btn_accept:
                handler.sendEmptyMessage(MSG_START_MONITOR);
                break;
            case R.id.main_btn_doornumber:
                showRoomNumberList(true);
                break;
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_GET_IP: // 解析域名
                // 解析域名地址
                ThreadManager.getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        mServerIP = "www.d-powercloud.net:8060";
                        DPLog.print("xufan", "run(SDLMainActivity.java:531)-->>" + "MSG_GET_IP " + "mServerIP=" + mServerIP);
                        handler.sendEmptyMessage(MSG_START_LOGININ);
                    }
                });
                break;
            case MSG_START_LOGININ: // 登录
                login();
                break;
            case MSG_START_LOGINOUT: // 登出
                try {
                    mCoreService.logout();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case MSG_START_MONITOR: // 监视
                try {
                    if (!mCoreService.isLogin()) {
                        toastShow(this, getString(R.string.main_unlogin));
                        handler.sendEmptyMessage(MSG_START_LOGININ);
                        return false;
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                String account = DBEdit.getStringStatus(this, "target_user");
                if (account != null && account.length() > 0) {
                    try {
                        DPLog.print("xufan", "handleMessage(SDLMainActivity.java:266)-->>" + "点击呼叫");
                        boolean isIntentIsNull = mCoreService.callout(account, null);
                        DPLog.print("xufan", "handleMessage(SDLMainActivity.java:267)-->>" + "intent不为空：" + isIntentIsNull);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } else {
                    toastShow(this, getString(R.string.main_device_unbind));
                }
                break;
            case MSG_INIT_ACTICITY: // 初始化
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        DPLog.print("xufan", "run(SDLMainActivity.java:321)-->>" + "init");
                        AIDLParamBean bean = new AIDLParamBean();
                        bean.callinActivityClassName = VideoActivity.class.getName(); // 呼入界面
                        bean.monitorActivityClassName = VideoActivity.class.getName(); // 监视界面
                        bean.messageFactoryClassName = CIMessageFactory.class.getName();

                        try {
                            mCoreService.setVideoParams(new Gson().toJson(bean));
                            setCallWhiteList();
                            if (mCoreService.isLogin()) {
                            } else if (mCoreService.isReady()) {
                            }
                        } catch (RemoteException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
                break;
            case CoreService.MSG_COMMAND_STARTACTIVITY:// 跳转界面
                Bundle b = msg.getData();
                Intent i = b.getParcelable(CoreService.BUNDLE_ACTIVITY_INTENT);
                if (i != null) {
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i, 0);
                    try {
                        pendingIntent.send();
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                    startActivity(i);
                }
                break;
            case MSG_BIND_DEVICE:
                DPLog.print("xufan", "handleMessage(SDLMainActivity.java:330)-->> " + "MSG_BIND_DEVICE");
                String username = (String) msg.obj;
                try {
                    if (username != null && mUserName != null) {
                        DPLog.print("xufan", "handleMessage(SDLMainActivity.java:334)-->>" + "username:" + username + " mUserName:" + mUserName);
                        boolean flag = false;
                        Bundle bundle = msg.getData();
                        String roomNumber = bundle.getString("roomNumber");
                        flag = SipControl.bind(mCoreService, username, roomNumber, mUserName);
                        DPLog.print("xufan", "handleMessage(SDLMainActivity.java:349)-->>" + "bind flag= " + flag);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case MSG_UNBIND_DEVICE:
                DPLog.print("xufan", "handleMessage(SDLMainActivity.java:364)-->>" + "MSG_UNBIND_DEVICE");
                String target = (String) msg.obj;
                if (target == null || target.length() < 1)
                    target = DBEdit.getStringStatus(instance, "target_user");
                try {
                    if (mUserName != null && mUserName.length() > 0 && target != null && target.length() > 0) {
                        SipControl.unbind(mCoreService, target, mUserName);

                        if (msg.arg1 == 1)
                            return false;
                        toastShow(instance, getString(R.string.main_unbind_success));
                        if (DeviceListFragment.instance != null) {
                            DeviceListFragment.instance.getHandler().sendEmptyMessage(DeviceListFragment.MSG_DELETE_DEVICE);
                        }

                        setCallWhiteList();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                updateDoorNumberAndSafeModeAfterDeleteDevice();
                            }
                        }, 100);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case MSG_GET_SAFEMODE:
                updateSafeMode();
                break;
            case MSG_SET_SAFEMODE:
                String mode = (String) msg.obj;
                DPLog.print("xufan", "handleMessage(SDLMainActivity.java:654)-->>" + "MSG_SET_SAFEMODE" + mode);
                target = null;
                DeviceInfoMod device = DBEdit.getPrimaryDevcie(instance, mRoomNumber);
                if (device != null) {
                    target = device.getDevacc();
                } else { // 找不到主设备
                    if (SafeModeFrament.instance != null)
                        SafeModeFrament.instance.getHandler().sendEmptyMessage(SafeModeFrament.MSG_INIT);
                    mRoomNumber = null;
                    deviceId = -1;
                    mIvDeviceOnline.setImageDrawable(getResources().getDrawable(R.mipmap.room_offline));
                }
                if (target != null && target.length() > 0) {
                    try {
                        if (mRoomNumber == null)
                            return false;
                        SipControl.setSafeMode(mCoreService, target, mode, mRoomNumber);
                        if (SafeModeFrament.instance != null) {
                            SafeModeFrament.instance.getHandler().sendEmptyMessage(SafeModeFrament.MSG_SHOW_PROGRESS);
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case CLOSE_DIALOG: //关闭设备列表窗口
                if (dialog != null) {
                    dialog.dismiss();
                    dialog = null;
                }
                break;
        }
        return true;
    }

    private ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BinderFactoryAIDL factory = BinderFactoryAIDL.Stub.asInterface(service);
            try {
                mCoreService = Core_Service.Stub.asInterface(factory.queryBinder(BinderPool.BINDERCODE_SIP));
                mCoreService.bindSipCallback(mSipCallback);

                mTargetMessenger = new Messenger(factory.queryBinder(BinderPool.BINDERCODE_STARTACTIVITY));
                Message msgFromClient = Message.obtain(null, CoreService.MSG_REQUEST_REGIST);
                msgFromClient.replyTo = mMessenger;
                try {
                    mTargetMessenger.send(msgFromClient);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                handler.sendEmptyMessageDelayed(MSG_INIT_ACTICITY, 250);
                handler.sendEmptyMessageDelayed(MSG_GET_IP, 500);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            try {
                boolean flag = mCoreService.logout();
                DPLog.print("xufan", "onServiceDisconnected(SDLMainActivity.java:399)-->>" + "flag= " + flag);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            try {
                mCoreService.unbindSipCallback(mSipCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

    };

    private SipCallback.Stub mSipCallback = new SipCallback.Stub() {

        @Override
        public void onSipLoginState(final int state, final String reason) throws RemoteException {
            runOnUiThread(new Runnable() {
                public void run() {
                    DPLog.print("xufan", "run(SDLMainActivity.java:413)-->>" + "sip login state= " + state);

                    switch (state) {
                        case SipClient.ACCOUNT_LOGIN_SUCCESS:
                            DPLog.print("xufan", "run(SDLMainActivity.java:361)-->>" + "onSipLoginState ACCOUNT_LOGIN_SUCCESS");
                            hideTextProgress();
                            // 多次触发
                            if (isFirstUpdate) {
                                updateSafeModeFragment();
                                startTimerOfCheckDeviceOnline();
                                isFirstUpdate = false;
                            }
                            break;
                        case SipClient.ACCOUNT_LOGIN_START:
                            DPLog.print("xufan", "run(SDLMainActivity.java:365)-->>" + "onSipLoginState ACCOUNT_LOGIN_START");
                            break;
                        case SipClient.ACCOUNT_LOGIN_FAIL:
                            DPLog.print("xufan", "run(SDLMainActivity.java:369)-->>" + "onSipLoginState ACCOUNT_LOGIN_FAIL");
                            hideTextProgress();
                            break;
                        case SipClient.ACCOUNT_LOGOUT:
                            DPLog.print("xufan", "run(SDLMainActivity.java:373)-->>" + "onSipLoginState ACCOUNT_LOGOUT");
                            hideTextProgress();
                            break;
                    }
                }
            });
        }

        @Override
        public void onDispatch(int flag, int code, int result, String reason) throws RemoteException {
            switch (flag) {
                case SipClient.FLAG_STATE_CALL:
                    switch (code) {
                        case SipClient.CALL_REFUSE:
                            DPLog.print("xufan", "onDispatch(MainActivity.java:953)-->>" + "呼叫挂断 user=" + reason);
                            // 解绑的设备
                            Message msg = new Message();
                            msg.what = SDLMainActivity.MSG_UNBIND_DEVICE;
                            msg.obj = reason;
                            msg.arg1 = 1;
                            SDLMainActivity.handler.sendMessage(msg);
                            break;
                    }
                    break;
                case CIMessageAdapter.FLAG_RESULT_BIND:
                    if (result == 1) {
                        DPLog.print("xufan", "onSipCall(SDLMainActivity.java:402)-->>" + "绑定成功");
                        if (CaptureActivity.instance != null) {
                            DPLog.print("xufan", "onSipCall(SDLMainActivity.java:413)-->>" + "captrueactivity instance is not null");
                            CaptureActivity.instance.mHandler.sendEmptyMessage(CaptureActivity.MSG_CONFIG_OK);
                        }
                    } else {
                        DPLog.print("xufan", "onSipCall(SDLMainActivity.java:402)-->>" + "绑定失败");
                        if (CaptureActivity.instance != null)
                            CaptureActivity.instance.mHandler.sendEmptyMessage(CaptureActivity.MSG_CONFIG_FAIL);
                    }
                    break;
                case CIMessageAdapter.FLAG_RESULT_UNBIND:
                    hideProgress();
                    if (result == 1) {
                        DPLog.print("xufan", "onSipCall(SDLMainActivity.java:402)-->>" + "解绑成功");
                    } else {
                        DPLog.print("xufan", "onSipCall(SDLMainActivity.java:402)-->>" + "解绑失败");
                    }
                    break;
                case CIMessageAdapter.FLAG_RESULT_GET_SAFEMODE:
                case CIMessageAdapter.FLAG_RESULT_GET_NEW_SAFEMODE:
                    if (result == 1) {
                        DPLog.print("xufan", "onSipCall(SDLMainActivity.java:611)-->>" + "修改安防模式成功");
                        String mode;
                        if (reason.contains(" ")) {
                            mode = reason.split(" ")[0];
                            String room = reason.split(" ")[1];
                            if (mRoomNumber != null && room != null && mRoomNumber.equals(room))
                                updateSafeModeInfo(mode);
                        } else {
                            mode = reason;
                            updateSafeModeInfo(mode);
                        }
                    } else {
                        if (SafeModeFrament.instance != null) {
                            SafeModeFrament.instance.getHandler().sendEmptyMessage(SafeModeFrament.MSG_INIT);
                        }
                        DPLog.print("xufan", "onSipCall(SDLSDLMainActivity.java:613)-->>" + "修改安防模式失败");
                    }
                    break;
                case CIMessageAdapter.FLAG_RESULT_CHECK_ONLINE:
                    if (result == 1) {
                        DPLog.print("xufan", "onSipCall(SDLMainActivity.java:1096)-->>" + "设备在线");
                        mIvDeviceOnline.setImageDrawable(getResources().getDrawable(R.mipmap.room_online));
                    } else {
                        DPLog.print("xufan", "onSipCall(SDLMainActivity.java:1099)-->>" + "设备不在线");
                        mIvDeviceOnline.setImageDrawable(getResources().getDrawable(R.mipmap.room_offline));
                    }
                    break;
            }
        }

        @Override
        public int getCallbackId() throws RemoteException {
            return 0;
        }
    };

    public static SDLMainActivity getInstance() {
        return instance;
    }

    private void smoothSwitchScreen() {
        // 5.0以上修复了此bug
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            ViewGroup rootView = ((ViewGroup) this.findViewById(android.R.id.content));
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            int statusBarHeight = getResources().getDimensionPixelSize(resourceId);
            rootView.setPadding(0, statusBarHeight, 0, 0);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    private void login() {
        DPLog.print("xufan", "login(SDLMainActivity.java:564)-->>" + "login");
        if (mServerIP == null) {

        } else {
            try {
                int netstate = NetWorkUntil.GetNetState(this);
                if (netstate == NetWorkUntil.NO_CONNECTED) {
                    toastShow(this, getString(R.string.main_no_network));
                    return;
                }

                if (!mCoreService.isLogin()) {
                    boolean flag = mCoreService.login(mUserName, mServerIP);
                    DPLog.print("xufan", "login(SDLMainActivity.java:579)-->>" + "flag= " + flag);
                }
                DPLog.print("xufan", "handleMessage(SDLMainActivity.java:259)-->>" + "login username:" + mUserName + " ip:" + mServerIP);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateSafeMode() {
        if  (mRoomNumber == null)
            return;
        DeviceInfoMod device = DBEdit.getPrimaryDevcie(instance, mRoomNumber);
        if (device != null) {
            String target = device.getDevacc();
            try {
                SipControl.getSafeMode(mCoreService, target, mRoomNumber);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else { // 找不到主设备
            if (SafeModeFrament.instance != null)
                SafeModeFrament.instance.getHandler().sendEmptyMessage(SafeModeFrament.MSG_INIT);
            mRoomNumber = null;
            deviceId = -1;
            mIvDeviceOnline.setImageDrawable(getResources().getDrawable(R.mipmap.room_offline));
        }
    }

    private void updateSafeModeInfo(String mode) {
        if ("Home".equals(mode)) {
            mSafeModeNumber = 0;
            if (SafeModeFrament.instance != null) {
                Message msg = new Message();
                msg.what = SafeModeFrament.MSG_UPDATE_SAFEMODE;
                msg.arg1 = mSafeModeNumber;
                SafeModeFrament.instance.getHandler().sendMessage(msg);
            }
        } else if ("Night".equals(mode)) {
            mSafeModeNumber = 1;
            if (SafeModeFrament.instance != null) {
                Message msg = new Message();
                msg.what = SafeModeFrament.MSG_UPDATE_SAFEMODE;
                msg.arg1 = mSafeModeNumber;
                SafeModeFrament.instance.getHandler().sendMessage(msg);
            }
        } else if ("Leave".equals(mode)) {
            mSafeModeNumber = 2;
            if (SafeModeFrament.instance != null) {
                Message msg = new Message();
                msg.what = SafeModeFrament.MSG_UPDATE_SAFEMODE;
                msg.arg1 = mSafeModeNumber;
                SafeModeFrament.instance.getHandler().sendMessage(msg);
            }
        } else if ("UnSafe".equals(mode)) {
            mSafeModeNumber = 3;
            if (SafeModeFrament.instance != null) {
                Message msg = new Message();
                msg.what = SafeModeFrament.MSG_UPDATE_SAFEMODE;
                msg.arg1 = mSafeModeNumber;
                SafeModeFrament.instance.getHandler().sendMessage(msg);
            }
            if (AlarmDialog.getInstance() != null) {
                AlarmDialog.getInstance().finish();
            }
        }
    }

    /**
     * 是否唤醒手机
     */
    public static void systemWakeup(boolean screen) {
        if (screen) {
            instance.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            instance.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            instance.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            phone.systemWakeup(instance);
        } else {
            instance.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            instance.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            instance.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            phone.release();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        String from = intent.getStringExtra(CoreService.INTENT_FROM);
        String msg = intent.getStringExtra(JPushInterface.EXTRA_MESSAGE);
        String extras = intent.getStringExtra(JPushInterface.EXTRA_EXTRA);
        int id = intent.getIntExtra(JPushReceiver.NOTIFY_ID, -1);
        DPLog.print("xufan", "onNewIntent(SDLMainActivity.java:1403)-->>" + "通知id=" + id);
        if (id != -1)
            JPushReceiver.cancelNotification(this, id);
        if (from != null && msg != null) {
            Intent mIntent = new Intent(this, AlarmDialog.class);
            mIntent.putExtra(CoreService.INTENT_FROM, from);
            mIntent.putExtra(JPushInterface.EXTRA_MESSAGE, msg);
            Log.e("xufan", "打开Main msg=" + msg);
            mIntent.putExtra(JPushInterface.EXTRA_EXTRA, extras);
            startActivity(mIntent);
        }
    }



    private NoScrollViewPager mViewpager;
    private ViewPagerIndicator mViewPagerIndicator;
    private List<String> mTitles = Arrays.asList("门口机", "安防模式");
    private List<Fragment> mContents = new ArrayList<Fragment>();// 装载ViewPager数据的List
    /**
     * FragmentPagerAdapter，见名知意，这个适配器就是用来实现Fragment在ViewPager里面进行滑动切换的，因此，
     * 如果我们想实现Fragment的左右滑动，可以选择ViewPager和FragmentPagerAdapter实现。
     * FragmentPagerAdapter拥有自己的缓存策略
     * ，当和ViewPager配合使用的时候，会缓存当前Fragment以及左边一个、右边一个，一共三个Fragment对象。
     * 假如有三个Fragment
     * ,那么在ViewPager初始化之后，3个fragment都会加载完成，中间的Fragment在整个生命周期里面只会加载一次
     * ，当最左边的Fragment处于显示状态
     * ，最右边的Fragment由于超出缓存范围，会被销毁，当再次滑到中间的Fragment的时候，最右边的Fragment会被再次初始化。
     */
    private FragmentPagerAdapter mFAdapter;// ViewPager适配器

    /**
     * 初始化视图
     */
    private void initDatas() {
        mViewpager = (NoScrollViewPager) findViewById(R.id.viewpager);
        mViewPagerIndicator = (ViewPagerIndicator) findViewById(R.id.indicator);
    }

    /**
     * 初始化数据
     */
    private void initViews() {
        Fragment fragment = new DeviceListFragment();
        mContents.add(fragment);
        fragment = new SafeModeFrament();
        mContents.add(fragment);

        // getFragmentManager();
        mFAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {

            @Override
            public int getCount() {
                return mContents.size();
            }

            @Override
            public Fragment getItem(int position) {
                return mContents.get(position);
            }
        };
    }

    private ListView mLvDoorList = null;
    private int deviceId = -1;
    private Dialog dialog = null;
    private final int CLOSE_DIALOG = 110;
    private boolean isInitDialog = false;

    private void showRoomNumberList(boolean isShow) {
        dialog = new Dialog(this, R.style.DPDialog);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view  = inflater.inflate(R.layout.dialog_decive, new LinearLayout(this), false);
        dialog.setContentView(view);
        mLvDoorList = (ListView) view.findViewById(R.id.device_list);

        setTitleArrow(mBtnDoorNumber, false);
        mBtnDoorNumber.setOnClickListener(null);
        List<String> roomNumberList = DBEdit.getRoomNumberList(this);
        if (roomNumberList != null) {
            roomNumberList = MyUtil.removeSameElement(roomNumberList);
            if (deviceId == -1)
                deviceId = 0;
            setRoomNumberList(roomNumberList);
            if (roomNumberList.size() > 1) { // 房号列表大于1 显示列表
                setTitleArrow(mBtnDoorNumber, true);
                mBtnDoorNumber.setOnClickListener(this);
                if (isShow) {
                    Window window = dialog.getWindow();
                    WindowManager.LayoutParams lp = window.getAttributes();
                    window.setGravity(Gravity.TOP);
                    lp.y = 120;
                    window.setAttributes(lp);
                    dialog.show();
                    return;
                }
            }
        } else {
            mBtnDoorNumber.setText("");
        }
        dialog = null;
    }

    private void setRoomNumberList(final List<String> data) {
        ListAdapter adapter = new ArrayAdapter<String>(this, R.layout.item_device_choice, data) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final ChoiceView view;
                if (convertView == null) {
                    view = new ChoiceView(SDLMainActivity.this);
                } else {
                    view = (ChoiceView)convertView;
                }
                view.setText(getItem(position));
                return view;
            }
        };
        mLvDoorList.setAdapter(adapter);

        mLvDoorList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mBtnDoorNumber.setText(data.get(position));
                mRoomNumber = data.get(position);
                DBEdit.setStatusValue(instance, "roomNumber", mRoomNumber);
                if (isInitDialog == false) {
                    handler.sendEmptyMessageDelayed(CLOSE_DIALOG, 350);
                }
                deviceId = position;
                setCallWhiteList();
                try {
                    if (mCoreService != null)
                        if (mCoreService.isLogin())
                            updateSafeModeFragment();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                startTimerOfCheckDeviceOnline();
            }
        });

        if (deviceId != -1) {
            if (deviceId >= data.size()) {
                deviceId = 0;
            } else if(!data.get(deviceId).equals(mRoomNumber)) {
                for (int i = 0; i < data.size(); i++) {
                    if (data.get(i).equals(mRoomNumber)) {
                        deviceId = i;
                        break;
                    }
                }
            }
            isInitDialog = true;
            mLvDoorList.performItemClick(mLvDoorList.getChildAt(deviceId), deviceId, mLvDoorList.getItemIdAtPosition(deviceId));
            isInitDialog = false;
        }
    }

    // 初始化房间列表(两种情况需要刷新该列表 一启动程序时加载， 二绑定完设备)
    public void initRoomNumberList() {
        showRoomNumberList(false);
    }

    // 设置箭头显示 or 不显示
    private void setTitleArrow(Button mBtn, boolean isShow) {
        if (isShow) {
            // 修改下拉箭头大小
            Drawable drawable = getResources().getDrawable(R.mipmap.png_title_arrow);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth() - 20, drawable.getIntrinsicHeight() - 20);
            mBtn.setCompoundDrawables(null, null, drawable, null);
        } else {
            mBtn.setCompoundDrawables(null, null, null, null);
        }
    }

    public void setCallWhiteList() {
        if (DeviceListFragment.instance != null)
            DeviceListFragment.instance.init_data();

        // 刷新白名单
        if (mCoreService == null)
            return;
        List<DeviceInfoMod> deviceList = DBEdit.getDeviceList(instance);
        List<String> deviceListCopy = new ArrayList<>();
        if (deviceList != null) {
            for (int i = 0; i < deviceList.size(); i++) {
                deviceListCopy.add(deviceList.get(i).getDevacc());
            }
            if (deviceListCopy.size() < 1) {
                deviceListCopy.add(null);
            }
        } else {
            deviceListCopy.add(null);
        }
        try {
            mCoreService.setCallWhiteList(deviceListCopy);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void updateSafeModeFragment() {
        Message msg = new Message();
        msg.what = SDLMainActivity.MSG_GET_SAFEMODE;
        SDLMainActivity.getInstance().handler.sendMessage(msg);
    }

    private void updateDoorNumberAndSafeModeAfterDeleteDevice() {
        showRoomNumberList(false);
        updateSafeModeFragment();
    }

    private void checkDeviceOnline() {
        if (mRoomNumber == null)
            return;
        DeviceInfoMod device = DBEdit.getPrimaryDevcie(instance, mRoomNumber);
        if (device != null) {
            String target = device.getDevacc();
            try {
                if (mCoreService != null && mCoreService.isLogin())
                    SipControl.checkOnline(mCoreService, target);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            mRoomNumber = null;
            deviceId = -1;
            mIvDeviceOnline.setImageDrawable(getResources().getDrawable(R.mipmap.room_offline));
        }
    }

    private void startTimerOfCheckDeviceOnline() {
        stopTimerOfCheckDeviceOnline();
        if (timerOfCheckDeviceOnline == null)
            timerOfCheckDeviceOnline = new Timer();
        if (timerTaskOfCheckDeviceOnline == null) {
            timerTaskOfCheckDeviceOnline = new TimerTask() {
                @Override
                public void run() {
                    checkDeviceOnline();
                }
            };
        }
        timerOfCheckDeviceOnline.schedule(timerTaskOfCheckDeviceOnline, 250, 30 * 1000);
    }

    private void stopTimerOfCheckDeviceOnline() {
        if (timerOfCheckDeviceOnline != null) {
            timerOfCheckDeviceOnline.cancel();
            timerOfCheckDeviceOnline = null;
        }
        if (timerTaskOfCheckDeviceOnline != null) {
            timerTaskOfCheckDeviceOnline.cancel();
            timerTaskOfCheckDeviceOnline = null;
        }
    }
}