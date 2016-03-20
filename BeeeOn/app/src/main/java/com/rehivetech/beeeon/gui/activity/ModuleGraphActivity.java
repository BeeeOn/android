package com.rehivetech.beeeon.gui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
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

/**
 * @author martin on 18.8.2015.
 */
public class ModuleGraphActivity extends BaseApplicationActivity implements OnSheetDismissedListener, GraphSettings.GraphSettingsListener{
	private final static String TAG = ModuleGraphActivity.class.getSimpleName();

	private static final String EXTRA_GATE_ID = "gate_id";
	private static final String EXTRA_DEVICE_ID = "device_id";
	private static final String EXTRA_MODULE_ID = "module_id";

	private boolean mRequestRedrawActiveFragmentCalled = false;

	private String mGateId;
	private String mDeviceId;
	private String mModuleId;

	private TextView mMinValue;
	private TextView mMaxValue;
	private TextView mActValue;

	private TextView mMinValueLabel;
	private TextView mMaxValuelabel;

	private TabLayout mTabLayout;
	private ViewPager mViewPager;

	private GraphSettings mGraphSettings;
	BottomSheetLayout mBottomSheetLayout;
	private FloatingActionButton mFab;
	FloatingActionButton.OnVisibilityChangedListener mOnVisibilityChangedListener;
	private Button mShowLegendButton;

	private String mModuleUnit;

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
		Module module = controller.getDevicesModel().getDevice(mGateId, mDeviceId).getModuleById(mModuleId);

		UnitsHelper unitsHelper = Utils.getUnitsHelper(this);
		if (unitsHelper != null) {
			mModuleUnit = unitsHelper.getStringUnit(module.getValue());
		}

		Toolbar toolbar = (Toolbar) findViewById(R.id.beeeon_toolbar);
		toolbar.setTitle(module.getName(this));
		setSupportActionBar(toolbar);

		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setDisplayShowTitleEnabled(true);
		}

		mMinValue = (TextView) findViewById(R.id.module_graph_min_value);
		mMaxValue = (TextView) findViewById(R.id.module_graph_max_value);
		mActValue = (TextView) findViewById(R.id.module_graph_act_value);

		mMinValueLabel = (TextView) findViewById(R.id.module_graph_min_label);
		mMaxValuelabel = (TextView) findViewById(R.id.module_graph_max_label);

		mTabLayout = (TabLayout) findViewById(R.id.module_graph_tab_layoout);
		mViewPager = (ViewPager) findViewById(R.id.module_graph_view_pager);

		mBottomSheetLayout  = (BottomSheetLayout) findViewById(R.id.module_graph_botom_sheet_layout);
		mBottomSheetLayout.setPeekOnDismiss(true);
		mBottomSheetLayout.addOnSheetDismissedListener(this);

		mGraphSettings = new GraphSettings(this);
		mGraphSettings.setGraphSettingsListener(this);
		mGraphSettings.setBottomSheetLayout(mBottomSheetLayout);
		mGraphSettings.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

		mFab = (FloatingActionButton) findViewById(R.id.module_graph_fab);

		mShowLegendButton = (Button) findViewById(R.id.module_graph_show_legend_btn);

		setupViewPager();

		if (module.getValue() instanceof EnumValue) {
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

		mFab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mFab.hide(mOnVisibilityChangedListener);
			}
		});

		updateActValue();
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				callbackTaskManager.cancelAndRemoveAll();
				finish();
				break;
		}
		return false;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		callbackTaskManager.cancelAndRemoveAll();
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

				callbackTaskManager.cancelAndRemoveAll();
				initActiveFragment();
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});
	}

	private void updateActValue() {
		BaseValue value = Controller.getInstance(this).getDevicesModel().getDevice(mGateId, mDeviceId).getModuleById(mModuleId).getValue();
		if (value instanceof EnumValue) {
			mActValue.setText(((EnumValue) value).getStateStringResource());

			mMinValue.setVisibility(View.GONE);
			mMinValueLabel.setVisibility(View.GONE);

			mMaxValue.setVisibility(View.GONE);
			mMaxValuelabel.setVisibility(View.GONE);

		} else {
			mActValue.setText(String.format("%.2f %s", value.getDoubleValue(), mModuleUnit));
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

	public void setMinValue(String minValue) {
		if (minValue.length() == 0) {
			mMinValueLabel.setVisibility(View.INVISIBLE);
			mMinValue.setText("");
		} else {
			mMinValueLabel.setVisibility(View.VISIBLE);
			mMinValue.setText(String.format("%s %s", minValue, mModuleUnit));
		}
	}

	public void setMaxValue(String maxValue) {
		if (maxValue.length() == 0) {
			mMaxValuelabel.setVisibility(View.INVISIBLE);
			mMaxValue.setText("");
		} else {
			mMaxValuelabel.setVisibility(View.VISIBLE);
			mMaxValue.setText(String.format("%s %s", maxValue, mModuleUnit));
		}
	}

	public void setShowLegendButtonOnClickListener(View.OnClickListener onClickListener) {
		mShowLegendButton.setOnClickListener(onClickListener);
	}

	public void setRequestRedrawActiveFragmentCalled(boolean requestRedrawActiveFragmentCalled) {
		mRequestRedrawActiveFragmentCalled = requestRedrawActiveFragmentCalled;
	}

	@Override
	public void onDismissed(BottomSheetLayout bottomSheetLayout) {
		mFab.show(mOnVisibilityChangedListener);
	}

	@Override
	public void onButtonDoneClick(boolean minChecked, boolean avgChecked, boolean maxChecked, ModuleLog.DataInterval dataInterval, int sliderProgress) {
		callbackTaskManager.cancelAndRemoveAll();
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
