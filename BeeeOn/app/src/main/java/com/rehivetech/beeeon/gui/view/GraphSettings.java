package com.rehivetech.beeeon.gui.view;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;

import com.flipboard.bottomsheet.BottomSheetLayout;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.device.ModuleLog;
import com.rehivetech.beeeon.util.Utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by martin on 20.2.16.
 */
public class GraphSettings extends FrameLayout{

	private Context mContext;

	private Slider mSlider;
	private AppCompatCheckBox mCheckBoxMin;
	private AppCompatCheckBox mCheckBoxAvg;
	private AppCompatCheckBox mCheckBoxMax;

	private BottomSheetLayout mBottomSheetLayout;

	private GraphSettingsListener mGraphSettingsListener;

	private Map<String, ModuleLog.DataInterval> mSliderValues;

	public GraphSettings(Context context) {
		super(context);
		init(context);
	}

	public GraphSettings(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public GraphSettings(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	private void init(Context context) {
		mContext = context;

		View view = LayoutInflater.from(context).inflate(R.layout.graph_settings_layout, this, true);

		mCheckBoxMin = (AppCompatCheckBox) view.findViewById(R.id.graph_settings_checkbox_min);
		mCheckBoxAvg = (AppCompatCheckBox) view.findViewById(R.id.graph_settings_checkbox_avg);
		mCheckBoxMax = (AppCompatCheckBox) view.findViewById(R.id.graph_settings_checkbox_max);
		mSlider = (Slider) view.findViewById(R.id.graph_settings_slider);
		AppCompatButton buttonDone = (AppCompatButton) view.findViewById(R.id.graph_settings_button_done);

		mSliderValues = getIntervalString(ModuleLog.DataInterval.values());

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

		mSlider.setProgressChangeLister(new Slider.OnProgressChangeLister() {
			@Override
			public void onProgressChanged(String progress) {
				if (progress.equals(mContext.getString(R.string.data_interval_raw))) {
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
		buttonDone.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mGraphSettingsListener != null) {
					ModuleLog.DataInterval interval = getIntervalByProgress();
					mGraphSettingsListener.onButtonDoneClick(mCheckBoxMin.isChecked(), mCheckBoxAvg.isChecked(), mCheckBoxMax.isChecked(),interval, mSlider.getProgress());
				}
			}
		});

		mCheckBoxAvg.setTextColor(Utils.getGraphColor(mContext, 0));
		mCheckBoxMin.setTextColor(Utils.getGraphColor(mContext, 1));
		mCheckBoxMax.setTextColor(Utils.getGraphColor(mContext, 2));

		setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mBottomSheetLayout != null) {
					mBottomSheetLayout.dismissSheet();
				}
			}
		});
		view.findViewById(R.id.graph_settings_layout).setOnClickListener(null);
	}

	public void initGraphSettings(boolean checkBoxMinCheck, boolean checkBoxAvgCheck, boolean checkBoxMaxCheck, int sliderMinValue, int sliderMaxValue, int sliderActValue) {
		mSlider.setValues(new ArrayList<>(mSliderValues.keySet()).subList(sliderMinValue, sliderMaxValue));
		mSlider.setProgress(sliderActValue);
		mCheckBoxMin.setChecked(checkBoxMinCheck);
		mCheckBoxAvg.setChecked(checkBoxAvgCheck);
		mCheckBoxMax.setChecked(checkBoxMaxCheck);
	}

	public void setGraphSettingsListener(GraphSettingsListener listener) {
		mGraphSettingsListener = listener;
	}

	public void setBottomSheetLayout(BottomSheetLayout bottomSheetLayout) {
		mBottomSheetLayout = bottomSheetLayout;
	}

	public ModuleLog.DataInterval getIntervalByProgress() {
		String mSliderProgress = mSlider.getProgressString();

		if (mSliderProgress.equals(mContext.getString(R.string.data_interval_raw))) {
			return ModuleLog.DataInterval.RAW;

		} else if (mSliderProgress.equals(mContext.getString(R.string.data_interval_minute))) {
			return ModuleLog.DataInterval.MINUTE;

		} else if (mSliderProgress.equals(mContext.getString(R.string.data_interval_five_minutes))) {
			return ModuleLog.DataInterval.FIVE_MINUTES;

		} else if (mSliderProgress.equals(mContext.getString(R.string.data_interval_ten_minutes))) {
			return ModuleLog.DataInterval.TEN_MINUTES;

		} else if (mSliderProgress.equals(mContext.getString(R.string.data_interval_half_hour))) {
			return ModuleLog.DataInterval.HALF_HOUR;

		} else if (mSliderProgress.equals(mContext.getString(R.string.data_interval_hour))) {
			return ModuleLog.DataInterval.HOUR;

		} else if (mSliderProgress.equals(mContext.getString(R.string.data_interval_day))) {
			return ModuleLog.DataInterval.DAY;

		} else if (mSliderProgress.equals(mContext.getString(R.string.data_interval_week))) {
			return ModuleLog.DataInterval.WEEK;

		} else {
			return ModuleLog.DataInterval.MONTH;
		}
	}

	private Map<String, ModuleLog.DataInterval> getIntervalString(ModuleLog.DataInterval[] intervals) {
		Map<String, ModuleLog.DataInterval> intervalStringMap = new LinkedHashMap<>();

		for (ModuleLog.DataInterval interval : intervals) {

			switch (interval) {
				case RAW:
					intervalStringMap.put(mContext.getString(R.string.data_interval_raw),interval);
					break;
				case MINUTE:
					intervalStringMap.put(mContext.getString(R.string.data_interval_minute), interval);
					break;
				case FIVE_MINUTES:
					intervalStringMap.put(mContext.getString(R.string.data_interval_five_minutes), interval);
					break;
				case TEN_MINUTES:
					intervalStringMap.put(mContext.getString(R.string.data_interval_ten_minutes), interval);
					break;
				case HALF_HOUR:
					intervalStringMap.put(mContext.getString(R.string.data_interval_half_hour), interval);
					break;
				case HOUR:
					intervalStringMap.put(mContext.getString(R.string.data_interval_hour), interval);
					break;
				case DAY:
					intervalStringMap.put(mContext.getString(R.string.data_interval_day), interval);
					break;
				case WEEK:
					intervalStringMap.put(mContext.getString(R.string.data_interval_week), interval);
					break;
				case MONTH:
					intervalStringMap.put(mContext.getString(R.string.data_interval_month), interval);
					break;
			}
		}
		return intervalStringMap;
	}

	public interface GraphSettingsListener {
		void onButtonDoneClick(boolean minChecked, boolean avgChecked, boolean maxChecked,ModuleLog.DataInterval dataInterval, int sliderProgress);
	}
}
