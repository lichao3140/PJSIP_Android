package com.dpower.cintercom.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Window;

import com.dpower.cintercom.R;
import com.dpower.cintercom.SDLMainActivity;
import com.dpower.cintercom.app.UserConfig;
import com.dpower.cintercom.util.DpTypeTable;
import com.dpower.push.jpush.broadcast.JPushReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import cn.jpush.android.api.JPushInterface;

import static com.dpower.dpsiplib.CoreService.INTENT_FROM;

public class AlarmDialog extends Activity {
    private MediaPlayer mRingbell = null;
    private static AlarmDialog instance = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_alarm);
        instance = this;

        SDLMainActivity.systemWakeup(true);
        if (mRingbell == null)
            mRingbell = MediaPlayer.create(this, R.raw.alarm);
        mRingbell.setLooping(true);
        mRingbell.start();

        Intent intent = getIntent();
        initAlarmMsg(intent);
    }

    // 显示报警消息
    private void showAlarm(String room, String area ,String type, String time) {
        String content = room + getString(R.string.main_room_number);
        content += DpTypeTable.getArea(this, area) + DpTypeTable.getType(this, type);
        content += getString(R.string.main_alarm) + "\n" + time;
        Log.e("xufan", "AlarmDialog content=" + content);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.main_alarm));
        builder.setMessage(content);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.alarm_dialog_ignore, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();
            }
        });
        builder.setNegativeButton(R.string.safe_noProtected, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Message msg = new Message();
                msg.what = SDLMainActivity.MSG_SET_SAFEMODE;
                msg.obj = "UnSafe";
                SDLMainActivity.getInstance().handler.sendMessage(msg);
                dialog.cancel();
                finish();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRingbell != null && mRingbell.isPlaying()) {
            mRingbell.stop();
            mRingbell = null;
        }
        SDLMainActivity.systemWakeup(false);
        instance = null;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        initAlarmMsg(intent);
    }

    // 初始化报警消息
    private void initAlarmMsg(Intent intent) {
        if (JPushReceiver.class.getName().equals(intent.getStringExtra(INTENT_FROM)) ) {
            Log.i("aa", "JPushReceiver send startActivity");
            String message = intent.getStringExtra(JPushInterface.EXTRA_MESSAGE);
            String extras = intent.getStringExtra(JPushInterface.EXTRA_EXTRA);
            Log.e("xufan", "报警:" + message);
            try {
                JSONObject json = new JSONObject(message);
                String aps = json.getString("aps");
                json = new JSONObject(aps);
                String alert = json.getString("alert");
                json = new JSONObject(alert);
                String room = json.getString("body");
                String place = json.getString("place");
                json = new JSONObject(place);
                String area = json.getString("area");
                String type = json.getString("type");
                String time = json.getString("time");
                showAlarm(room, area, type, time);
            } catch (JSONException e) {
                e.printStackTrace();
                finish();
            }
        } else
            finish();
    }

    public static AlarmDialog getInstance() {
        return instance;
    }
}
