package com.rehivetech.beeeon.household.device.values;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.device.units.BlankUnit;
import com.rehivetech.beeeon.util.Log;

public abstract class BaseEnumValue extends BaseValue {

	private static final String TAG = BaseEnumValue.class.getSimpleName();
	
	protected final List<Item> mItems = new ArrayList<Item>();

	protected final Item mUnknownValue = this.new Item(-1, "", R.drawable.dev_unknown, R.string.dev_unknown_unit, Color.BLACK);

	private Item mValue = mUnknownValue;

	private static BlankUnit mUnit = new BlankUnit();

	public class Item {
		private final int mId;
		private final String mValue;
		private final int mIconResource;
		private final int mStringResource;
		private final int mColor;

		protected Item(int id, String value, int iconResource, int stringResource, int color) {
			mId = id;
			mValue = value;
			mIconResource = iconResource;
			mStringResource = stringResource;
			mColor = color;
		}

		public int getId() {
			return mId;
		}
		
		public String getValue() {
			return mValue;
		}

		public int getIconResource() {
			return mIconResource;
		}

		public int getStringResource() {
			return mStringResource;
		}

		public int getColor() {
			return mColor;
		}
	}

	@Override
	public void setValue(String value) {
		super.setValue(value);
		mValue = getItemByValue(value);
	}
	
	public void setValue(Item item) {
		super.setValue(item.getValue());
		mValue = item;
	}

	@Override
	public int getIconResource() {
		return mValue.getIconResource();
	}

	@Override
	public BlankUnit getUnit() {
		return mUnit;
	}

	@Override
	public double getDoubleValue() {
		return mValue.getId();
	}

	/**
	 * @param value
	 * @return Item object with specified value, or mUnknownValue Item
	 */
	protected Item getItemByValue(String value) {
		for (Item item : mItems) {
			if (item.getValue().equalsIgnoreCase(value)) {
				return item;
			}
		}
		return mUnknownValue;
	}
	
	/**
	 * @param value
	 * @return Item object with specified double value (= id for now), or mUnknownValue Item
	 */
	public Item getItemByDoubleValue(double value) {
		for (Item item : mItems) {
			if (item.getId() == (int)value) {
				return item;
			}
		}
		return mUnknownValue;
	}

	/**
	 * @return Color depending on active value
	 */
	public int getStateColor() {
		return mValue.getColor();
	}

	/**
	 * @return Resource for human readable string representing active value
	 */
	public int getStateStringResource() {
		return mValue.getStringResource();
	}
	
	public List<Item> getEnumItems() {
		return mItems;
	}

	private boolean setValueToOffset(int offset) {
		int pos = mItems.indexOf(mValue);
		if (pos == -1) {
			Log.e(TAG, "Item was not found (probably unknown value), we can't use any offset");
			return false;
		}
		
		if (mItems.size() < 2) {
			Log.e(TAG, "There are less than 2 items in this value, we can't use any offset");
			return false;
		}

		pos = (pos + offset) % mItems.size();
		setValue(mItems.get(pos));
		return true;
	}
	
	/** Actors support ****************************************************/

	public boolean setNextValue() {
		return setValueToOffset(1);
	}
	
	public boolean setPrevValue() {
		return setValueToOffset(-1);
	}

	public boolean isActiveValue(String value) {
		return mValue.getValue().equalsIgnoreCase(value);
	}

}
