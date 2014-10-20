package cz.vutbr.fit.iha.adapter.device.units;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;

public abstract class BaseUnit {
	
	protected final List<Item> mItems = new ArrayList<Item>();

	public class Item {
		private final int mId;
		private final int mResUnitName;
		private final int mResUnitShortName;
	
		protected Item(int id, int resUnitName, int resUnitShortName) {
			this.mId = id;
			this.mResUnitName = resUnitName;
			this.mResUnitShortName = resUnitShortName;
		}
		
		/**
		 * @return SharedPreference ID
		 */
		public int getId() {
			return mId;
		}

		/**
		 * Get short form for unit. For example for celsius you will get "°C".
		 * 
		 * @param context
		 *            It can be app context
		 * @return Short form for unit
		 */
		public String getStringUnit(Context context) {
			return context.getString(mResUnitShortName);
		}

		/**
		 * Get full name for unit. For example for celsius you will get "Celsius".
		 * 
		 * @param context
		 *            It can be app context
		 * @return String which
		 */
		public String getStringName(Context context) {
			return context.getString(mResUnitName);
		}

		/**
		 * Get full name with short form for unit. For example for celsius you will get "Celsius (°C)".
		 * 
		 * @param context
		 *            It can be app context
		 * @return String which
		 */
		public String getStringNameUnit(Context context) {
			return String.format("%s (%s)", getStringName(context), getStringUnit(context));
		}	
	}

	abstract public int getDefaultId();
	abstract public String getPersistenceKey();
	abstract public float convertValue(Item to, float value);
	
	public Item getDefault() {
		for (Item item : mItems) {
			if (item.getId() == getDefaultId()) {
				return item;
			}
		}
		return mItems.get(0); // TODO: is this correct for us?
	}
	
	/**
	 * @return List of values (name and short form of unit (ex.: Celsius (°C))) which will be visible for user
	 */
	public CharSequence[] getEntries(Context context) {
		List<String> list = new ArrayList<String>();
		for (Item item : mItems) {
			list.add(item.getStringNameUnit(context));
		}
		return list.toArray(new CharSequence[list.size()]);
	}

	/**
	 * @return List of IDs which will be saved in SharedPreferences.
	 */
	public CharSequence[] getEntryValues() {
		List<String> list = new ArrayList<String>();
		for (Item item : mItems) {
			list.add(String.valueOf(item.mId));
		}
		return list.toArray(new CharSequence[list.size()]);
	}

	/**
	 * Get Item by ID which will be saved in SharedPreferences.
	 * 
	 * @return If the ID exists, it returns Item object. Otherwise it returns default Item unit.
	 */
	private Item getItemById(int id) {
		for (Item item : mItems) {
			if (item.mId == id) {
				return item;
			}
		}
		return getDefault();
	}

	public Item fromSettings(SharedPreferences prefs) {
		String id = prefs.getString(getPersistenceKey(), String.valueOf(getDefault().getId()));
		return getItemById(Integer.parseInt(id));
	}
	
}
