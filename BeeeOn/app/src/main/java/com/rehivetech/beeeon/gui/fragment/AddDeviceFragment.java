package com.rehivetech.beeeon.gui.fragment;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rehivetech.beeeon.R;


public class AddDeviceFragment extends BaseApplicationFragment {
	private static final String TAG = AddDeviceFragment.class.getSimpleName();

	public static AddDeviceFragment newInstance() {
		return new AddDeviceFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_device_add, container, false);

		TextView guideText = (TextView) view.findViewById(R.id.device_add_guide_text);
		guideText.setMovementMethod(LinkMovementMethod.getInstance());

		return view;
	}
}
