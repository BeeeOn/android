package com.rehivetech.beeeon.widget.persistence;

import android.content.Context;
import android.content.SharedPreferences;

import com.rehivetech.beeeon.R;

/**
 * Created by Tomáš on 24. 4. 2015.
 */
public class WidgetSettings{
	private static final String TAG = WidgetSettings.class.getSimpleName();

	protected static final String PREF_FILENAME = "widget_%d_settings";
	protected static final String PREF_IS_COLOR_SCHEME = "is_color_scheme";
	protected static final String PREF_COLOR_PRIMARY = "color_primary";
	protected static final String PREF_COLOR_SECONDARY = "color_secondary";

	public boolean isColorScheme;
	public int colorPrimary;
	public int colorSecondary;

	private Context mContext;
	private int mWidgetId;
	private SharedPreferences mPrefs;

	private WidgetSettings(Context context, int widgetId){
		mContext = context;
		mWidgetId = widgetId;
		mPrefs = getPrefFile();
	}

	public static WidgetSettings getSettings(Context context, int widgetId){
		WidgetSettings settings = new WidgetSettings(context, widgetId);
		settings.load();
		return settings;
	}

	public void load(){
		isColorScheme = mPrefs.getBoolean(PREF_IS_COLOR_SCHEME, false);
		colorPrimary = mPrefs.getInt(PREF_COLOR_PRIMARY, R.color.beeeon_primary_cyan);
		colorSecondary = mPrefs.getInt(PREF_COLOR_SECONDARY, R.color.beeeon_secundary_pink);
	}

	public void save(){
		mPrefs.edit()
				.putBoolean(PREF_IS_COLOR_SCHEME, isColorScheme)
				.putInt(PREF_COLOR_PRIMARY, colorPrimary)
				.putInt(PREF_COLOR_SECONDARY, colorSecondary)
				.apply();
	}

	public void delete(){
		mPrefs.edit().clear().apply();
	}

	private SharedPreferences getPrefFile(){
		return mContext.getSharedPreferences(String.format(PREF_FILENAME, mWidgetId), Context.MODE_PRIVATE);
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
}