package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.avast.android.dialogs.fragment.ListDialogFragment;
import com.rehivetech.beeeon.IconResourceType;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.activity.DeviceDetailActivity;
import com.rehivetech.beeeon.gui.adapter.DeviceModuleAdapter;
import com.rehivetech.beeeon.gui.dialog.NumberPickerDialogFragment;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.DeviceFeatures;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.household.device.values.BaseValue;
import com.rehivetech.beeeon.household.device.values.EnumValue;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.task.ActorActionTask;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.TimeHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author martin on 4.8.2015.
 */
public class DeviceDetailFragment extends BaseApplicationFragment {

	private static final String TAG = DeviceDetailFragment.class.getSimpleName();

	private static final String KEY_GATE_ID = "gateId";
	private static final String KEY_DEVICE_ID = "deviceId";

	private static final int REQUEST_SET_ACTUATOR = 7894;

	private DeviceDetailActivity mActivity;
	private Device mDevice;
	private TimeHelper mTimeHelper;
	private String mGateId;
	private String mDeviceId;
	private String mModuleId;

	private TextView mDeviceName;
	private TextView mDeviceLocation;
	private TextView mDeviceLastUpdate;
	private TextView mDeviceSignal;
	private TextView mDeviceBattery;
	private TextView mDeviceRefresh;
	private ImageView mDeviceLocationIcon;
	private RecyclerView mRecyclerView;
	private DeviceModuleAdapter mModuleAdapter;

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
		mActivity  = (DeviceDetailActivity) activity;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_device_detail, container, false);
		Controller controller = Controller.getInstance(mActivity);
		Location location = controller.getLocationsModel().getLocation(mGateId, mDevice.getLocationId());

		Toolbar toolbar = (Toolbar) view.findViewById(R.id.beeeon_toolbar);
		AppCompatActivity activity = (AppCompatActivity) getActivity();
		activity.setSupportActionBar(toolbar);
		ActionBar actionBar = activity.getSupportActionBar();

		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setDisplayShowTitleEnabled(false);
		}

		mDeviceName = (TextView) view.findViewById(R.id.device_detail_device_name);
		mDeviceName.setText(mDevice.getType().getNameRes());

		mDeviceLocation = (TextView) view.findViewById(R.id.device_detail_loc_label);
		mDeviceLocation.setText(location.getName());

		mDeviceLocationIcon = (ImageView) view.findViewById(R.id.device_detail_loc_icon);
		mDeviceLocationIcon.setImageResource(location.getIconResource(IconResourceType.WHITE));

		mDeviceLastUpdate = (TextView) view.findViewById(R.id.device_detail_last_update_label);
		mDeviceLastUpdate.setText(mTimeHelper.formatLastUpdate(mDevice.getLastUpdate(), controller.getGatesModel().getGate(mGateId)));

		DeviceFeatures deviceFeatures = mDevice.getType().getFeatures();

		if (deviceFeatures.hasRssi()) {
			LinearLayout signalLayout = (LinearLayout) view.findViewById(R.id.device_detail_signal_layout);
			mDeviceSignal = (TextView) view.findViewById(R.id.device_detail_signal_value);
			mDeviceSignal.setText(String.format("%d%%", mDevice.getNetworkQuality()));

			signalLayout.setVisibility(View.VISIBLE);
		}

		if (deviceFeatures.hasBattery()) {
			LinearLayout batteryLayout = (LinearLayout) view.findViewById(R.id.device_detail_battery_layout);
			mDeviceBattery = (TextView) view.findViewById(R.id.device_detail_battery_value);
			mDeviceBattery.setText(String.format("%d%%", mDevice.getBattery()));

			batteryLayout.setVisibility(View.VISIBLE);
		}

		if (deviceFeatures.hasRefresh()) {
			LinearLayout refreshLayout = (LinearLayout) view.findViewById(R.id.device_detail_refresh_layout);
			mDeviceRefresh = (TextView) view.findViewById(R.id.device_detail_refresh_value);
			RefreshInterval refreshInterval = mDevice.getRefresh();
			if (refreshInterval != null) {
				mDeviceRefresh.setText(refreshInterval.getStringInterval(mActivity));
			}

			refreshLayout.setVisibility(View.VISIBLE);
		}

		if (deviceFeatures.hasLed()) {
			LinearLayout ledLayout = (LinearLayout) view.findViewById(R.id.device_detail_led_layout);
			ledLayout.setVisibility(View.VISIBLE);
		}

		List<String> moduleGroups = mDevice.getModulesGroups(mActivity);

		if (moduleGroups.size() == 1) {
			List<Module> modules = mDevice.getAllModules();
			mRecyclerView = (RecyclerView) view.findViewById(R.id.device_detail_modules_list);
			mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
			TextView emptyView = (TextView) view.findViewById(R.id.device_detrail_module_list_empty_view);
			mModuleAdapter = new DeviceModuleAdapter(mActivity, modules, getRecyclerViewClickListener());
			mRecyclerView.setAdapter(mModuleAdapter);

			if (mModuleAdapter.getItemCount() == 0) {
				mRecyclerView.setVisibility(View.GONE);
				emptyView.setVisibility(View.VISIBLE);
			}
		} else {
			view.findViewById(R.id.device_detail_recyclerview_layout).setVisibility(View.GONE);
			view.findViewById(R.id.device_detail_viewpager_layout).setVisibility(View.VISIBLE);
			ViewPager pager = (ViewPager) view.findViewById(R.id.device_detail_group_pager);
			final TabLayout tabLayout = (TabLayout) view.findViewById(R.id.device_detail_group_tab_layout);
			setupViewPager(pager, tabLayout, moduleGroups);
		}
		return view;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Controller controller = Controller.getInstance(mActivity);

		mGateId = getArguments().getString(KEY_GATE_ID);
		mDeviceId = getArguments().getString(KEY_DEVICE_ID);

		mDevice = controller.getDevicesModel().getDevice(mGateId, mDeviceId);

		SharedPreferences prefs = controller.getUserSettings();
		mTimeHelper = (prefs == null) ? null : new TimeHelper(prefs);

		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.activity_device_detail_menu, menu);
	}

	private void setupViewPager(ViewPager viewPager, TabLayout tabLayout, List<String> moduleGroups) {
		ModuleGroupPagerAdapter adapter = new ModuleGroupPagerAdapter(getChildFragmentManager());
		for (String group : moduleGroups) {
			DeviceDetailGroupModuleFragment fragment = DeviceDetailGroupModuleFragment.newInstance(mGateId, mDeviceId, group);
			adapter.addFragment(fragment, group);
		}
		viewPager.setAdapter(adapter);
		tabLayout.setupWithViewPager(viewPager);
	}

	static class ModuleGroupPagerAdapter extends FragmentPagerAdapter {
		private final List<Fragment> mFragments = new ArrayList<>();
		private final List<String> mFragmentTitles = new ArrayList<>();

		public ModuleGroupPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			return mFragments.get(position);
		}

		@Override
		public int getCount() {
			return mFragments.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return mFragmentTitles.get(position);
		}

		public void addFragment(Fragment fragment, String title) {
			mFragments.add(fragment);
			mFragmentTitles.add(title);
		}
	}

	DeviceModuleAdapter.ItemClickListener getRecyclerViewClickListener() {
		return new DeviceModuleAdapter.ItemClickListener() {
			@Override
			public void onItemClick(String moduleId) {
				Log.d(TAG, "onItemClick");
			}

			@Override
			public void onButtonChangeState(String moduleId) {
				Log.d(TAG, "onButtonChangeState");
				mModuleId = moduleId;
				showListDialog(moduleId);
			}

			@Override
			public void onButtonSetNewValue(String moduleId) {
				Log.d(TAG, "onButtonSetNewValue");
				mModuleId = moduleId;
				showNumberPickerDialog(moduleId);
			}

			@Override
			public void onSwitchChange(String moduleId) {
				Log.d(TAG, "onSwitchChange");
				Module module = mDevice.getModuleById(moduleId);
				doActorAction(module);
			}
		};
	}

	private void showListDialog(String moduleId) {
		EnumValue value = (EnumValue) mDevice.getModuleById(moduleId).getValue();
		List<EnumValue.Item> items = value.getEnumItems();

		List<String> namesList = new ArrayList<>();
		for (EnumValue.Item item : items) {
			namesList.add(getString(item.getStringResource()));
		}
		ListDialogFragment
				.createBuilder(mActivity, getFragmentManager())
				.setTitle(getString(R.string.number_picker_dialog_dialog_title_actuator_set_value))
				.setItems(namesList.toArray(new CharSequence[namesList.size()]))
				.setSelectedItem(value.getActive().getId())
				.setRequestCode(REQUEST_SET_ACTUATOR)
				.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE)
				.setConfirmButtonText(R.string.activity_fragment_btn_set)
				.setCancelButtonText(R.string.activity_fragment_btn_cancel)
				.setTargetFragment(DeviceDetailFragment.this, REQUEST_SET_ACTUATOR)
				.show();
		Log.d(TAG, "dialog is created");
	}

	private void showNumberPickerDialog(String moduleId) {
		Module module = mDevice.getModuleById(moduleId);
		NumberPickerDialogFragment.show(mActivity, module, DeviceDetailFragment.this);
	}

	private void doChangeStateModuleTask(final Module module) {
		ActorActionTask changeStateModuleTask = new ActorActionTask(mActivity);

		changeStateModuleTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if (success) {
					mModuleAdapter.notifyDataSetChanged();
				}
			}
		});

		// Execute and remember task so it can be stopped automatically
		mActivity.callbackTaskManager.executeTask(changeStateModuleTask, module);
	}

	private void doActorAction(final Module module) {
		if (!module.isActuator()) {
			return;
		}

		// SET NEW VALUE
		BaseValue value = module.getValue();
		if (value instanceof EnumValue) {
			((EnumValue) value).setNextValue();
		} else {
			Log.e(TAG, "We can't switch actor, which value isn't inherited from EnumValue, yet");
			return;
		}

		ActorActionTask actorActionTask = new ActorActionTask(mActivity);
		actorActionTask.setListener(new CallbackTask.ICallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				if (success) {
					mModuleAdapter.notifyDataSetChanged();
				}
			}

		});

		// Execute and remember task so it can be stopped automatically
		mActivity.callbackTaskManager.executeTask(actorActionTask, module);
	}

	public void onSetTemperatureClick(Double value, String moduleId) {
		Module module = mDevice.getModuleById(moduleId);
		if (module == null) {
			Log.e(TAG, "Can't load module for changing its value");
			return;
		}

		module.setValue(String.valueOf(value));
		doChangeStateModuleTask(module);
	}


}
