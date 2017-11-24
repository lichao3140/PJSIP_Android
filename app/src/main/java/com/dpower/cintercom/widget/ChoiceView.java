package com.dpower.cintercom.widget;

import android.content.Context;
import android.view.View;
import android.widget.Checkable;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.dpower.cintercom.R;

public class ChoiceView extends FrameLayout implements Checkable {
	private TextView mTextView;
	private RadioButton mRadioButton;
	
	public ChoiceView(Context context) {
		super(context);
		View.inflate(context, R.layout.item_device_choice, this);
		mTextView = (TextView) findViewById(R.id.device_name);
		mRadioButton = (RadioButton) findViewById(R.id.device_checked);
	}
	
	public void setText(String text) {
		mTextView.setText(text);
	}
	
	@Override
	public boolean isChecked() {
		return mRadioButton.isChecked();
	}

	@Override
	public void setChecked(boolean checked) {
		mRadioButton.setChecked(checked);
	}

	@Override
	public void toggle() {
		mRadioButton.toggle();
	}
}
