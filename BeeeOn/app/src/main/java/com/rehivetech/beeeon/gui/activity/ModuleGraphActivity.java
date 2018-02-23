package com.rehivetech.beeeon.gui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flipboard.bottomsheet.BottomSheetLayout;
import com.flipboard.bottomsheet.OnSheetDismissedListener;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gcm.analytics.GoogleAnalyticsManager;
import com.rehivetech.beeeon.gui.fragment.ModuleGraphFragment;
import com.rehivetech.beeeon.gui.view.GraphSettings;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.ModuleLog;
import com.rehivetech.beeeon.household.device.values.BaseValue;
import com.rehivetech.beeeon.household.device.values.EnumValue;
import com.rehivetech.beeeon.util.ChartHelper;
import com.rehivetech.beeeon.util.UnitsHelper;
import com.rehivetech.beeeon.util.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author martin on 18.8.2015.
 */
public class ModuleGraphActivity extends BaseApplicationActivity implements OnSheetDismissedListener, GraphSettings.GraphSettingsListener {

	private static final String EXTRA_GATE_ID = "gate_id";
	private static final String EXTRA_DEVICE_ID = "device_id";
	private static final String EXTRA_MODULE_ID = "module_id";

	private boolean mRequestRedrawActiveFragmentCalled = false;

	private String mGateId;
	private String mDeviceId;
	private String mModuleId;

	@BindView(R.id.module_graph_act_value)
	TextView mActValue;
	@BindView(R.id.module_graph_tab_layoout)
	TabLayout mTabLayout;
	@BindView(R.id.module_graph_view_pager)
	ViewPager mViewPager;
	@BindView(R.id.module_graph_botom_sheet_layout)
	BottomSheetLayout mBottomSheetLayout;
	@BindView(R.id.module_graph_fab)
	FloatingActionButton mFab;
	@BindView(R.id.module_graph_show_legend_btn)
	Button mShowLegendButton;

	private GraphSettings mGraphSettings;
	FloatingActionButton.OnVisibilityChangedListener mOnVisibilityChangedListener;

	private Module mModule;
	private @Nullable UnitsHelper mUnitsHelper;

	public static Intent getActivityIntent(Context context, String gateId, String deviceId, String moduleId) {
		Intent intent = new Intent(context, ModuleGraphActivity.class);
		intent.putExtra(EXTRA_GATE_ID, gateId);
		intent.putExtra(EXTRA_DEVICE_ID, deviceId);
		intent.putExtra(EXTRA_MODULE_ID, moduleId);

		return intent;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_module_graph);
		ButterKnife.bind(this);

		Bundle bundle = getIntent().getExtras();

		if (bundle != null) {
			mGateId = bundle.getString(EXTRA_GATE_ID);
			mDeviceId = bundle.getString(EXTRA_DEVICE_ID);
			mModuleId = bundle.getString(EXTRA_MODULE_ID);
		}


		if (mGateId == null || mModuleId == null) {
			Toast.makeText(this, R.string.module_detail_toast_not_specified_gate_or_module, Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		Controller controller = Controller.getInstance(this);
		mModule = controller.getDevicesModel().getDevice(mGateId, mDeviceId).getModuleById(mModuleId);
		mUnitsHelper = Utils.getUnitsHelper(this);

		setupToolbar(mModule.getName(this), INDICATOR_BACK);

		mBottomSheetLayout.setPeekOnDismiss(true);
		mBottomSheetLayout.addOnSheetDismissedListener(this);

		mGraphSettings = new GraphSettings(this);
		mGraphSettings.setGraphSettingsListener(this);
		mGraphSettings.setBottomSheetLayout(mBottomSheetLayout);
		mGraphSettings.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

		setupViewPager();

		if (mModule.getValue() instanceof EnumValue) {
			mFab.setVisibility(View.GONE);
		}

		mOnVisibilityChangedListener = new FloatingActionButton.OnVisibilityChangedListener() {
			@Override
			public void onShown(FloatingActionButton fab) {
				super.onShown(fab);
			}

			@Override
			public void onHidden(FloatingActionButton fab) {
				super.onHidden(fab);
				mBottomSheetLayout.removeOnSheetDismissedListener(ModuleGraphActivity.this);
				mBottomSheetLayout.addOnSheetDismissedListener(ModuleGraphActivity.this);
				mBottomSheetLayout.showWithSheetView(mGraphSettings);
			}
		};

		setupRefreshIcon(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				initActiveFragment();
			}
		});

		updateActualValue();
	}

	@Override
	public void onResume() {
		super.onResume();

		if (!mRequestRedrawActiveFragmentCalled) {
			mRequestRedrawActiveFragmentCalled = true;
			mViewPager.post(new Runnable() {
				@Override
				public void run() {
					initActiveFragment();
				}
			});
		}
	}

	@OnClick(R.id.module_graph_fab)
	@SuppressWarnings("unused")
	public void onFloatingActionButtonClicked() {
		mFab.hide(mOnVisibilityChangedListener);
		GoogleAnalyticsManager.getInstance().logEvent(GoogleAnalyticsManager.EVENT_CATEGORY_MODULE_GRAPH_DETAIL, GoogleAnalyticsManager.EVENT_ACTION_OPEN_GRAPH_SETTINGS, String.valueOf(mViewPager.getCurrentItem()));
	}

	private void setupViewPager() {
		GraphPagerAdapter adapter = new GraphPagerAdapter(getSupportFragmentManager());

		for (int dataRange : ChartHelper.ALL_RANGES) {
			ModuleGraphFragment fragment = ModuleGraphFragment.newInstance(mGateId, mDeviceId, mModuleId, dataRange);
			adapter.addFragment(fragment, getString(ChartHelper.getIntervalString(dataRange)));
		}

		mViewPager.setAdapter(adapter);
		mTabLayout.setupWithViewPager(mViewPager);

		mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			@Override
			public void onPageSelected(int position) {

				if (!mRequestRedrawActiveFragmentCalled) {
					mRequestRedrawActiveFragmentCalled = true;
				}

				callbackTaskManager.cancelAllTasks();
				initActiveFragment();

				GoogleAnalyticsManager.getInstance().logEvent(GoogleAnalyticsManager.EVENT_CATEGORY_MODULE_GRAPH_DETAIL, GoogleAnalyticsManager.EVENT_ACTION_SELECT_TAB, String.valueOf(position));
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});
	}

	/**
	 * Updates actual value in toolbar and hides min/max values if enum value
	 */
	private void updateActualValue() {
		BaseValue value = Controller.getInstance(this).getDevicesModel().getDevice(mGateId, mDeviceId).getModuleById(mModuleId).getValue();
		if (value instanceof EnumValue) {
			mActValue.setText(((EnumValue) value).getState(this));

		} else {
			mActValue.setText(UnitsHelper.format(mUnitsHelper, value));
			mShowLegendButton.setVisibility(View.GONE);
		}
	}

	private void initActiveFragment() {
		ModuleGraphFragment currentFragment = (ModuleGraphFragment) ((GraphPagerAdapter) mViewPager.getAdapter()).getActiveFragment(mViewPager, mViewPager.getCurrentItem());
		mGraphSettings = currentFragment.onFragmentChange(mGraphSettings);
	}

	private void redrawActiveFragment(boolean checkBoxMinChecked, boolean checkboxAvgChecked, boolean checkboxMaxChecked, ModuleLog.DataInterval dataGranularity, int sliderProgress) {
		ModuleGraphFragment currentFragment = (ModuleGraphFragment) ((GraphPagerAdapter) mViewPager.getAdapter()).getActiveFragment(mViewPager, mViewPager.getCurrentItem());

		currentFragment.onChartSettingChanged(checkBoxMinChecked, checkboxAvgChecked, checkboxMaxChecked, dataGranularity, sliderProgress);
	}

	public void setShowLegendButtonOnClickListener(View.OnClickListener onClickListener) {
		mShowLegendButton.setOnClickListener(onClickListener);
	}

	public void setRequestRedrawActiveFragmentCalled(boolean requestRedrawActiveFragmentCalled) {
		mRequestRedrawActiveFragmentCalled = requestRedrawActiveFragmentCalled;
	}

	@Override
	public void onDismissed(BottomSheetLayout bottomSheetLayout) {
		callbackTaskManager.cancelAllTasks();
		mFab.show(mOnVisibilityChangedListener);
	}

	@Override
	public void onButtonDoneClick(boolean minChecked, boolean avgChecked, boolean maxChecked, ModuleLog.DataInterval dataInterval, int sliderProgress) {
		callbackTaskManager.cancelAllTasks();
		mBottomSheetLayout.dismissSheet();
		redrawActiveFragment(minChecked, avgChecked, maxChecked, dataInterval, sliderProgress);
	}

	private static class GraphPagerAdapter extends FragmentPagerAdapter {

		private final List<Fragment> mFragments = new ArrayList<>();
		private final List<String> mFragmentTitles = new ArrayList<>();
		private final FragmentManager mFragmentManager;

		public GraphPagerAdapter(FragmentManager fm) {
			super(fm);
			mFragmentManager = fm;
		}

		@Override
		public Fragment getItem(int position) {
			return mFragments.get(position);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return mFragmentTitles.get(position);
		}

		@Override
		public int getCount() {
			return mFragments.size();
		}

		public void addFragment(Fragment fragment, String title) {
			mFragments.add(fragment);
			mFragmentTitles.add(title);
		}

		public Fragment getActiveFragment(ViewPager container, int position) {
			String name = makeFragmentName(container.getId(), position);
			return mFragmentManager.findFragmentByTag(name);
		}

		private static String makeFragmentName(int viewId, int index) {
			return "android:switcher:" + viewId + ":" + index;
		}
	}


	public interface ChartSettingListener {
		void onChartSettingChanged(boolean drawMin, boolean drawAvg, boolean drawMax, ModuleLog.DataInterval dataGranularity, int sliderProgress);

		GraphSettings onFragmentChange(GraphSettings settings);
	}
}
