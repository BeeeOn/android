package com.rehivetech.beeeon.household.device.values;

import android.support.annotation.NonNull;

import com.rehivetech.beeeon.IconResourceType;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.device.units.BlankUnit;
import com.rehivetech.beeeon.util.Log;

import java.util.Collections;
import java.util.List;

public class EnumValue extends BaseValue {

	private static final String TAG = EnumValue.class.getSimpleName();

	protected final List<Item> mItems;

	protected final Item mUnknownValue = new Item(-1, "", R.string.unit_blank_blank);

	private Item mValue = mUnknownValue;

	private static BlankUnit mUnit = new BlankUnit();

	public static class Item {
		private final int mId;
		private final String mValue;
		private final int mStringResource;

		public Item(int id, String value,  int stringResource) {
			mId = id;
			mValue = value;
			mStringResource = stringResource;
		}

		public int getId() {
			return mId;
		}

		public String getValue() {
			return mValue;
		}

		public int getStringResource() {
			return mStringResource;
		}
	}

	public EnumValue(@NonNull List<Item> items) {
		mItems = Collections.unmodifiableList(items);
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
	public int getIconResource(IconResourceType type) {
		// TODO: Support specific icon based on particular value type
		return (type == IconResourceType.DARK) ? R.drawable.ic_val_state_gray : R.drawable.ic_val_state;
	}

	@Override
	public int getActorIconResource(IconResourceType type) {
		// Enum values probably won't have special icon for actors
		return getIconResource(type);
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
			if (item.getId() == (int) value) {
				return item;
			}
		}
		return mUnknownValue;
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

	/**
	 * Actors support ***************************************************
	 */

	public boolean setNextValue() {
		return setValueToOffset(1);
	}

	public boolean setPrevValue() {
		return setValueToOffset(-1);
	}

	public boolean isActiveValue(String value) {
		return mValue.getValue().equalsIgnoreCase(value);
	}

	public Item getActive() {
		return mValue;
	}

}
