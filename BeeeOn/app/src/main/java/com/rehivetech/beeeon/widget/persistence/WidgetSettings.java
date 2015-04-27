package com.rehivetech.beeeon.widget.persistence;

import android.content.Context;
import android.content.SharedPreferences;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;

/**
 * Created by Tomáš on 24. 4. 2015.
 */
public class WidgetSettings extends WidgetPersistence{

	protected static final String PREF_IS_COLOR_SCHEME = "is_color_scheme";
	protected static final String PREF_COLOR_PRIMARY = "color_primary";
	protected static final String PREF_COLOR_SECONDARY = "color_secondary";

	public boolean isColorScheme;
	public int colorPrimary;
	public int colorSecondary;

	private WidgetSettings(Context context, int widgetId) {
		super(context, widgetId);
	}

	public static WidgetSettings getSettings(Context context, int widgetId){
		WidgetSettings settings = new WidgetSettings(context, widgetId);
		settings.load();
		return settings;
	}

	@Override
	public void load() {
		isColorScheme = mPrefs.getBoolean(getProperty(PREF_IS_COLOR_SCHEME), false);
		colorPrimary = mPrefs.getInt(getProperty(PREF_COLOR_PRIMARY), R.color.beeeon_primary_cyan);
		colorSecondary = mPrefs.getInt(getProperty(PREF_COLOR_SECONDARY), R.color.beeeon_secundary_pink);
	}

	@Override
	public void save() {
		mPrefs.edit()
			.putBoolean(getProperty(PREF_IS_COLOR_SCHEME), isColorScheme)
			.putInt(getProperty(PREF_COLOR_PRIMARY), colorPrimary)
			.putInt(getProperty(PREF_COLOR_SECONDARY), colorSecondary)
			.apply();
	}

	@Override
	public void delete() {
		mPrefs.edit()
				.remove(getProperty(PREF_IS_COLOR_SCHEME))
				.remove(getProperty(PREF_COLOR_PRIMARY))
				.remove(getProperty(PREF_COLOR_SECONDARY))
				.apply();
	}

	public void setColorScheme(int primary, int secondary){
		isColorScheme = true;
		colorPrimary = primary;
		colorSecondary = secondary;
		save();
	}

	public boolean isColorSchemeEqual(int col1, int col2){
		if(colorPrimary == col1 && colorSecondary == col2) return true;

		return false;
	}

	@Override
	public String getPropertyPrefix() {
		return "settings";
	}
}