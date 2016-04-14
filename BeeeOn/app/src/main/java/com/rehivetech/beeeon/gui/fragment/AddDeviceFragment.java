package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gcm.analytics.GoogleAnalyticsManager;
import com.rehivetech.beeeon.gui.activity.AddDeviceActivity;
import com.rehivetech.beeeon.gui.activity.ScanQRActivity;
import com.rehivetech.beeeon.gui.activity.WebViewActivity;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class AddDeviceFragment extends BaseApplicationFragment {
	private static final String TAG = AddDeviceFragment.class.getSimpleName();

	public static final String KEY_GATE_ID = "gate_id";

	String mGateId;

	@Bind(R.id.device_add_guide_text)
	TextView mDeviceAddGuideText;

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
		ButterKnife.bind(this, view);

		mDeviceAddGuideText.setMovementMethod(LinkMovementMethod.getInstance());
		return view;
	}

	@OnClick(R.id.device_add_search_button)
	public void onSearchClick() {
		startActivityForDeviceSearch();
	}

	@OnClick(R.id.device_add_supported_devices_button)
	public void onSupportedDevicesClick() {
		Intent intent = new Intent(mActivity, WebViewActivity.class);
		intent.putExtra(WebViewActivity.EXTRA_URL_ADDRESS, mActivity.getString(R.string.device_supported_list_url));
		startActivity(intent);
	}

	@Override
	public void onResume() {
		super.onResume();
		GoogleAnalyticsManager.getInstance().logScreen(GoogleAnalyticsManager.ADD_DEVICE_SCREEN);
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

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		ButterKnife.unbind(this);
	}
}
