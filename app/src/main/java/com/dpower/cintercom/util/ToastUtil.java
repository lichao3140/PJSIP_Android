package com.dpower.cintercom.util;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {
	private static Toast toast = null; // 用于判断
	
	/**
	 * Toast提示
	 * @param context
	 * @param text
	 */
	public static void toastShow(Context context, String text) {
		if (toast == null) {
			toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
		} else {
			toast.setText(text);
		}
		toast.show();
	}
}
