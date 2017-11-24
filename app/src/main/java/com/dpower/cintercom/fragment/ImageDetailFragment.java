package com.dpower.cintercom.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dpower.cintercom.R;

import phtotoview.PhotoViewAttacher;

public class ImageDetailFragment extends Fragment {
	private String mImageUrl;
	private String mTime;
	private ImageView mImageView;
	private TextView mTvTime;
	private ProgressBar progressBar;
	private PhotoViewAttacher mAttacher;
	
	public static ImageDetailFragment newInstance(String imageUrl, String time) {
		final ImageDetailFragment f = new ImageDetailFragment();
		
		final Bundle args = new Bundle();
		args.putString("url", imageUrl);
		args.putString("time", time);
		f.setArguments(args);
		
		return f;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mImageUrl = getArguments() != null ? getArguments().getString("url") : null;
		mTime = getArguments() != null ? getArguments().getString("time") : null;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.image_detail_fragment, container, false);
		mImageView = (ImageView) v.findViewById(R.id.image);
		mTvTime = (TextView) v.findViewById(R.id.name);
		mAttacher = new PhotoViewAttacher(mImageView);
		
		mAttacher.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
			@Override
			public void onPhotoTap(View view, float x, float y) {
				getActivity().finish();
			}
		});	
		progressBar = (ProgressBar) v.findViewById(R.id.loading);
		
		mImageView.setImageDrawable(Drawable.createFromPath(mImageUrl));
		mTvTime.setText(mTime);
		return v;
	}
}
