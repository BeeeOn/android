package com.rehivetech.beeeon.activity.fragment;

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
import com.rehivetech.beeeon.activity.AddAdapterActivity;
import com.rehivetech.beeeon.activity.AddSensorActivity;
import com.rehivetech.beeeon.activity.IntroActivity;

public final class IntroImageFragment extends Fragment {
	private static final String KEY_CONTENT = "TestFragment:Content";
	private static final String KEY_TEXT = "TestFragment:Text";

	public static IntroImageFragment newInstance(int resourceImg, String text) {
		IntroImageFragment fragment = new IntroImageFragment();

		fragment.mContent = resourceImg;
		fragment.mText = text;

		return fragment;
	}

	private int mContent = 0;
	private String mText = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
			mContent = savedInstanceState.getInt(KEY_CONTENT);
			mText = savedInstanceState.getString(KEY_TEXT);
		}
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
		image.setImageResource(mContent);
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
			if (mActivity instanceof AddAdapterActivity) {
				((AddAdapterActivity) mActivity).resetBtn();
			}
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

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(KEY_CONTENT, mContent);
		outState.putString(KEY_TEXT, mText);
	}
}
