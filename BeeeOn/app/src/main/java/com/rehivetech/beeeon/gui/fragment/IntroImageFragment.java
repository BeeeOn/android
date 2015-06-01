package com.rehivetech.beeeon.gui.fragment;

import android.content.Context;
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
import com.rehivetech.beeeon.gui.activity.AddSensorActivity;
import com.rehivetech.beeeon.gui.activity.IntroActivity;

public final class IntroImageFragment extends Fragment {
	private static final String KEY_CONTENT = "TestFragment:Content";
	private static final String KEY_TEXT = "TestFragment:Text";

	public static IntroImageFragment newInstance(int resourceImg, String text) {
		IntroImageFragment fragment = new IntroImageFragment();

		Bundle args = new Bundle();
		args.putInt(KEY_CONTENT, resourceImg);
		args.putString(KEY_TEXT, text);
		fragment.setArguments(args);

		return fragment;
	}

	private int mImageRes;
	private String mText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mImageRes = getArguments().getInt(KEY_CONTENT);
		mText = getArguments().getString(KEY_TEXT);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		TextView text = new TextView(getActivity());
		text.setPadding(20, 20, 20, 20);
		text.setText(mText);
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
		layout.addView(text);
		layout.addView(image);

		return layout;
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser) {
			Context mActivity = getActivity();
			if (mActivity instanceof AddSensorActivity) {
				((AddSensorActivity) mActivity).resetBtn();
			} else if (mActivity instanceof IntroActivity) {
				if (((IntroActivity) mActivity).isLastFragment()) {
					((IntroActivity) mActivity).setLastFragmentBtn();
				} else {
					((IntroActivity) mActivity).resetBtn();
				}
			}
		}

	}

}
