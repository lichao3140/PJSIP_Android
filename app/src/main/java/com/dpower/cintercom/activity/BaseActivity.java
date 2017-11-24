package com.dpower.cintercom.activity;

import com.dpower.cintercom.R;
import com.dpower.cintercom.impl.OnActionBackListener;
import com.dpower.cintercom.util.ToastUtil;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class BaseActivity extends FragmentActivity implements OnActionBackListener {
	private Dialog progressDialog; // 文字提示加载框

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 5.0以上状态栏颜色
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			window.setStatusBarColor(Color.TRANSPARENT);
//			window.setStatusBarColor(getResources().getColor(R.color.title_bg));
		}
	}
	
	/**
	 * Toast提示
	 * @param context
	 * @param text
	 */
	public void toastShow(final Context context, final String text) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				ToastUtil.toastShow(context, text);
			}
		});
	}

	public AlertDialog.Builder showDialog(final String title, final String content) {
		AlertDialog.Builder builder = new AlertDialog.Builder(BaseActivity.this);
		builder.setTitle(title);
		builder.setMessage(content);
		builder.setCancelable(false);
		builder.setPositiveButton(R.string.notify_ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.create().show();

		return builder;
	}
	
	public void showProgress(final Context context, final boolean isCancelable) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (progressDialog == null) {
					progressDialog = new Dialog(context,R.style.text_progress_dialog);
					progressDialog.setContentView(R.layout.text_progress_dialog);
					progressDialog.setCancelable(true);
					progressDialog.setCanceledOnTouchOutside(isCancelable);
					progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
					TextView msg = (TextView) progressDialog.findViewById(R.id.id_tv_loadingmsg);
					msg.setVisibility(View.GONE);
					progressDialog.show();
				}
			}
		});
	}
	
	public void hideProgress() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {		
				hideTextProgress();
			}
		});
	}
	
	public void showTextProgress(final int textId) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (progressDialog == null) {
					progressDialog = new Dialog(BaseActivity.this,R.style.text_progress_dialog);
			        progressDialog.setContentView(R.layout.text_progress_dialog);
			        progressDialog.setCancelable(false);
			        progressDialog.setCanceledOnTouchOutside(false);
			        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
			        TextView msg = (TextView) progressDialog.findViewById(R.id.id_tv_loadingmsg);
			        msg.setText(textId);
			        progressDialog.show();
				}
			}
		});
	}
	
	public void hideTextProgress() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (progressDialog != null) {
					progressDialog.dismiss();
					progressDialog = null;
				}
			}
		});
	}

	@Override
	public boolean onActionBack(int type) {
		switch (type) {
			case BACK_TYPE_PROGRESS:
				hideProgress();
				break;
			default:
				break;
		}
		return false;
	}
}
