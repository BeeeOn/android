package com.rehivetech.beeeon.gui.fragment;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.activity.IntroActivity;

public final class IntroImageFragment extends Fragment {
	private static final String KEY_IMAGE_RES = "ImageRes";
	private static final String KEY_TEXT_RES = "TextRes";
	private static final String KEY_TITLE_RES = "TitleRes";

	public static IntroImageFragment newInstance(int resourceImg, int textRes, int titleRes) {
		IntroImageFragment fragment = new IntroImageFragment();

		Bundle args = new Bundle();
		args.putInt(KEY_IMAGE_RES, resourceImg);
		args.putInt(KEY_TEXT_RES, textRes);
		args.putInt(KEY_TITLE_RES,titleRes);
		fragment.setArguments(args);

		return fragment;
	}

	private int mImageRes;
	private int mTextRes;
	private int mTitleRes;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mImageRes = getArguments().getInt(KEY_IMAGE_RES);
		mTextRes = getArguments().getInt(KEY_TEXT_RES);
		mTitleRes = getArguments().getInt(KEY_TITLE_RES);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		TextView text = new TextView(getActivity());
		text.setPadding(20, 20, 20, 20);
		text.setText(mTextRes);
		text.setGravity(Gravity.CENTER_HORIZONTAL);
		text.setTextSize(20);
		text.setTextColor(getResources().getColor(R.color.white));

		ImageView image = new ImageView(getActivity());
		image.setImageResource(mImageRes);
		image.setPadding(20, 20, 20, 20);


		LinearLayout layout = new LinearLayout(getActivity());
		layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		layout.setGravity(Gravity.CENTER);
		layout.setOrientation(LinearLayout.VERTICAL);

		if (mTitleRes != 0) {
			// 0 is sent from intro Activity, there is no title needed
			TextView title = new TextView(getActivity());
			title.setText(mTitleRes);
			title.setTextSize(35);
			title.setTypeface(null,Typeface.BOLD);
			title.setGravity(Gravity.CENTER_HORIZONTAL);
			title.setPadding(0,0,0,0);
			title.setTextColor(getResources().getColor(R.color.white));
			layout.addView(title);
		}

		layout.addView(text);
		layout.addView(image);

		return layout;
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser) {
			Context mActivity = getActivity();
			if (mActivity instanceof IntroActivity) {
				if (((IntroActivity) mActivity).isLastFragment()) {
					((IntroActivity) mActivity).setLastFragmentBtn();
				} else {
					((IntroActivity) mActivity).resetBtn();
				}
			}
		}

	}

	public static class ImageTextPair {
		public final int imageRes;
		public final int textRes;
		public final int titleRes;

		public ImageTextPair(int imageRes, int textRes, int titleRes) {
			this.imageRes = imageRes;
			this.textRes = textRes;
			this.titleRes = titleRes;
		}
	}

}
