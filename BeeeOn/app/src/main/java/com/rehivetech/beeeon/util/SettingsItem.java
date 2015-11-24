package com.rehivetech.beeeon.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class SettingsItem {

	protected final List<BaseItem> mItems = new ArrayList<>();

	public class BaseItem {
		protected final int mId;
		protected final int mResName;

		protected BaseItem(int id, int resName) {
			this.mId = id;
			this.mResName = resName;
		}

		/**
		 * @return SharedPreference ID
		 */
		public int getId() {
			return mId;
		}

		public String getSettingsName(Context context) {
			return context.getString(mResName);
		}
	}

	abstract public int getDefaultId();

	abstract public String getPersistenceKey();

	public BaseItem getDefault() {
		for (BaseItem item : mItems) {
			if (item.getId() == getDefaultId()) {
				return item;
			}
		}
		throw new IllegalStateException("There is no item with id = getDefaultId()");
	}

	/**
	 * @return List of values to be shown in settings
	 */
	public CharSequence[] getEntries(Context context) {
		List<String> list = new ArrayList<>();
		for (BaseItem item : mItems) {
			list.add(item.getSettingsName(context));
		}
		return list.toArray(new CharSequence[list.size()]);
	}

	/**
	 * @return List of IDs which will be saved in SharedPreferences.
	 */
	public CharSequence[] getEntryValues() {
		List<String> list = new ArrayList<>();
		for (BaseItem item : mItems) {
			list.add(String.valueOf(item.mId));
		}
		return list.toArray(new CharSequence[list.size()]);
	}

	/**
	 * Get Item by ID which will be saved in SharedPreferences.
	 *
	 * @return If the ID exists, it returns Item object. Otherwise it returns default Item.
	 */
	private BaseItem getItemById(int id) {
		for (BaseItem item : mItems) {
			if (item.mId == id) {
				return item;
			}
		}
		return getDefault();
	}

	/**
	 * Get Item representing value chosen by user in settings.
	 *
	 * @param prefs If given null, it return default Item
	 * @return user chosen Item or default Item, if user didn't chose it in settings yet.
	 */
	public BaseItem fromSettings(@Nullable SharedPreferences prefs) {
		BaseItem defaultItem = getDefault();

		// If we don't have options, return default Item
		if (prefs == null) {
			return defaultItem;
		}

		// NOTE: optimization is to cache this value (e.g. in static attribute of child object - or in some extra object with all settings), and update it automatically when settings is changed...
		String id = prefs.getString(getPersistenceKey(), String.valueOf(defaultItem.getId()));
		return getItemById(Utils.parseIntSafely(id, defaultItem.getId()));
	}

}
