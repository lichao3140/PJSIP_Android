package swipelist.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dpower.cintercom.R;
import com.dpower.cintercom.SDLMainActivity;
import com.dpower.cintercom.activity.VideoActivity;
import com.dpower.cintercom.app.UserConfig;
import com.dpower.cintercom.domain.DeviceInfoMod;
import com.dpower.cintercom.util.DBEdit;
import com.dpower.cintercom.util.DPLog;
import com.dpower.cintercom.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import swipelist.interfaces.DeleteClickListener;
import swipelist.widget.SwipeListView;

public class SwipeAdapter extends BaseAdapter {
    /**
     * 上下文对象
     */
    private Context mContext = null;
    private List<DeviceInfoMod> list = null;

    private int mRightWidth = 0;

    private SwipeListView swipeListView;
    private int delPosition;

    /**
     *
     */
    public SwipeAdapter(Context ctx, List<DeviceInfoMod> list, int rightWidth, SwipeListView swipeListView) {
        mContext = ctx;
        this.list = list;
        this.swipeListView = swipeListView;
        mRightWidth = rightWidth;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_devicelist, parent, false);
            holder = new ViewHolder();
            holder.item_left = (RelativeLayout)convertView.findViewById(R.id.item_left);
            holder.item_right = (RelativeLayout)convertView.findViewById(R.id.item_right);

            holder.tv_name = (TextView)convertView.findViewById(R.id.devicelist_tv_devicename);
            holder.tv_id = (TextView)convertView.findViewById(R.id.devicelist_tv_deviceid);
            holder.tv_dele =  (TextView)convertView.findViewById(R.id.devicelist_btn_dele);

            convertView.setTag(holder);
        } else {// 有直接获得ViewHolder
            holder = (ViewHolder)convertView.getTag();
        }

        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        holder.item_left.setLayoutParams(lp1);
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(mRightWidth, LinearLayout.LayoutParams.MATCH_PARENT);
        holder.item_right.setLayoutParams(lp2);

        DeviceInfoMod mod = list.get(position);

        holder.tv_name.setText(mod.getDevnote());
        holder.tv_id.setText(mod.getDevacc());

        holder.tv_dele.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (swipeListView instanceof DeleteClickListener) {
                    DBEdit.setStatusValue(mContext, "sip_target", list.get(position).getDevacc());
                    Message msg = new Message();
                    msg.what = SDLMainActivity.MSG_UNBIND_DEVICE;
                    msg.obj = list.get(position).getDevacc();
                    SDLMainActivity.handler.sendMessage(msg);
//            		list.remove(position);
                    delPosition = position;
                }
            }
        });
        return convertView;
    }

    static class ViewHolder {
        RelativeLayout item_left;
        RelativeLayout item_right;

        TextView tv_name;
        TextView tv_id;

        TextView tv_dele;
    }

    public List<DeviceInfoMod> getDeviceList() {
        return list;
    }

    // 删除设备
    public void deleSelected() {
        List<DeviceInfoMod> selectedList = new ArrayList<DeviceInfoMod>();
        selectedList.add(list.get(delPosition));
        if (selectedList.size() < 1) {
            return;
        }
        list.removeAll(selectedList);
        // FIXME 删除数据库中的设备信息
        DBEdit.removeDeviceByRoomNumber(mContext, SDLMainActivity.mRoomNumber);
        for (int i = 0; i < list.size(); i++) {
            DBEdit.AddDevice(mContext, list.get(i));
        }
    }

    // 全选
    public void selectAll() {
        for (int i = 0; i < list.size(); i++) {
// 			list.get(i).isSelected = true;
        }
    }

    // 全部删除
    public void deleAll() {
        list.removeAll(list);
        DBEdit.removeAllDevice(mContext);
        DBEdit.setStatusValue(mContext, "sip_target", "");
    }

    // 监视设备
    public void monitorDevcie(int position) {
        DeviceInfoMod mod = list.get(position);
        String devacc = mod.getDevacc();
        DPLog.print("xufan", "pos=" + position + " devacc=" + devacc);
        if (devacc == null || (devacc.length() < 1)) {
            ToastUtil.toastShow(mContext, mContext.getString(R.string.main_device_unbind));
            return;
        }
        Intent intent = new Intent(mContext, VideoActivity.class);
        DBEdit.setStatusValue(mContext, "sip_target", devacc);;
        mContext.startActivity(intent);
        ((Activity) mContext).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }
}
