package com.dpower.cintercom.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.dpower.cintercom.R;
import com.dpower.cintercom.SDLMainActivity;
import com.dpower.cintercom.domain.DeviceInfoMod;
import com.dpower.cintercom.util.DBEdit;
import com.dpower.utils.MyUtil;

import java.util.List;

import swipelist.adapter.SwipeAdapter;
import swipelist.widget.SwipeListView;

import static com.dpower.cintercom.SDLMainActivity.MSG_START_GUIDEVIEW;
import static com.dpower.cintercom.SDLMainActivity.MSG_START_LOGININ;
import static com.dpower.cintercom.SDLMainActivity.MSG_START_MONITOR;
import static com.dpower.cintercom.SDLMainActivity.mCoreService;
import static com.dpower.cintercom.util.ToastUtil.toastShow;

public class DeviceListFragment extends Fragment{
    private FrameLayout mLayoutTitle = null;
    private LinearLayout mLayoutInfo = null;
    private Button mBtnMonitor = null;

    public static final int MSG_DELETE_DEVICE = 1002;

    private SwipeListView mListView;
    private SwipeAdapter mAdapter;

    public static DeviceListFragment instance = null;
    private static MyHandler handler = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rooView = inflater.inflate(R.layout.activity_main, container, false);
        init_id(rooView);
        init_data();
        instance = this;
        handler = new MyHandler();
        return rooView;
    }

    private void init_id(View rootView) {
        mLayoutTitle = (FrameLayout) rootView.findViewById(R.id.main_layout_title);
        mLayoutTitle.setVisibility(View.GONE);

        mLayoutInfo = (LinearLayout) rootView.findViewById(R.id.main_layout_info);
        mBtnMonitor = (Button) rootView.findViewById(R.id.monitor_btn_accept);
        mListView = (SwipeListView) rootView.findViewById(R.id.listview);
    }

    public void init_data() {
        mListView.setVisibility(View.VISIBLE);
        mBtnMonitor.setVisibility(View.GONE);

        mLayoutInfo.setVisibility(View.GONE);

        if (SDLMainActivity.mRoomNumber == null)
            return;
        List<DeviceInfoMod> list = DBEdit.getDeviceListByRoomNumber(getActivity(), SDLMainActivity.mRoomNumber);
        if (list != null && list.size() > 0) {
            mAdapter = new SwipeAdapter(getActivity(), list, mListView.getRightViewWidth(), mListView);
            mListView.setAdapter(mAdapter);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (!MyUtil.isNetworkConnected(getActivity())) {
                        toastShow(getActivity(), getString(R.string.main_no_network));
                        return;
                    }
                    try {
                        if (!mCoreService.isLogin()) {
                            toastShow(getActivity(), getString(R.string.main_unlogin));
                            handler.sendEmptyMessage(MSG_START_LOGININ);
                            return;
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
//                    mAdapter.monitorDevcie(position);
                    DeviceInfoMod device = (DeviceInfoMod) mAdapter.getItem(position);
                    DBEdit.setStatusValue(getActivity(), "target_info", device.getDevnote());
                    DBEdit.setStatusValue(getActivity(), "target_user", device.getDevacc());
                    SDLMainActivity.handler.sendEmptyMessage(MSG_START_MONITOR);
                }
            });
        }
        if (list == null || list.size() < 1) {
            SDLMainActivity.handler.sendEmptyMessageDelayed(MSG_START_GUIDEVIEW, 100);
        }
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
                case MSG_DELETE_DEVICE:
                    mAdapter.deleSelected();
                    mListView.onDeleteItemClick();
                    mAdapter.notifyDataSetChanged();
                    SDLMainActivity.getInstance().initRoomNumberList();
                    break;
            }
        }
    }

    public static Handler getHandler() {
        return handler;
    }
}
