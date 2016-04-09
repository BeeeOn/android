package com.rehivetech.beeeon.gui.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
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
import android.util.Log;
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
import com.avast.android.dialogs.iface.IListDialogListener;
import com.rehivetech.beeeon.IconResourceType;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.activity.ModuleGraphActivity;
import com.rehivetech.beeeon.gui.adapter.DeviceModuleAdapter;
import com.rehivetech.beeeon.gui.dialog.NumberPickerDialogFragment;
import com.rehivetech.beeeon.gui.view.DeviceFeatureView;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.ModuleType;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.household.device.Status;
import com.rehivetech.beeeon.household.device.values.BaseValue;
import com.rehivetech.beeeon.household.device.values.EnumValue;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.ICallbackTaskFactory;
import com.rehivetech.beeeon.threading.task.ActorActionTask;
import com.rehivetech.beeeon.util.ActualizationTime;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnavailableModules;
import com.rehivetech.beeeon.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author martin
 * @since 04.08.2015
 */
public class DeviceDetailFragment extends BaseApplicationFragment implements DeviceModuleAdapter.ItemClickListener,
		AppBarLayout.OnOffsetChangedListener, IListDialogListener, NumberPickerDialogFragment.SetNewValueListener {

	private static final String TAG = DeviceDetailFragment.class.getSimpleName();

	private static final String KEY_GATE_ID = "gateId";
	private static final String KEY_DEVICE_ID = "deviceId";
	private static final String KEY_VIEW_PAGER_SELECTED_ITEM = "selected_item";
	private static final String DEVICE_DETAIL_FRAGMENT_AUTO_RELOAD_ID = "deviceDetailFragmentAutoReload";


	private static final int REQUEST_SET_ACTUATOR = 7894;

	private UpdateDevice mDeviceCallback;
	private Device mDevice;
	private TimeHelper mTimeHelper;
	private String mGateId;
	private String mDeviceId;
	private String mModuleId;
	private boolean mHideUnavailableModules;

	private CoordinatorLayout mRootLayout;
	private ImageView mIcon;
	private ImageView mStatusIcon;
	private TextView mDeviceName;

	private DeviceFeatureView mDeviceLocation;
	private DeviceFeatureView mDeviceLastUpdate;
	private DeviceFeatureView mDeviceSignal;
	private DeviceFeatureView mDeviceBattery;
	private DeviceFeatureView mDeviceRefresh;

	private TextView mEmptyTextView;
	private RecyclerView mRecyclerView;
	private DeviceModuleAdapter mModuleAdapter;

	private ViewPager mViewPager;
	private TabLayout mTabLayout;

	private final ICallbackTaskFactory mICallbackTaskFactory = new ICallbackTaskFactory() {
		@Override
		public CallbackTask createTask() {
			return mDeviceCallback != null ? mDeviceCallback.createReloadDevicesTask(true) : null;
		}

		@Override
		public Object createParam() {
			return mGateId;
		}
	};

	public static DeviceDetailFragment newInstance(String gateId, String deviceId) {
		Bundle args = new Bundle();
		args.putString(KEY_GATE_ID, gateId);
		args.putString(KEY_DEVICE_ID, deviceId);

		DeviceDetailFragment fragment = new DeviceDetailFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

		try {
			mDeviceCallback = (UpdateDevice) context;
		} catch (ClassCastException e) {
			throw new ClassCastException(context.toString() + " must implement UpdateDevice");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Controller controller = Controller.getInstance(mActivity);

		mGateId = getArguments().getString(KEY_GATE_ID);
		mDeviceId = getArguments().getString(KEY_DEVICE_ID);

		mDevice = controller.getDevicesModel().getDevice(mGateId, mDeviceId);

		SharedPreferences prefs = controller.getUserSettings();
		mTimeHelper = Utils.getTimeHelper(prefs);

		mHideUnavailableModules = UnavailableModules.fromSettings(prefs);

		setHasOptionsMenu(true);
		mModuleId = "-1";
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_device_detail, container, false);

		// FIXME: Why this doesn't work when it's in DeviceDetailActivity?
		Toolbar toolbar = (Toolbar) view.findViewById(R.id.beeeon_toolbar);

		CollapsingToolbarLayout.LayoutParams layoutParams = (CollapsingToolbarLayout.LayoutParams) toolbar.getLayoutParams();
		layoutParams.setCollapseMode(CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PIN);
		toolbar.setLayoutParams(layoutParams);

		AppCompatActivity activity = (AppCompatActivity) getActivity();
		activity.setSupportActionBar(toolbar);
		ActionBar actionBar = activity.getSupportActionBar();

		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setDisplayShowTitleEnabled(false);
		}

		mRootLayout = (CoordinatorLayout) view.findViewById(R.id.device_detail_root_layout);

		AppBarLayout appBarLayout = (AppBarLayout) view.findViewById(R.id.device_detail_appbar);
		appBarLayout.addOnOffsetChangedListener(this);

		mIcon = (ImageView) view.findViewById(R.id.device_detail_icon);
		setParallaxMultiplier(mIcon, 0.9f);
		mStatusIcon = (ImageView) view.findViewById(R.id.device_detail_status_icon);
		setParallaxMultiplier(mStatusIcon, 0.9f);
		mDeviceName = (TextView) view.findViewById(R.id.device_detail_device_name);
		setParallaxMultiplier(mDeviceName, 0.9f);

		if (mDevice == null)
			return view;

		LinearLayout featuresLayout = (LinearLayout) view.findViewById(R.id.device_detail_features_layout);

		final Module rssi = mDevice.getFirstModuleByType(ModuleType.TYPE_RSSI);
		if (rssi != null) {
			mDeviceSignal = new DeviceFeatureView(mActivity);
			mDeviceSignal.setCaption(getString(R.string.module_detail_label_signal));
			mDeviceSignal.setIcon(R.drawable.ic_signal_wifi_4_bar_white_24dp);
			mDeviceSignal.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onItemClick(rssi.getId());
				}
			});
			featuresLayout.addView(mDeviceSignal);
		}

		final Module battery = mDevice.getFirstModuleByType(ModuleType.TYPE_BATTERY);
		if (battery != null) {
			mDeviceBattery = new DeviceFeatureView(mActivity);
			mDeviceBattery.setCaption(getString(R.string.devices__type_battery));
			mDeviceBattery.setIcon(R.drawable.ic_battery);
			mDeviceBattery.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onItemClick(battery.getId());
				}
			});
			featuresLayout.addView(mDeviceBattery);
		}

		final Module refresh = mDevice.getFirstModuleByType(ModuleType.TYPE_REFRESH);
		if (mDevice.getRefresh() != null) {
			mDeviceRefresh = new DeviceFeatureView(mActivity);
			mDeviceRefresh.setCaption(getString(R.string.devices__type_refresh));
			mDeviceRefresh.setIcon(R.drawable.ic_refresh);
			mDeviceRefresh.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onItemClick(refresh.getId());
				}
			});
			featuresLayout.addView(mDeviceRefresh);
		}

//		TODO device LED

		mDeviceLastUpdate = new DeviceFeatureView(mActivity);
		mDeviceLastUpdate.setCaption(getString(R.string.module_detail_label_last_update));
		mDeviceLastUpdate.setIcon(R.drawable.ic_clock);
		featuresLayout.addView(mDeviceLastUpdate);

		mDeviceLocation = new DeviceFeatureView(mActivity);
		mDeviceLocation.setCaption(getString(R.string.module_edit_label_detail_location));
		featuresLayout.addView(mDeviceLocation);


		List<String> moduleGroups = mDevice.getModulesGroups(mActivity, mHideUnavailableModules);

		mRecyclerView = (RecyclerView) view.findViewById(R.id.device_detail_modules_list);
		mViewPager = (ViewPager) view.findViewById(R.id.device_detail_group_pager);

		if (moduleGroups.size() <= 1) {
			mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
			mEmptyTextView = (TextView) view.findViewById(R.id.device_detail_module_list_empty_view);
			mModuleAdapter = new DeviceModuleAdapter(mActivity, this);
			mRecyclerView.setAdapter(mModuleAdapter);
			mRootLayout.removeView(mViewPager);
			mViewPager = null;
		} else {
			mTabLayout = (TabLayout) view.findViewById(R.id.device_detail_group_tab_layout);
			mTabLayout.setVisibility(View.VISIBLE);
			mRootLayout.removeView(mRecyclerView);
			setupViewPager(mViewPager, mTabLayout, mDevice.getModulesGroups(mActivity, mHideUnavailableModules));
		}

		return view;
	}

	private void setAutoReloadDataTimer() {
		SharedPreferences prefs = Controller.getInstance(getActivity()).getUserSettings();
		ActualizationTime.Item item = (ActualizationTime.Item) new ActualizationTime().fromSettings(prefs);
		int period = item.getSeconds();
		if (period > 0)    // zero means do not update
			mActivity.callbackTaskManager.executeTaskEvery(mICallbackTaskFactory, DEVICE_DETAIL_FRAGMENT_AUTO_RELOAD_ID, period);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (savedInstanceState != null && mViewPager != null) {
			mViewPager.setCurrentItem(savedInstanceState.getInt(KEY_VIEW_PAGER_SELECTED_ITEM));
			Log.d(TAG, "restore instance");
		}
		setAutoReloadDataTimer();
		updateData();
	}

	@Override
	public void onResume() {
		super.onResume();
		doReloadDevicesTask(mGateId, false);
		mActivity.setupRefreshIcon(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				doReloadDevicesTask(mGateId, true);
			}
		});
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mViewPager != null) {
			outState.putInt(KEY_VIEW_PAGER_SELECTED_ITEM, mViewPager.getCurrentItem());
		}
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

	@SuppressLint("DefaultLocale")
	public void updateData() {
		Controller controller = Controller.getInstance(mActivity);

		mDevice = controller.getDevicesModel().getDevice(mGateId, mDeviceId);
		mDevice = mDeviceCallback.getDevice();

		if (mDevice == null)
			return;

		mDeviceName.setText(mDevice.getName(mActivity));

		ActionBar actionBar = mActivity.getSupportActionBar();
		if (actionBar != null) {
			actionBar.setTitle(mDevice.getName(mActivity));
		}

		Location location = controller.getLocationsModel().getLocation(mGateId, mDevice.getLocationId());
		List<String> moduleGroups = mDevice.getModulesGroups(mActivity, mHideUnavailableModules);

		if (location != null && mDeviceLocation != null) {
			mDeviceLocation.setValue(location.getName());
			mDeviceLocation.setIcon(location.getIconResource(IconResourceType.WHITE));
		}

		if (mDeviceLastUpdate != null) {
			mDeviceLastUpdate.setValue(mTimeHelper.formatLastUpdate(mDevice.getLastUpdate(), controller.getGatesModel().getGate(mGateId)));
		}

		mIcon.setImageResource(R.drawable.ic_status_online);
		// available/unavailable icon
		boolean statusOk = mDevice.getStatus().equals(Status.AVAILABLE);
		mStatusIcon.setVisibility(statusOk ? View.GONE : View.VISIBLE);

		// signal
		Integer rssi = mDevice.getRssi();
		if (rssi != null && mDeviceSignal != null) {
			mDeviceSignal.setValue(String.format("%d%%", rssi));
			mDeviceSignal.setIcon(rssi == 0 ? R.drawable.ic_signal_wifi_off_white_24dp : R.drawable.ic_signal_wifi_4_bar_white_24dp);
		}

		// battery
		if (mDevice.getBattery() != null && mDeviceBattery != null) {
			mDeviceBattery.setValue(String.format("%d%%", mDevice.getBattery()));
		}

		// refresh
		if (mDevice.getRefresh() != null && mDeviceRefresh != null) {
			RefreshInterval refreshInterval = mDevice.getRefresh();
			mDeviceRefresh.setValue(refreshInterval.getStringInterval(mActivity));
		}
		// TODO device LED initialize

		if (mModuleAdapter != null) {
			mModuleAdapter.swapModules(mDevice.getVisibleModules(mHideUnavailableModules));

			if (mModuleAdapter.getItemCount() == 0) {
				mRecyclerView.setVisibility(View.GONE);
				mEmptyTextView.setVisibility(View.VISIBLE);
			}
		}

		if (mViewPager != null) {
			for (int i = 0; i < moduleGroups.size(); i++) {
				ModuleGroupPagerAdapter adapter = (ModuleGroupPagerAdapter) mViewPager.getAdapter();
				DeviceDetailGroupModuleFragment fragment = (DeviceDetailGroupModuleFragment) adapter.getActiveFragment(mViewPager, i);
				if (fragment != null) {
					Log.d(TAG, "updating viewpager fragment " + fragment.getTag());
					fragment.updateData();
				}
			}

		}

	}

	@Override
	public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
		int maxScroll = (int) (appBarLayout.getTotalScrollRange() * 0.5f);
		ActionBar actionBar = mActivity.getSupportActionBar();

		if (actionBar != null) {
			actionBar.setDisplayShowTitleEnabled(verticalOffset <= -maxScroll);
		}
	}

	@Override
	public void onItemClick(String moduleId) {
		Intent intent = ModuleGraphActivity.getActivityIntent(mActivity, mGateId, mDeviceId, moduleId);
		startActivity(intent);
	}

	@Override
	public void onButtonChangeState(String moduleId) {
		mModuleId = moduleId;
		showListDialog(moduleId);
	}

	@Override
	public void onButtonSetNewValue(String moduleId) {
		NumberPickerDialogFragment.showNumberPickerDialog(mActivity, mDevice.getModuleById(moduleId), this);
	}

	@Override
	public void onListItemSelected(CharSequence charSequence, int number, int requestCode) {
		if (requestCode == REQUEST_SET_ACTUATOR) {
			Module module = mDevice.getModuleById(mModuleId);
			if (module == null) {
				Log.e(TAG, "Can't load module for changing its value");
				return;
			}

			module.setValue(String.valueOf(number));
			doChangeStateModuleTask(module);
		}
	}

	@Override
	public void onSwitchChange(String moduleId) {
		Module module = mDevice.getModuleById(moduleId);
		doActorAction(module);
	}

	private void showListDialog(String moduleId) {
		Module module = mDevice.getModuleById(moduleId);
		EnumValue value = (EnumValue) module.getValue();
		List<EnumValue.Item> items = value.getEnumItems();

		List<String> namesList = new ArrayList<>();
		for (EnumValue.Item item : items) {
			namesList.add(getString(item.getStringResource()));
		}
		ListDialogFragment
				.createBuilder(mActivity, getFragmentManager())
				.setTitle(module.getName(mActivity))
				.setItems(namesList.toArray(new CharSequence[namesList.size()]))
				.setSelectedItem(value.getActive().getId())
				.setRequestCode(REQUEST_SET_ACTUATOR)
				.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE)
				.setConfirmButtonText(R.string.activity_fragment_btn_set)
				.setCancelButtonText(R.string.activity_fragment_btn_cancel)
				.setTargetFragment(DeviceDetailFragment.this, REQUEST_SET_ACTUATOR)
				.show();
	}

	protected void doReloadDevicesTask(final String gateId, final boolean forceReload) {
		// Execute and remember task so it can be stopped automatically
		if (mDeviceCallback != null)
			mActivity.callbackTaskManager.executeTask(mDeviceCallback.createReloadDevicesTask(forceReload), gateId);
	}


	private void doChangeStateModuleTask(final Module module) {
		ActorActionTask changeStateModuleTask = new ActorActionTask(mActivity);

		changeStateModuleTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if (success) {
					mDevice = Controller.getInstance(mActivity).getDevicesModel().getDevice(mGateId, mDeviceId);
					updateData();
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
					updateData();
				}
			}

		});

		// Execute and remember task so it can be stopped automatically
		mActivity.callbackTaskManager.executeTask(actorActionTask, module);
	}

	private void setParallaxMultiplier(View view, float multiplier) {
		CollapsingToolbarLayout.LayoutParams layoutParams = (CollapsingToolbarLayout.LayoutParams) view.getLayoutParams();
		layoutParams.setParallaxMultiplier(multiplier);
		view.setLayoutParams(layoutParams);
	}

	@Override
	public void onSetNewValue(String moduleId, String actualValue) {
		Module module = mDevice.getModuleById(moduleId);
		if (module == null) {
			Log.e(TAG, "Can't load module for changing its value");
			return;
		}

		module.setValue(actualValue);
		doChangeStateModuleTask(module);
	}


	public interface UpdateDevice {
		CallbackTask createReloadDevicesTask(boolean forceReload);

		Device getDevice();
	}

	static class ModuleGroupPagerAdapter extends FragmentPagerAdapter {
		private final List<Fragment> mFragments = new ArrayList<>();
		private final List<String> mFragmentTitles = new ArrayList<>();
		private final FragmentManager mFragmentManager;

		public ModuleGroupPagerAdapter(FragmentManager fm) {
			super(fm);
			mFragmentManager = fm;
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

		public Fragment getActiveFragment(ViewPager container, int position) {
			String name = makeFragmentName(container.getId(), position);
			return mFragmentManager.findFragmentByTag(name);
		}

		private static String makeFragmentName(int viewId, int index) {
			return "android:switcher:" + viewId + ":" + index;
		}

		public void addFragment(Fragment fragment, String title) {
			mFragments.add(fragment);
			mFragmentTitles.add(title);
		}
	}
}
