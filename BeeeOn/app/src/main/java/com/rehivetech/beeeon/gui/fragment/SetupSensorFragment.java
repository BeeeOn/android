package com.rehivetech.beeeon.gui.fragment;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.activity.MainActivity;
import com.rehivetech.beeeon.gui.activity.SetupSensorActivity;
import com.rehivetech.beeeon.gui.adapter.LocationArrayAdapter;
import com.rehivetech.beeeon.gui.adapter.LocationIconAdapter;
import com.rehivetech.beeeon.gui.adapter.SetupSensorListAdapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.TimeHelper;

import java.util.ArrayList;
import java.util.List;

public class SetupSensorFragment extends TrackFragment {

	public SetupSensorActivity mActivity;
	private View mView;
	private Controller mController;

	private static final String TAG = MainActivity.class.getSimpleName();
	private static final int NAME_ITEM_HEIGHT = 56;

	private Gate mGate;
	private List<Device> mNewDevices;

	private LinearLayout mLayout;


	private ListView mListOfName;
	private EditText mNewLocation;
	private TextView mOrLabel;
	private Spinner mSpinner;
	private Spinner mNewIconSpinner;
	private Button mPosButton;

	private boolean isError = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Get activity and controller
		mActivity = (SetupSensorActivity) getActivity();
		mController = Controller.getInstance(mActivity);

		mGate = mController.getActiveGate();
		if (mGate == null) {
			// CHYBA
			return;
		}
		mNewDevices = mController.getUninitializedDevicesModel().getUninitializedDevicesByGate(mGate.getId());

		// TODO: sent as parameter if we want first uninitialized module or some
		// module with particular id


		// Create the AlertDialog object and return it
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.activity_setup_sensor_activity_dialog, container, false);

		mLayout = (LinearLayout) mView.findViewById(R.id.container);

		initViews();

		return mView;
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser) {
			Log.d(TAG, "SETUP SENSOR fragment is visible");
			mActivity.setFragment(this);
		}
	}


	private void initViews() {
		// Get GUI elements
		mListOfName = (ListView) mView.findViewById(R.id.setup_sensor_name_list);
		mSpinner = (Spinner) mView.findViewById(R.id.addsensor_spinner_choose_location);
		mNewLocation = (EditText) mView.findViewById(R.id.addsensor_new_location_name);
		TextView time = (TextView) mView.findViewById(R.id.setup_sensor_info_text);

		// Create gate for setting names of new sensors
		SetupSensorListAdapter listAdapter = new SetupSensorListAdapter(mActivity, mNewDevices.get(0));
		LocationArrayAdapter dataAdapter = new LocationArrayAdapter(mActivity, R.layout.custom_spinner_item);

		// Set layout to DataAdapter for locations
		dataAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);

		// Set listener for hide or unhide layout for add new location
		mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (position == mSpinner.getCount() - 1) {
					// show new location
					if (!hideInputForNewLocation(false) && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
						shringSpinner(true);
					}
				} else {
					// hide input for new location
					if (hideInputForNewLocation(true) && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
						shringSpinner(false);
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				hideInputForNewLocation(true);
			}
		});

		// UserSettings can be null when user is not logged in!
		SharedPreferences prefs = mController.getUserSettings();

		TimeHelper timeHelper = (prefs == null) ? null : new TimeHelper(prefs);

		// Set involved time of mDevice
		if (timeHelper != null) {
			Device device = mNewDevices.get(0);
			Gate gate = mController.getGatesModel().getGate(device.getGateId());
			time.setText(String.format("%s %s", time.getText(), timeHelper.formatLastUpdate(device.getInvolveTime(), gate)));
		}

		// Set involved time of mDevice

		// Set gate to ListView and to Spinner
		mListOfName.setAdapter(listAdapter);
		mSpinner.setAdapter(dataAdapter);
		// Set listview height, for all 
		float scale = mActivity.getResources().getDisplayMetrics().density;
		mListOfName.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, (int) (scale * NAME_ITEM_HEIGHT * mNewDevices.get(0).getModules().size())));
	}

	/**
	 * Method take needed inputs and switch visibility
	 *
	 * @param hide items is hidden if true, visible otherwise
	 * @return true if is item hidden
	 */
	private boolean hideInputForNewLocation(boolean hide) {
		if (mNewLocation == null)
			mNewLocation = (EditText) mView.findViewById(R.id.addsensor_new_location_name);
		if (mOrLabel == null)
			mOrLabel = (TextView) mView.findViewById(R.id.addsensor_or);
		if (mNewIconSpinner == null) {
			mNewIconSpinner = (Spinner) mView.findViewById(R.id.addsensor_spinner_choose_new_location_icon);

			// Prepare list of icons
			List<Integer> iconsList = new ArrayList<Integer>();
			for (Location.LocationIcon icon : Location.LocationIcon.values()) {
				iconsList.add(icon.getIconResource());
			}

			// first call need to add gate
			LocationIconAdapter iconAdapter = new LocationIconAdapter(mActivity, R.layout.custom_spinner_icon_item);
			iconAdapter.setDropDownViewResource(R.layout.custom_spinner_icon_dropdown_item);
			mNewIconSpinner.setAdapter(iconAdapter);
		}

		int visibility = (hide ? View.GONE : View.VISIBLE);
		mNewLocation.setVisibility(visibility);
		mOrLabel.setVisibility(visibility);
		mNewIconSpinner.setVisibility(visibility);

		return hide;
	}

	private boolean shringSpinner(boolean shrink) {
		LayoutParams params = (LayoutParams) mSpinner.getLayoutParams();
		if (shrink)
			params.width = 180;
		else
			params.width = LayoutParams.MATCH_PARENT;
		mSpinner.setLayoutParams(params);
		return false;
	}


	public Spinner getSpinner() {
		return mSpinner;
	}

	public ListView getListOfName() {
		return mListOfName;
	}

	public TextView getNewLocation() {
		return mNewLocation;
	}

	public Spinner getNewIconSpinner() {
		return mNewIconSpinner;
	}

}
