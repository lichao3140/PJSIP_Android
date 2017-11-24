package com.dpower.cintercom.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

public class StateDrawableUtil {
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @SuppressWarnings("deprecation")
    public static void setBackground(View v, int idNormal, int idSelected) {
        if (isEarlyVersion(Build.VERSION_CODES.JELLY_BEAN)) {
            v.setBackgroundDrawable(setStateDrawable(v.getContext(), idNormal,
                    idSelected));
        } else {
            v.setBackground(setStateDrawable(v.getContext(), idNormal,
                    idSelected));
        }
    }

    public static void setLeftCompound(TextView tv, int idNormal, int idSelected) {
        tv.setCompoundDrawables(
                setStateDrawable(tv.getContext(), idNormal, idSelected), null,
                null, null);
    }

    public static void setImageDrawable(ImageView iv, int idNormal,
                                        int idSelected) {
        iv.setImageDrawable(setStateDrawable(iv.getContext(), idNormal,
                idSelected));
    }

    public static StateListDrawable setStateDrawable(Context context,
                                                     int idNormal, int idSelected) {
        StateListDrawable sd = new StateListDrawable();
        Drawable normal = idNormal == -1 ? null : context.getResources()
                .getDrawable(idNormal);
        Drawable pressed = idSelected == -1 ? null : context.getResources()
                .getDrawable(idSelected);
        normal.setBounds(0, 0, normal.getIntrinsicWidth(),
                normal.getIntrinsicHeight());
        pressed.setBounds(0, 0, normal.getIntrinsicWidth(),
                normal.getIntrinsicHeight());
        sd.addState(new int[] { android.R.attr.state_selected,
                android.R.attr.state_enabled }, pressed);
        sd.addState(new int[] { android.R.attr.state_selected }, pressed);
        sd.addState(new int[] { android.R.attr.state_enabled }, normal);
        sd.addState(new int[] {}, normal);
        return sd;
    }

    public static void setButtonDrawable(CompoundButton btn, int idNormal,
                                         int idSelected) {
        btn.setButtonDrawable(setCheckedStateDrawable(btn.getContext(),
                idNormal, idSelected));
    }

    public static StateListDrawable setCheckedStateDrawable(Context context,
                                                            int noChecked, int isChecked) {
        StateListDrawable sd = new StateListDrawable();
        Drawable normal = noChecked == -1 ? null : context.getResources()
                .getDrawable(noChecked);
        Drawable checked = isChecked == -1 ? null : context.getResources()
                .getDrawable(isChecked);
        normal.setBounds(0, 0, normal.getIntrinsicWidth(),
                normal.getIntrinsicHeight());
        checked.setBounds(0, 0, normal.getIntrinsicWidth(),
                normal.getIntrinsicHeight());
        sd.addState(new int[] { android.R.attr.state_checked,
                android.R.attr.state_checkable }, checked);
        sd.addState(new int[] { android.R.attr.state_checked }, checked);
        sd.addState(new int[] { android.R.attr.state_checkable }, normal);
        sd.addState(new int[] {}, normal);
        return sd;
    }

    public static void setTextColor(TextView tv, int idNormal, int idSelected) {
        tv.setTextColor(setStateColor(tv.getContext(), idNormal, idSelected));
    }

    public static ColorStateList setStateColor(Context context, int idNormal,
                                               int idSelected) {
        int[][] states = {
                { android.R.attr.state_selected, android.R.attr.state_enabled },
                { android.R.attr.state_selected },
                { android.R.attr.state_enabled }, {} };
        int[] colors = { idSelected, idSelected, idNormal, idNormal };
        ColorStateList cl = new ColorStateList(states, colors);
        return cl;
    }

    public static boolean isEarlyVersion(int version) {
        return Build.VERSION.SDK_INT < version;
    }
}
