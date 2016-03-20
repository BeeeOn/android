package com.rehivetech.beeeon.persistence;

import android.content.SharedPreferences;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Persistence helper for graph settings
 *
 * @author martin
 */
public class GraphSettingsPersistence {

	public static final String CHECKBOX_MIN = "checkbox_min";
	public static final String CHECKBOX_AVG = "checkbox_avg";
	public static final String CHECKBOX_MAX = "checkbox_max";

	private static final String SLIDER_PROGRESS = "slider_progress";

	@Retention(RetentionPolicy.SOURCE)
	@StringDef({CHECKBOX_MIN, CHECKBOX_AVG, CHECKBOX_MAX})
	public @interface CheckBoxes {}

	SharedPreferences mPreferences;

	public GraphSettingsPersistence(SharedPreferences preferences) {
		mPreferences = preferences;
	}

	/**
	 * Save actual values of settings checkboxes
	 * @param checkBoxMin boolean value
	 * @param checkBoxAvg boolean value
	 * @param checkBoxMax boolean value
	 */
	public void saveCheckBoxesStates(boolean checkBoxMin, boolean checkBoxAvg, boolean checkBoxMax) {
		SharedPreferences.Editor editor = mPreferences.edit();

		editor.putBoolean(CHECKBOX_MIN, checkBoxMin);
		editor.putBoolean(CHECKBOX_AVG, checkBoxAvg);
		editor.putBoolean(CHECKBOX_MAX, checkBoxMax);

		editor.apply();
	}

	/**
	 * Save actual progress of settings slider
	 * @param sliderValue value of slider
	 */
	public void saveSliderValue(int sliderValue) {
		SharedPreferences.Editor editor = mPreferences.edit();

		editor.putInt(SLIDER_PROGRESS, sliderValue);

		editor.apply();
	}

	/**
	 * Restore checkbox value
	 * @param which define which checkbox to be restored
	 * @param defaultValue default value
	 * @return checkbox restored value
	 */
	public boolean restoreCheckboxValue(@CheckBoxes String which, boolean defaultValue) {
		return mPreferences.getBoolean(which, defaultValue);
	}


	/**
	 * Restore slider value
	 * @param defaultValue slider default value
	 * @return slider restored value
	 */
	public int restoreSliderValue(int defaultValue) {
		return mPreferences.getInt(SLIDER_PROGRESS, defaultValue);
	}
}
