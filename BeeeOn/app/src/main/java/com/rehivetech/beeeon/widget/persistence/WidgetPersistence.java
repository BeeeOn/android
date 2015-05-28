package com.rehivetech.beeeon.widget.persistence;

import android.content.Context;
import android.content.SharedPreferences;

import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;
import com.rehivetech.beeeon.widget.ViewsBuilder;
import com.rehivetech.beeeon.widget.data.WidgetData;

import java.util.List;

/**
 * Created by Tomáš on 26. 4. 2015.
 */
public abstract class WidgetPersistence {

	protected Context mContext;
	protected final int mWidgetId;
	protected final int mOffset;
	protected int mBoundView;
	protected ViewsBuilder mBuilder;
	protected SharedPreferences mPrefs;
	protected WidgetSettings mWidgetSettings;

	// helpers
	protected UnitsHelper mUnitsHelper;
	protected TimeHelper mTimeHelper;

	protected boolean mIsCached = false;

	/**
	 * If used this constructor, not available manipulating with GUI
	 *
	 * @param context
	 * @param widgetId
	 */
	WidgetPersistence(Context context, int widgetId) {
		mContext = context.getApplicationContext();
		mWidgetId = widgetId;
		mPrefs = getWidgetPreferences();
		mOffset = 0;
	}

	WidgetPersistence(Context context, int widgetId, int offset, int boundView, UnitsHelper unitsHelper, TimeHelper timeHelper, WidgetSettings settings) {
		mContext = context.getApplicationContext();
		mWidgetId = widgetId;
		mPrefs = getWidgetPreferences();
		mOffset = offset;
		mBoundView = boundView;
		mBuilder = new ViewsBuilder(mContext);
		mUnitsHelper = unitsHelper;
		mTimeHelper = timeHelper;
		mWidgetSettings = settings;
	}

	public abstract void load();

	public abstract void save();

	public abstract void delete();

	public abstract String getPropertyPrefix();

	public void configure() {
	}

	public void configure(Object obj1, Object obj2) {
	}

	public void configure(Object obj1, Object obj2, Object obj3) {
	}

	public void renderView(ViewsBuilder parentBuilder) {
		renderView(parentBuilder, false, "");
	}

	public void renderView(ViewsBuilder parentBuilder, boolean isCached, String cachedString) {
		mIsCached = isCached;
	}

	/**
	 * Gets shared preferences (the same as for widgetData)
	 *
	 * @return
	 */
	public SharedPreferences getWidgetPreferences() {
		return mContext.getSharedPreferences(String.format(WidgetData.PREF_FILENAME, mWidgetId), Context.MODE_PRIVATE);
	}

	/**
	 * Parse property in sharedPreferences file
	 *
	 * @param key
	 * @return
	 */
	public String getProperty(String key) {
		return String.format(getPropertyPrefix() + "_%d_%s", mOffset, key);
	}

	// ----------------------------------------------------------- //
	// ---------------------- GETTERS ---------------------------- //
	// ----------------------------------------------------------- //

	public ViewsBuilder getBuilder() {
		return mBuilder;
	}

	public int getOffset() {
		return mOffset;
	}

	public int getBoundView() {
		return mBoundView;
	}

	// ----------------------------------------------------------- //
	// ------ METHODS FOR WORKING WITH MORE OBJECTS AT ONCE ------ //
	// ----------------------------------------------------------- //

	public static <T extends WidgetPersistence> void loadAll(List<T> widgetPersistences) {
		if (widgetPersistences == null) return;
		for (WidgetPersistence per : widgetPersistences) {
			per.load();
		}
	}

	public static <T extends WidgetPersistence> void saveAll(List<T> widgetPersistences) {
		if (widgetPersistences == null) return;
		for (WidgetPersistence per : widgetPersistences) {
			per.save();
		}
	}

	public static <T extends WidgetPersistence> void deleteAll(List<T> widgetPersistences) {
		if (widgetPersistences == null) return;
		for (WidgetPersistence per : widgetPersistences) {
			per.delete();
		}
	}
}
