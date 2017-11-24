package com.dpower.cintercom.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;

import com.dpower.cintercom.R;
import com.dpower.cintercom.SDLMainActivity;
import com.dpower.cintercom.adapter.SafeAdapter;
import com.dpower.cintercom.util.DPLog;

public class SafeModeFrament extends Fragment implements AdapterView.OnItemClickListener{
    private GridView mGvTable = null;
    private FrameLayout mLayoutTitle = null;

    public static SafeModeFrament instance = null;

    public static final int MODE_UNKNOWN = -1; // 未知
    public static final int MODE_ATHOME = 0; // 在家
    public static final int MODE_REST = 1; // 休息
    public static final int MODE_LEAVEHOME = 2; // 离家
    public static final int MODE_NOPROTECTED = 3; // 撤防

    public static final int MSG_UPDATE_SAFEMODE = 1001;
    public static final int MSG_INIT = 1002;
    public static final int MSG_SHOW_PROGRESS = 1003;

    private int mCurrentSalfeSelected = MODE_UNKNOWN;
    private static MyHandler handler = null;
    private SafeAdapter mSafeAdapter = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rooView = inflater.inflate(R.layout.activity_modecontrol, container, false);
        init_id(rooView);
        instance = this;
        handler = new MyHandler();
        return rooView;
    }

    private void init_id(View rootView) {
        mGvTable = (GridView) rootView.findViewById(R.id.modecontrol_gv_table);
        mSafeAdapter = new SafeAdapter(getActivity());
        mGvTable.setAdapter(mSafeAdapter);
        mGvTable.setOnItemClickListener(SafeModeFrament.this);
        mLayoutTitle = (FrameLayout) rootView.findViewById(R.id.modeconrol_layout_title);
        mLayoutTitle.setVisibility(View.GONE);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DPLog.print("xufan", "onItemClick(SafeModeFrament.java:41)-->>" + "点击position=" + position);
//        if (mCurrentSalfeSelected != position) {
            String mode = "";
            switch (position) {
                case MODE_ATHOME:
                    mode = "Home";
                    break;
                case MODE_REST:
                    mode = "Night";
                    break;
                case MODE_LEAVEHOME:
                    mode = "Leave";
                    break;
                case MODE_NOPROTECTED:
                    mode = "UnSafe";
                    break;
            }
            Message msg = new Message();
            msg.what = SDLMainActivity.MSG_SET_SAFEMODE;
            msg.obj = mode;
            SDLMainActivity.getInstance().handler.sendMessage(msg);
            handler.removeCallbacks(hideProgress);
            handler.postDelayed(hideProgress, 5 * 1000);
//            setSafeMode(position);
//        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
    }

    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what) {
                case MSG_UPDATE_SAFEMODE:
//                    hideProgress();
                    int mode = msg.arg1;
                    setSafeMode(mode);
                    SDLMainActivity.getInstance().hideProgress();
                    break;
                case MSG_INIT:
                    mSafeAdapter = new SafeAdapter(getActivity());
                    mGvTable.setAdapter(mSafeAdapter);
                    break;
                case MSG_SHOW_PROGRESS:
                    SDLMainActivity.getInstance().showProgress(getActivity(), false);
                    break;
            }
        }
    }

    public void setSafeMode(int mode) {
        mSafeAdapter.setCurrentSelected(mode);
        mSafeAdapter.notifyDataSetChanged();
        mCurrentSalfeSelected = mode;
    }

    public static Handler getHandler() {
        return handler;
    }

    Runnable hideProgress = new Runnable() {
        @Override
        public void run() {
            SDLMainActivity.getInstance().hideProgress();
        }
    };
 }
