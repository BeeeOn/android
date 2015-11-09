package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
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
import com.rehivetech.beeeon.IconResourceType;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.activity.DeviceDetailActivity;
import com.rehivetech.beeeon.gui.activity.ModuleGraphActivity;
import com.rehivetech.beeeon.gui.adapter.DeviceModuleAdapter;
import com.rehivetech.beeeon.gui.dialog.NumberPickerDialogFragment;
import com.rehivetech.beeeon.gui.view.DeviceFeatureView;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.household.device.values.BaseValue;
import com.rehivetech.beeeon.household.device.values.EnumValue;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.ICallbackTaskFactory;
import com.rehivetech.beeeon.threading.task.ActorActionTask;
import com.rehivetech.beeeon.threading.task.ReloadGateDataTask;
import com.rehivetech.beeeon.util.ActualizationTime;
import com.rehivetech.beeeon.util.TimeHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author martin on 4.8.2015.
 */
public class DeviceDetailFragment extends BaseApplicationFragment implements DeviceModuleAdapter.ItemClickListener,
		AppBarLayout.OnOffsetChangedListener {

	private static final String TAG = DeviceDetailFragment.class.getSimpleName();

	private static final String KEY_GATE_ID = "gateId";
	private static final String KEY_DEVICE_ID = "deviceId";
	private static final String KEY_VIEW_PAGER_SELECTED_ITEM = "selected_item";
	private static final String DEVICE_DETAIL_FRAGMENT_AUTO_RELOAD_ID = "deviceDetailFragmentAutoReload";


	private static final int REQUEST_SET_ACTUATOR = 7894;

	private DeviceDetailActivity mActivity;
	private Device mDevice;
	private TimeHelper mTimeHelper;
	private String mGateId;
	private String mDeviceId;

	private ImageView mIcon;
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
			return createReloadDevicesTask(true);
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

	private CallbackTask createReloadDevicesTask(boolean forceReload) {
		if (getActivity() == null)
			return null;

		ReloadGateDataTask reloadDevicesTask = new ReloadGateDataTask(getActivity(), forceReload, ReloadGateDataTask.ReloadWhat.DEVICES);

		final int tabPos = (mViewPager != null ? mViewPager.getCurrentItem() : 0);

		reloadDevicesTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				Device device = Controller.getInstance(mActivity).getDevicesModel().getDevice(mGateId, mDeviceId);
				if (device == null) {
					Log.e(TAG, String.format("Device #%s does not exists", mDeviceId));
					mActivity.finish();
				}

				if (success) {
					updateData();
					if (mViewPager != null) {
						mViewPager.setCurrentItem(tabPos);
					}
				}
			}
		});
		return reloadDevicesTask;
	}

	@Override
	public void onAttach(Activity activity) {
		Log.d(TAG, "onAttach");
		super.onAttach(activity);
		mActivity = (DeviceDetailActivity) activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		Controller controller = Controller.getInstance(mActivity);

		mGateId = getArguments().getString(KEY_GATE_ID);
		mDeviceId = getArguments().getString(KEY_DEVICE_ID);

		mDevice = controller.getDevicesModel().getDevice(mGateId, mDeviceId);

		SharedPreferences prefs = controller.getUserSettings();
		mTimeHelper = (prefs == null) ? null : new TimeHelper(prefs);

		setHasOptionsMenu(true);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		View view = inflater.inflate(R.layout.fragment_device_detail, container, false);

		// FIXME: Why this doesn't work when it's in DeviceDetailActivity?
		Toolbar toolbar = (Toolbar) view.findViewById(R.id.beeeon_toolbar);
		AppCompatActivity activity = (AppCompatActivity) getActivity();
		activity.setSupportActionBar(toolbar);
		ActionBar actionBar = activity.getSupportActionBar();

		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setDisplayShowTitleEnabled(false);
		}

		AppBarLayout appBarLayout = (AppBarLayout) view.findViewById(R.id.device_detail_appbar);
		appBarLayout.addOnOffsetChangedListener(this);

		mIcon = (ImageView) view.findViewById(R.id.device_detail_icon);
		setParallaxMultiplier(mIcon, 0.9f);
		mDeviceName = (TextView) view.findViewById(R.id.device_detail_device_name);
		setParallaxMultiplier(mDeviceName, 0.9f);

		if (mDevice == null)
			return view;

		LinearLayout featuresLayout = (LinearLayout) view.findViewById(R.id.device_detail_features_layout);

		if (mDevice.getRssi() != null) {
			mDeviceSignal = new DeviceFeatureView(mActivity);
			mDeviceSignal.setCaption(getString(R.string.module_detail_label_signal));
			mDeviceSignal.setIcon(R.drawable.ic_signal);
			featuresLayout.addView(mDeviceSignal);
		}

		if (mDevice.getBattery() != null) {
			mDeviceBattery = new DeviceFeatureView(mActivity);
			mDeviceBattery.setCaption(getString(R.string.devices__type_battery));
			mDeviceBattery.setIcon(R.drawable.ic_battery);
			featuresLayout.addView(mDeviceBattery);
		}

		if (mDevice.getRefresh() != null) {
			mDeviceRefresh = new DeviceFeatureView(mActivity);
			mDeviceRefresh.setCaption(getString(R.string.devices__type_refresh));
			mDeviceRefresh.setIcon(R.drawable.ic_refresh);
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


		List<String> moduleGroups = mDevice.getModulesGroups(mActivity);

		if (moduleGroups.size() == 1) {
			List<Module> modules = mDevice.getVisibleModules();
			mRecyclerView = (RecyclerView) view.findViewById(R.id.device_detail_modules_list);
			mRecyclerView.setVisibility(View.VISIBLE);
			mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
			mEmptyTextView = (TextView) view.findViewById(R.id.device_detrail_module_list_empty_view);
			mModuleAdapter = new DeviceModuleAdapter(mActivity, modules, this);
			mRecyclerView.setAdapter(mModuleAdapter);

		} else {
			mViewPager = (ViewPager) view.findViewById(R.id.device_detail_group_pager);
			mTabLayout = (TabLayout) view.findViewById(R.id.device_detail_group_tab_layout);
			mTabLayout.setVisibility(View.VISIBLE);
			mViewPager.setVisibility(View.VISIBLE);
			setupViewPager(mViewPager, mTabLayout, mDevice.getModulesGroups(mActivity));
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
		Log.d(TAG, "onActivityCreated");
		super.onActivityCreated(savedInstanceState);

		if (savedInstanceState != null && mViewPager != null) {
			mViewPager.setCurrentItem(savedInstanceState.getInt(KEY_VIEW_PAGER_SELECTED_ITEM));
			Log.d(TAG, "restore instance");
		}
		setAutoReloadDataTimer();
	}

	@Override
	public void onResume() {
		Log.d(TAG, "onResume");
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
		Log.d(TAG, "onSaveInstanceState");
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

	public void updateData() {
		Controller controller = Controller.getInstance(mActivity);

		mDevice = controller.getDevicesModel().getDevice(mGateId, mDeviceId);
		if (mDevice == null)
			return;

		mDeviceName.setText(mDevice.getName(mActivity));

		ActionBar actionBar = mActivity.getSupportActionBar();
		if (actionBar != null) {
			actionBar.setTitle(mDevice.getName(mActivity));
		}

		Location location = controller.getLocationsModel().getLocation(mGateId, mDevice.getLocationId());
		List<String> moduleGroups = mDevice.getModulesGroups(mActivity);

		if (location != null && mDeviceLocation != null) {
			mDeviceLocation.setValue(location.getName());
			mDeviceLocation.setIcon(location.getIconResource(IconResourceType.WHITE));
		}

		if (mDeviceLastUpdate != null) {
			mDeviceLastUpdate.setValue(mTimeHelper.formatLastUpdate(mDevice.getLastUpdate(), controller.getGatesModel().getGate(mGateId)));
		}

		// signal
		if (mDevice.getRssi() != null && mDeviceSignal != null) {
			mDeviceSignal.setValue(String.format("%d%%", mDevice.getRssi()));
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
			mModuleAdapter.swapModules(mDevice.getVisibleModules());

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
					fragment.updateData(mDevice);
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
		Log.d(TAG, "onItemClick:" + moduleId);
		Bundle args = new Bundle();
		args.putString(ModuleGraphActivity.EXTRA_GATE_ID, mGateId);
		args.putString(ModuleGraphActivity.EXTRA_DEVICE_ID, mDeviceId);
		args.putString(ModuleGraphActivity.EXTRA_MODULE_ID, moduleId);
		Intent intent = new Intent(mActivity, ModuleGraphActivity.class);
		intent.putExtras(args);
		startActivity(intent);
	}

	@Override
	public void onButtonChangeState(String moduleId) {
		Log.d(TAG, "onButtonChangeState");
		showListDialog(moduleId);
	}

	@Override
	public void onButtonSetNewValue(String moduleId) {
		Log.d(TAG, "onButtonSetNewValue");

		NumberPickerDialogFragment.showNumberPickerDialog(mActivity, mDevice.getModuleById(moduleId), this);
	}

	@Override
	public void onSwitchChange(String moduleId) {
		Log.d(TAG, "onSwitchChange");
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
		Log.d(TAG, "dialog is created");
	}

	protected void doReloadDevicesTask(final String gateId, final boolean forceReload) {
		// Execute and remember task so it can be stopped automatically
		mActivity.callbackTaskManager.executeTask(createReloadDevicesTask(forceReload), gateId);
	}

	private void doChangeStateModuleTask(final Module module) {
		ActorActionTask changeStateModuleTask = new ActorActionTask(mActivity);

		changeStateModuleTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if (success) {
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

	public void onSetTemperatureClick(Double value, String moduleId) {
		Module module = mDevice.getModuleById(moduleId);
		if (module == null) {
			Log.e(TAG, "Can't load module for changing its value");
			return;
		}

		module.setValue(String.valueOf(value));
		doChangeStateModuleTask(module);
	}

	private void setParallaxMultiplier(View view, float multiplier) {
		CollapsingToolbarLayout.LayoutParams layoutParams = (CollapsingToolbarLayout.LayoutParams) view.getLayoutParams();
		layoutParams.setParallaxMultiplier(multiplier);
		view.setLayoutParams(layoutParams);
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
			return  mFragmentManager.findFragmentByTag(name);
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
