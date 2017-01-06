package com.rehivetech.beeeon.gui.fragment;

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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.rehivetech.beeeon.gcm.analytics.GoogleAnalyticsManager;
import com.rehivetech.beeeon.gui.activity.BaseApplicationActivity;
import com.rehivetech.beeeon.gui.adapter.DeviceModuleAdapter;
import com.rehivetech.beeeon.gui.view.DeviceFeatureView;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.ModuleType;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.household.device.Status;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.ICallbackTaskFactory;
import com.rehivetech.beeeon.util.PreferencesHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.State;
import timber.log.Timber;

/**
 * @author martin
 * @since 04.08.2015
 */
public class DeviceDetailFragment extends BaseDeviceDetailFragment implements AppBarLayout.OnOffsetChangedListener {

	private static final String DEVICE_DETAIL_FRAGMENT_AUTO_RELOAD_ID = "deviceDetailFragmentAutoReload";

	@BindView(R.id.device_detail_root_layout)
	CoordinatorLayout mRootLayout;

	@BindView(R.id.device_detail_icon)
	ImageView mIcon;

	@BindView(R.id.device_detail_status_icon)
	ImageView mStatusIcon;

	@BindView(R.id.device_detail_device_name)
	TextView mDeviceName;

	@BindView(R.id.device_detail_module_list_empty_view)
	TextView mEmptyTextView;

	@BindView(R.id.device_detail_modules_list)
	RecyclerView mRecyclerView;

	@BindView(R.id.device_detail_group_pager)
	ViewPager mViewPager;

	@BindView(R.id.device_detail_group_tab_layout)
	TabLayout mTabLayout;

	private DeviceModuleAdapter mModuleAdapter;

	private DeviceFeatureView mDeviceLocation;
	private DeviceFeatureView mDeviceLastUpdate;
	private DeviceFeatureView mDeviceSignal;
	private DeviceFeatureView mDeviceBattery;
	private DeviceFeatureView mDeviceRefresh;

	@State
	int mSelectedTabIndex;

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
		fillArguments(args, gateId, deviceId);
		DeviceDetailFragment fragment = new DeviceDetailFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_device_detail, container, false);

		mUnbinder = ButterKnife.bind(this, view);

		AppBarLayout appBarLayout = ButterKnife.findById(view, R.id.device_detail_appbar);
		appBarLayout.addOnOffsetChangedListener(this);

		setParallaxMultiplier(mIcon, 0.9f);
		setParallaxMultiplier(mStatusIcon, 0.9f);
		setParallaxMultiplier(mDeviceName, 0.9f);

		if (mDevice == null)
			return view;

		LinearLayout featuresLayout = ButterKnife.findById(view, R.id.device_detail_features_layout);

		final Module rssi = mDevice.getFirstModuleByType(ModuleType.TYPE_RSSI);
		if (rssi != null) {
			mDeviceSignal = new DeviceFeatureView(mActivity);
			mDeviceSignal.setCaption(getString(R.string.module_detail_label_signal));
			mDeviceSignal.setIcon(R.drawable.ic_signal_wifi_4_bar_black_24dp);
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
			mDeviceBattery.setIcon(R.drawable.ic_battery_std_black_24dp);
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
			mDeviceRefresh.setIcon(R.drawable.ic_refresh_black_24dp);
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
		mDeviceLastUpdate.setIcon(R.drawable.ic_update_black_24dp);
		featuresLayout.addView(mDeviceLastUpdate);

		mDeviceLocation = new DeviceFeatureView(mActivity);
		mDeviceLocation.setCaption(getString(R.string.module_edit_label_detail_location));
		featuresLayout.addView(mDeviceLocation);


		List<String> moduleGroups = mDevice.getModulesGroups(mActivity, mHideUnavailableModules);

		if (moduleGroups.size() <= 1) {
			mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
			mModuleAdapter = new DeviceModuleAdapter(mActivity, this);
			mRecyclerView.setAdapter(mModuleAdapter);
			mRootLayout.removeView(mViewPager);
			mViewPager = null;
		} else {
			mTabLayout.setVisibility(View.VISIBLE);
			mRootLayout.removeView(mRecyclerView);
			setupViewPager(mViewPager, mTabLayout, mDevice.getModulesGroups(mActivity, mHideUnavailableModules));
		}

		return view;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (mViewPager != null) {
			mViewPager.setCurrentItem(mSelectedTabIndex);
		}

		Toolbar toolbar = mActivity.setupToolbar("", BaseApplicationActivity.INDICATOR_BACK);

		if (toolbar != null) {
			CollapsingToolbarLayout.LayoutParams layoutParams;
			layoutParams = (CollapsingToolbarLayout.LayoutParams) toolbar.getLayoutParams();
			layoutParams.setCollapseMode(CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PIN);
			toolbar.setLayoutParams(layoutParams);
		}

		setAutoReloadDataTimer();
		updateData();
	}

	@Override
	public void onResume() {
		super.onResume();

		GoogleAnalyticsManager.getInstance().logScreen(GoogleAnalyticsManager.DEVICE_DETAIL_SCREEN);

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
			mSelectedTabIndex = mViewPager.getCurrentItem();
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.activity_device_detail_menu, menu);
	}

	@Override
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
			mDeviceSignal.setValue(String.format(Locale.getDefault(), "%d%%", rssi));
			mDeviceSignal.setIcon(rssi == 0 ? R.drawable.ic_signal_wifi_off_black_24dp : R.drawable.ic_signal_wifi_4_bar_black_24dp);
		}

		// battery
		if (mDevice.getBattery() != null && mDeviceBattery != null) {
			mDeviceBattery.setValue(String.format(Locale.getDefault(), "%d%%", mDevice.getBattery()));
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
					Timber.d("updating viewpager fragment %s", fragment.getTag());
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

	/**
	 * Set automatic reload UI timer if is set in app settings
	 */
	private void setAutoReloadDataTimer() {
		SharedPreferences prefs = Controller.getInstance(getActivity()).getUserSettings();
		int period = PreferencesHelper.getInt(mActivity, prefs, R.string.pref_actualization_time_key);

		if (period > 0)    // zero means do not update
			mActivity.callbackTaskManager.executeTaskEvery(mICallbackTaskFactory, DEVICE_DETAIL_FRAGMENT_AUTO_RELOAD_ID, period);
	}

	/**
	 * Fill ViewPager with fragments by number of module groups and TabLayout with group title
	 * @param viewPager
	 * @param tabLayout
	 * @param moduleGroups
	 */
	private void setupViewPager(ViewPager viewPager, TabLayout tabLayout, List<String> moduleGroups) {
		ModuleGroupPagerAdapter adapter = new ModuleGroupPagerAdapter(getChildFragmentManager());
		for (String group : moduleGroups) {
			DeviceDetailGroupModuleFragment fragment = DeviceDetailGroupModuleFragment.newInstance(mGateId, mDeviceId, group);
			adapter.addFragment(fragment, group);
		}

		viewPager.setAdapter(adapter);
		tabLayout.setupWithViewPager(viewPager);
	}

	/**
	 * Create and execute task for device refresh from server
	 * @param gateId ID of current gateway
	 * @param forceReload true if can force refresh
	 */
	private void doReloadDevicesTask(final String gateId, final boolean forceReload) {
		// Execute and remember task so it can be stopped automatically
		if (mDeviceCallback != null)
			mActivity.callbackTaskManager.executeTask(mDeviceCallback.createReloadDevicesTask(forceReload), gateId);
	}

	/**
	 * Set Collapsing Toolbar LayoutParams with parallax multiplier
	 *
	 * See
	 * @link {{@link android.support.design.widget.CollapsingToolbarLayout.LayoutParams#setParallaxMultiplier(float)}}
	 *
	 * @param view View to set
	 * @param multiplier
	 */
	private void setParallaxMultiplier(View view, float multiplier) {
		CollapsingToolbarLayout.LayoutParams layoutParams = (CollapsingToolbarLayout.LayoutParams) view.getLayoutParams();
		layoutParams.setParallaxMultiplier(multiplier);
		view.setLayoutParams(layoutParams);
	}

	/**
	 * Interface for updating Device via Activity
	 */
	public interface UpdateDevice {
		CallbackTask createReloadDevicesTask(boolean forceReload);

		Device getDevice();
	}

	/**
	 * FragmentPagerAdapter for ViewPager
	 */
	static class ModuleGroupPagerAdapter extends FragmentPagerAdapter {
		private final List<Fragment> mFragments = new ArrayList<>();
		private final List<String> mFragmentTitles = new ArrayList<>();
		private final FragmentManager mFragmentManager;

		ModuleGroupPagerAdapter(FragmentManager fm) {
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

		Fragment getActiveFragment(ViewPager container, int position) {
			String name = makeFragmentName(container.getId(), position);
			return mFragmentManager.findFragmentByTag(name);
		}

		private static String makeFragmentName(int viewId, int index) {
			return "android:switcher:" + viewId + ":" + index;
		}

		void addFragment(Fragment fragment, String title) {
			mFragments.add(fragment);
			mFragmentTitles.add(title);
		}
	}
}
