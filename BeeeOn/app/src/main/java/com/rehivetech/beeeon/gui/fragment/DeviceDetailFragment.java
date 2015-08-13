package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rehivetech.beeeon.IconResourceType;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.DeviceFeatures;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.util.TimeHelper;

/**
 * @author martin on 4.8.2015.
 */
public class DeviceDetailFragment extends BaseApplicationFragment {

	private static final String TAG = DeviceDetailFragment.class.getSimpleName();

	private static final String KEY_GATE_ID = "gateId";
	private static final String KEY_DEVICE_ID = "deviceId";

	private Context mContext;
	private TimeHelper mTimeHelper;
	private String mGateId;
	private String mDeviceId;

	private TextView mDeviceName;
	private TextView mDeviceLocation;
	private TextView mDeviceLastUpdate;
	private TextView mDeviceSignal;
	private TextView mDeviceBattery;
	private TextView mDeviceRefresh;
	private ImageView mDeviceLocationIcon;

	public static DeviceDetailFragment newInstance(String gateId, String deviceId) {

		Bundle args = new Bundle();
		args.putString(KEY_GATE_ID, gateId);
		args.putString(KEY_DEVICE_ID, deviceId);

		DeviceDetailFragment fragment = new DeviceDetailFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_device_detail, container, false);
		Controller controller = Controller.getInstance(mContext);
		Device device = controller.getDevicesModel().getDevice(mGateId, mDeviceId);
		Location location = controller.getLocationsModel().getLocation(mGateId, device.getLocationId());

		Toolbar toolbar = (Toolbar) view.findViewById(R.id.beeeon_toolbar);
		AppCompatActivity activity = (AppCompatActivity) getActivity();
		activity.setSupportActionBar(toolbar);
		ActionBar actionBar = activity.getSupportActionBar();

		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setDisplayShowTitleEnabled(false);
		}

		mDeviceName = (TextView) view.findViewById(R.id.device_detail_device_name);
		mDeviceName.setText(device.getType().getNameRes());

		mDeviceLocation = (TextView) view.findViewById(R.id.device_detail_loc_label);
		mDeviceLocation.setText(location.getName());

		mDeviceLocationIcon = (ImageView) view.findViewById(R.id.device_detail_loc_icon);
		mDeviceLocationIcon.setImageResource(location.getIconResource(IconResourceType.WHITE));

		mDeviceLastUpdate = (TextView) view.findViewById(R.id.device_detail_last_update_label);
		mDeviceLastUpdate.setText(mTimeHelper.formatLastUpdate(device.getLastUpdate(), controller.getGatesModel().getGate(mGateId)));

		DeviceFeatures deviceFeatures = device.getType().getFeatures();

		if (deviceFeatures.hasRssi()) {
			LinearLayout signalLayout = (LinearLayout) view.findViewById(R.id.device_detail_signal_layout);
			mDeviceSignal = (TextView) view.findViewById(R.id.device_detail_signal_value);
			mDeviceSignal.setText(String.format("%d%%", device.getNetworkQuality()));

			signalLayout.setVisibility(LinearLayout.VISIBLE);
		}

		if (deviceFeatures.hasBattery()) {
			LinearLayout batteryLayout = (LinearLayout) view.findViewById(R.id.device_detail_battery_layout);
			mDeviceBattery = (TextView) view.findViewById(R.id.device_detail_battery_value);
			mDeviceBattery.setText(String.format("%d%%", device.getBattery()));

			batteryLayout.setVisibility(LinearLayout.VISIBLE);
		}

		if (deviceFeatures.hasRefresh()) {
			LinearLayout refreshLayout = (LinearLayout) view.findViewById(R.id.device_detail_refresh_layout);
			mDeviceRefresh = (TextView) view.findViewById(R.id.device_detail_refresh_value);
			RefreshInterval refreshInterval = device.getRefresh();
			if (refreshInterval != null) {
				mDeviceRefresh.setText(refreshInterval.getStringInterval(mContext));
			}

			refreshLayout.setVisibility(LinearLayout.VISIBLE);
		}

		if (deviceFeatures.hasLed()) {
			LinearLayout ledLayout = (LinearLayout) view.findViewById(R.id.device_detail_led_layout);
			ledLayout.setVisibility(LinearLayout.VISIBLE);
		}

		return view;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mGateId = getArguments().getString(KEY_GATE_ID);
		mDeviceId = getArguments().getString(KEY_DEVICE_ID);

		SharedPreferences prefs = Controller.getInstance(mContext).getUserSettings();
		mTimeHelper = (prefs == null) ? null : new TimeHelper(prefs);

		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.activity_device_detail_menu, menu);
	}
}
