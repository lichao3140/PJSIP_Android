package com.dpower.cintercom.activity;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Window;
import android.widget.TextView;

import com.dpower.cintercom.R;
import com.dpower.cintercom.fragment.ImageDetailFragment;
import com.dpower.cintercom.util.DPLog;
import com.dpower.cintercom.widget.HackyViewPager;

/**
 * 图片查看器
 */
public class ImagePagerActivity extends FragmentActivity {
	private static final String STATE_POSITION = "STATE_POSITION";
	public static final String EXTRA_IMAGE_INDEX = "image_index";
	public static final String EXTRA_IMAGE_URLS = "image_urls";
	public static final String EXTRA_IMAGE_TIMELIST = "image_time_list";
	
	private HackyViewPager mPager;
	private int pagerPosition;
	private TextView indicator;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_detail_pager);
		
		pagerPosition = getIntent().getIntExtra(EXTRA_IMAGE_INDEX, 0);
		ArrayList<String> urls = getIntent().getStringArrayListExtra(EXTRA_IMAGE_URLS);
		ArrayList<String> timeList = getIntent().getStringArrayListExtra(EXTRA_IMAGE_TIMELIST);
		
		mPager = (HackyViewPager) findViewById(R.id.pager);
		ImagePagerAdapter mAdapter = new ImagePagerAdapter(getSupportFragmentManager(), urls, timeList);
		mPager.setAdapter(mAdapter);
		indicator = (TextView) findViewById(R.id.indicator);
		
		if (1 <= urls.size()) {
			CharSequence text = getString(R.string.viewpager_indicator, 1, mPager.getAdapter().getCount());
			indicator.setText(text);
		}
		
		// 更新下标
		mPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageScrollStateChanged(int arg0) {	
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageSelected(int arg0) {
				CharSequence text = getString(R.string.viewpager_indicator, arg0 + 1, mPager.getAdapter().getCount());
				indicator.setText(text);
				DPLog.print("xufan", "page selected text=" + text + " " + (arg0 + 1));
			}
			
		});
		if (savedInstanceState != null) {
			pagerPosition = savedInstanceState.getInt(STATE_POSITION);
		}
		
		mPager.setCurrentItem(pagerPosition);
	}
	
	private class ImagePagerAdapter extends FragmentStatePagerAdapter {
		public ArrayList<String> fileList;
		public ArrayList<String> timeList;
		
		public ImagePagerAdapter(FragmentManager fm, ArrayList<String> fileList, ArrayList<String> timeList) {
			super(fm);
			this.fileList = fileList;
			this.timeList = timeList;
		}

		@Override
		public Fragment getItem(int position) {
			String url = fileList.get(position);
			String time = timeList.get(position);
			DPLog.print("xufan", "adapter position=" + (position + 1));
			return ImageDetailFragment.newInstance(url, time);
		}

		@Override
		public int getCount() {
			return fileList == null ? 0 : fileList.size();
		}
		
	}
}
