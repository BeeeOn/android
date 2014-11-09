package cz.vutbr.fit.iha.adapter.device.values;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.device.units.BlankUnit;

public abstract class BaseEnumValue extends BaseValue {

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

}
