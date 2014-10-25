package cz.vutbr.fit.iha.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;

public abstract class SettingsItem {
	
	protected final List<BaseItem> mItems = new ArrayList<BaseItem>();

	public abstract class BaseItem {
		private final int mId;
	
		protected BaseItem(int id) {
			this.mId = id;
		}
		
		/**
		 * @return SharedPreference ID
		 */
		public int getId() {
			return mId;
		}

		abstract public String getSettingsName(Context context);
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
		List<String> list = new ArrayList<String>();
		for (BaseItem item : mItems) {
			list.add(item.getSettingsName(context));
		}
		return list.toArray(new CharSequence[list.size()]);
	}

	/**
	 * @return List of IDs which will be saved in SharedPreferences.
	 */
	public CharSequence[] getEntryValues() {
		List<String> list = new ArrayList<String>();
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

	public BaseItem fromSettings(SharedPreferences prefs) {
		String id = prefs.getString(getPersistenceKey(), String.valueOf(getDefault().getId()));
		return getItemById(Integer.parseInt(id));
	}
	
}
