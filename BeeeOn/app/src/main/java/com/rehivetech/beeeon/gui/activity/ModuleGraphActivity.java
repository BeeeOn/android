package com.rehivetech.beeeon.gui.activity;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.fragment.ModuleGraphFragment;
import com.rehivetech.beeeon.gui.view.Slider;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.ModuleLog;
import com.rehivetech.beeeon.household.device.values.BaseValue;
import com.rehivetech.beeeon.household.device.values.EnumValue;
import com.rehivetech.beeeon.util.ChartHelper;
import com.rehivetech.beeeon.util.UnitsHelper;
import com.rehivetech.beeeon.util.Utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author martin on 18.8.2015.
 */
public class ModuleGraphActivity extends BaseApplicationActivity {
	private final static String TAG = ModuleGraphActivity.class.getSimpleName();

	public static final String EXTRA_GATE_ID = "gate_id";
	public static final String EXTRA_DEVICE_ID = "device_id";
	public static final String EXTRA_MODULE_ID = "module_id";

	private static final String OUT_STATE_CHECK_BOX_MIN = "check_box_min";
	private static final String OUT_STATE_CHECK_BOX_MAX = "check_box_max";
	private static final String OUT_STET_CHECK_BOX_AVG = "check_box_avg";
	private static final String OUT_STATE_SLIDER_PROGRESS = "slider_progress";

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

	private Slider mSlider;
	private AppCompatCheckBox mCheckBoxMin;
	private AppCompatCheckBox mCheckBoxAvg;
	private AppCompatCheckBox mCheckBoxMax;

	private Button mButtonCancel;
	private Button mButtonDone;
	private FloatingActionButton mFab;
	private Button mShowLegendButton;

	private String mModuleUnit;

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

		SharedPreferences prefs = controller.getUserSettings();
		UnitsHelper unitsHelper = new UnitsHelper(prefs, this);

		mModuleUnit = unitsHelper.getStringUnit(module.getValue());

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


		mCheckBoxMin = (AppCompatCheckBox) findViewById(R.id.module_graph_checkbox_min);
		mCheckBoxAvg = (AppCompatCheckBox) findViewById(R.id.module_graph_checkbox_avg);
		mCheckBoxMax = (AppCompatCheckBox) findViewById(R.id.module_graph_checkbox_max);

		mCheckBoxAvg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!isChecked) {
					if (!mCheckBoxMin.isChecked() && !mCheckBoxMax.isChecked()) {
						mCheckBoxAvg.setChecked(true);
					}
				}
			}
		});

		mCheckBoxMin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!isChecked) {
					if (!mCheckBoxAvg.isChecked() && !mCheckBoxMax.isChecked()) {
						mCheckBoxMin.setChecked(true);
					}
				}
			}
		});

		mCheckBoxMax.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!isChecked) {
					if (!mCheckBoxMin.isChecked() && !mCheckBoxAvg.isChecked()) {
						mCheckBoxMax.setChecked(true);
					}
				}
			}
		});

		mSlider = (Slider) findViewById(R.id.module_graph_slider);
		mSlider.setProgressChangeLister(new Slider.OnProgressChangeLister() {
			@Override
			public void onProgressChanged(int progress) {
				if (progress == 0) {
					mCheckBoxMin.setChecked(false);
					mCheckBoxAvg.setChecked(true);
					mCheckBoxMax.setChecked(false);
					mCheckBoxMin.setEnabled(false);
					mCheckBoxAvg.setEnabled(false);
					mCheckBoxMax.setEnabled(false);
				} else {
					mCheckBoxMin.setEnabled(true);
					mCheckBoxAvg.setEnabled(true);
					mCheckBoxMax.setEnabled(true);
				}
			}
		});

		TextView textMin = ((TextView) findViewById(R.id.module_graph_text_min));
		textMin.setTextColor(Utils.getGraphColor(this, 1));
		textMin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mCheckBoxMin.isEnabled()) {
					mCheckBoxMin.setChecked(!mCheckBoxMin.isChecked());
				}
			}
		});

		TextView textAvg = ((TextView) findViewById(R.id.module_graph_text_avg));
		textAvg.setTextColor(Utils.getGraphColor(this, 0));
		textAvg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mCheckBoxAvg.isEnabled()) {
					mCheckBoxAvg.setChecked(!mCheckBoxAvg.isChecked());
				}
			}
		});

		TextView textMax = ((TextView) findViewById(R.id.module_graph_text_max));
		textMax.setTextColor(Utils.getGraphColor(this, 2));
		textMax.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mCheckBoxMax.isEnabled()) {
					mCheckBoxMax.setChecked(!mCheckBoxMax.isChecked());
				}

			}
		});

		mFab = (FloatingActionButton) findViewById(R.id.module_graph_fab);
		mButtonCancel = (Button) findViewById(R.id.module_graph_button_cancel);
		mButtonDone = (Button) findViewById(R.id.module_graph_button_done);

		mShowLegendButton = (Button) findViewById(R.id.module_graph_show_legend_btn);

		setupViewPager();

		Map<ModuleLog.DataInterval, String> intervals = getIntervalString(ModuleLog.DataInterval.values());

		mSlider.setValues(new ArrayList<>(intervals.values()));

		if (module.getValue() instanceof EnumValue) {
			mFab.setVisibility(View.GONE);
		} else {
			mSlider.setProgress(2);  // default dataInterval 5 minutes
		}

		final View graphSettingsBackground = findViewById(R.id.module_graph_settings_background);

		final View graphSettings = findViewById(R.id.module_graph_graph_settings);
		graphSettings.setVisibility(View.GONE);

		final Animation animDown = AnimationUtils.loadAnimation(this, R.anim.graph_settings_anim_down);
		final Animation animUp = AnimationUtils.loadAnimation(this, R.anim.graph_settings_anim_up);


		final ObjectAnimator backgroundAnimUp = (ObjectAnimator) AnimatorInflater.loadAnimator(this, R.animator.graph_settings_background_animator_up);
		backgroundAnimUp.setTarget(graphSettingsBackground);
		backgroundAnimUp.setEvaluator(new ArgbEvaluator());
		backgroundAnimUp.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				super.onAnimationStart(animation);
				graphSettingsBackground.setVisibility(View.VISIBLE);
			}
		});

		final ObjectAnimator backgroundAnimDownDone = (ObjectAnimator) AnimatorInflater.loadAnimator(this, R.animator.graph_settings_background_animator_down);
		backgroundAnimDownDone.setTarget(graphSettingsBackground);
		backgroundAnimDownDone.setEvaluator(new ArgbEvaluator());

		final ObjectAnimator backgroundAnimDownCancel = (ObjectAnimator) AnimatorInflater.loadAnimator(this, R.animator.graph_settings_background_animator_down);
		backgroundAnimDownCancel.setTarget(graphSettingsBackground);
		backgroundAnimDownCancel.setEvaluator(new ArgbEvaluator());

		final FloatingActionButton.OnVisibilityChangedListener onVisibilityChangedListener = new FloatingActionButton.OnVisibilityChangedListener() {
			@Override
			public void onShown(FloatingActionButton fab) {
				super.onShown(fab);
				redrawActiveFragment();
			}

			@Override
			public void onHidden(FloatingActionButton fab) {
				super.onHidden(fab);
				graphSettings.setVisibility(View.VISIBLE);
				graphSettings.startAnimation(animUp);
				backgroundAnimUp.start();
			}
		};

		backgroundAnimDownDone.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				graphSettingsBackground.setVisibility(View.GONE);
				mFab.show(onVisibilityChangedListener);
			}
		});

		backgroundAnimDownCancel.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				super.onAnimationStart(animation);
				graphSettingsBackground.setClickable(false);
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				graphSettingsBackground.setVisibility(View.GONE);
				graphSettingsBackground.setClickable(true);
				mFab.show();
			}
		});

		mFab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mFab.hide(onVisibilityChangedListener);
				graphSettingsBackground.setVisibility(View.VISIBLE);
			}
		});

		mButtonCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				backgroundAnimDownCancel.start();
				graphSettings.startAnimation(animDown);
				graphSettings.setVisibility(View.GONE);
			}
		});

		mButtonDone.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				backgroundAnimDownDone.start();
				graphSettings.startAnimation(animDown);
				graphSettings.setVisibility(View.GONE);
			}
		});

		graphSettingsBackground.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				backgroundAnimDownCancel.start();
				graphSettings.startAnimation(animDown);
				graphSettings.setVisibility(View.GONE);
			}
		});

		if (savedInstanceState != null) {
			mCheckBoxMin.setChecked(savedInstanceState.getBoolean(OUT_STATE_CHECK_BOX_MIN));
			mCheckBoxAvg.setChecked(savedInstanceState.getBoolean(OUT_STET_CHECK_BOX_AVG));
			mCheckBoxMax.setChecked(savedInstanceState.getBoolean(OUT_STATE_CHECK_BOX_MAX));

			mSlider.setProgress(savedInstanceState.getInt(OUT_STATE_SLIDER_PROGRESS));
		} else {
			mCheckBoxAvg.setChecked(true);
		}

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
					redrawActiveFragment();
				}
			});
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				break;
		}
		return false;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(OUT_STATE_CHECK_BOX_MIN, mCheckBoxMin.isChecked());
		outState.putBoolean(OUT_STET_CHECK_BOX_AVG, mCheckBoxAvg.isChecked());
		outState.putBoolean(OUT_STATE_CHECK_BOX_MAX, mCheckBoxMax.isChecked());
		outState.putInt(OUT_STATE_SLIDER_PROGRESS, mSlider.getProgress());
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

				redrawActiveFragment();
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});
	}

	private void setupGraphSettings(int fragmentIndex) {
		switch (fragmentIndex) {
			case 0:
				mSlider.setMaxValue(4);
				break;
			case 1:
				mSlider.setMaxValue(5);
				break;
			case 2:
				mSlider.setMaxValue(6);
				break;
			case 3:
				mSlider.setMaxValue(7);
				break;
		}
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

	private Map<ModuleLog.DataInterval, String> getIntervalString(ModuleLog.DataInterval[] intervals) {
		Map<ModuleLog.DataInterval, String> intervalStringMap = new LinkedHashMap<>();

		for (ModuleLog.DataInterval interval : intervals) {

			switch (interval) {
				case RAW:
					intervalStringMap.put(interval, getString(R.string.data_interval_raw));
					break;
				case MINUTE:
					intervalStringMap.put(interval, getString(R.string.data_interval_minute));
					break;
				case FIVE_MINUTES:
					intervalStringMap.put(interval, getString(R.string.data_interval_five_minutes));
					break;
				case TEN_MINUTES:
					intervalStringMap.put(interval, getString(R.string.data_interval_ten_minutes));
					break;
				case HALF_HOUR:
					intervalStringMap.put(interval, getString(R.string.data_interval_half_hour));
					break;
				case HOUR:
					intervalStringMap.put(interval, getString(R.string.data_interval_hour));
					break;
				case DAY:
					intervalStringMap.put(interval, getString(R.string.data_interval_day));
					break;
				case WEEK:
					intervalStringMap.put(interval, getString(R.string.data_interval_week));
					break;
				case MONTH:
					intervalStringMap.put(interval, getString(R.string.data_interval_month));
					break;
			}
		}
		return intervalStringMap;
	}

	private ModuleLog.DataInterval getIntervalBySliderProgress() {

		switch (mSlider.getProgress()) {
			case 1:
				return ModuleLog.DataInterval.MINUTE;
			case 2:
				return ModuleLog.DataInterval.FIVE_MINUTES;
			case 3:
				return ModuleLog.DataInterval.TEN_MINUTES;
			case 4:
				return ModuleLog.DataInterval.HALF_HOUR;
			case 5:
				return ModuleLog.DataInterval.HOUR;
			case 6:
				return ModuleLog.DataInterval.DAY;
			case 7:
				return ModuleLog.DataInterval.WEEK;
			case 8:
				return ModuleLog.DataInterval.MONTH;
		}

		return ModuleLog.DataInterval.RAW;
	}

	private void redrawActiveFragment() {
		ModuleGraphFragment currentFragment = (ModuleGraphFragment) ((GraphPagerAdapter) mViewPager.getAdapter()).getActiveFragment(mViewPager, mViewPager.getCurrentItem());

		currentFragment.onChartSettingChanged(mCheckBoxMin.isChecked(), mCheckBoxAvg.isChecked(), mCheckBoxMax.isChecked(), getIntervalBySliderProgress());
		setupGraphSettings(mViewPager.getCurrentItem());
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
		void onChartSettingChanged(boolean drawMin, boolean drawAvg, boolean drawMax, ModuleLog.DataInterval dataGranularity);
	}
}
