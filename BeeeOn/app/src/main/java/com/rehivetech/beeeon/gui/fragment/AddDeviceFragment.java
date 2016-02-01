package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.activity.AddDeviceActivity;


public class AddDeviceFragment extends BaseApplicationFragment {
	private static final String TAG = AddDeviceFragment.class.getSimpleName();

	public static final String KEY_GATE_ID = "gate_id";

	String mGateId;

	public static AddDeviceFragment newInstance(String gateId) {
		Bundle args = new Bundle();
		args.putString(KEY_GATE_ID, gateId);
		AddDeviceFragment fragment = new AddDeviceFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();
		mGateId = args.getString(KEY_GATE_ID);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_device_add, container, false);

		TextView guideText = (TextView) view.findViewById(R.id.device_add_guide_text);
		guideText.setMovementMethod(LinkMovementMethod.getInstance());

		Button button = (Button) view.findViewById(R.id.device_add_search_button);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForDeviceSearch();
			}
		});
		return view;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			mActivity.setResult(Activity.RESULT_OK);
			mActivity.finish();
		}
	}

	private void startActivityForDeviceSearch() {
		Intent intent = AddDeviceActivity.prepareAddDeviceActivityIntent(mActivity, mGateId, AddDeviceActivity.ACTION_SEARCH, null);
		startActivityForResult(intent, 0);
	}
}
