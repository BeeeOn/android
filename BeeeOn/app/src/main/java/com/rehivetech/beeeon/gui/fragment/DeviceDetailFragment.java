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
import android.widget.TextView;

import com.rehivetech.beeeon.IconResourceType;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.device.Device;
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

		TextView deviceName = (TextView) view.findViewById(R.id.device_detail_device_name);
		deviceName.setText(device.getType().getNameRes());

		TextView deviceLocation = (TextView) view.findViewById(R.id.device_detail_loc_label);
		deviceLocation.setText(location.getName());

		ImageView deviceLocationIcon = (ImageView) view.findViewById(R.id.device_detail_loc_icon);
		deviceLocationIcon.setImageResource(location.getIconResource(IconResourceType.WHITE));

		TextView deviceLastUpdate = (TextView) view.findViewById(R.id.device_detail_last_update_label);
		deviceLastUpdate.setText(mTimeHelper.formatLastUpdate(device.getLastUpdate(), controller.getGatesModel().getGate(mGateId)));

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
