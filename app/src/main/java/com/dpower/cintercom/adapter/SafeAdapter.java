package com.dpower.cintercom.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dpower.cintercom.R;
import com.dpower.cintercom.fragment.SafeModeFrament;
import com.dpower.cintercom.util.StateDrawableUtil;

public class SafeAdapter extends BaseAdapter {
    private LayoutInflater inflater = null;
    private int currentSelected = SafeModeFrament.MODE_UNKNOWN;

    public SafeAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    public void setCurrentSelected(int position) {
        currentSelected = position;
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.adapter_btn_item, parent, false);
        }
        ImageView iv = (ImageView) convertView.findViewById(R.id.modeItem_iv_mode);
        TextView tv = (TextView) convertView.findViewById(R.id.modeItem_tv_mode);
        setImageDrawable(iv, tv, position);
        if (position == currentSelected) {
            iv.setSelected(true);
        } else {
            iv.setSelected(false);
        }
        return convertView;
    }

    private void setImageDrawable(ImageView iv, TextView tv, int position) {
        switch (position) {
            case SafeModeFrament.MODE_ATHOME:
                StateDrawableUtil.setImageDrawable(iv,
                        R.mipmap.png_security_athome_normal,
                        R.mipmap.png_security_athome_selected);
                tv.setText(R.string.safe_atHome);
                break;
            case SafeModeFrament.MODE_REST:
                StateDrawableUtil.setImageDrawable(iv,
                        R.mipmap.png_security_rest_normal,
                        R.mipmap.png_security_rest_selected);
                tv.setText(R.string.safe_rest);
                break;
            case SafeModeFrament.MODE_LEAVEHOME:
                StateDrawableUtil.setImageDrawable(iv,
                        R.mipmap.png_security_leavehome_normal,
                        R.mipmap.png_security_leavehome_selected);
                tv.setText(R.string.safe_leaveHome);
                break;
            case SafeModeFrament.MODE_NOPROTECTED:
                StateDrawableUtil.setImageDrawable(iv,
                        R.mipmap.png_security_noprotected_normal,
                        R.mipmap.png_security_noprotected_selected);
                tv.setText(R.string.safe_noProtected);
                break;
        }
    }
}
