package com.rehivetech.beeeon.widget;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.widget.configuration.WidgetConfigurationActivity;

/**
 * Created by Tomáš on 13. 4. 2015.
 */
public class WidgetClockFragment extends Fragment {
	private static final String TAG = WidgetClockFragment.class.getSimpleName();

	private WidgetConfigurationActivity mActivity;
	private View mView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mActivity = (WidgetConfigurationActivity) getActivity();
		if (!(mActivity instanceof WidgetConfigurationActivity)) {
			throw new IllegalStateException(String.format("Activity holding %s must be WidgetConfigurationActivity", TAG));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_widget_clock, container, false);
		return mView;

	}
}
