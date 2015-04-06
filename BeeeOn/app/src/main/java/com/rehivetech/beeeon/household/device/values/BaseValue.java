package com.rehivetech.beeeon.household.device.values;

import com.rehivetech.beeeon.household.device.DeviceType;
import com.rehivetech.beeeon.household.device.units.BaseUnit;

public abstract class BaseValue {

	private String mRawValue = null;

	public abstract BaseUnit getUnit();

	public abstract int getIconResource();

	public abstract double getDoubleValue();

	/**
	 * @return "Raw" string value, which means value given in {@link #setValue(String)} method
	 * @throws IllegalStateException
	 *             When mRawValue is null (probably child object didn't call'd super.setValue())
	 */
	public final String getRawValue() {
		if (mRawValue == null) {
			throw new IllegalStateException("mRawValue in BaseDeviceValue is null, did child object called super.setValue()?");
		}

		return mRawValue;
	}

	/**
	 * Remember given value as mRawValue. <br>
	 * Child object should override this method, parse this value correctly and set it as own attribute to work with.
	 * 
	 * @param value
	 */
	public void setValue(String value) {
		mRawValue = value;
	}

	public static BaseValue createFromDeviceType(DeviceType type) {
		try {
			// Try to create and return new BaseValue object
			return type.getValueClass().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}

		// If creation failed, create UnknownValue object
		return new UnknownValue();
	}

}