package com.rehivetech.beeeon.household.device.values;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.rehivetech.beeeon.IconResourceType;
import com.rehivetech.beeeon.household.device.ModuleType;
import com.rehivetech.beeeon.household.device.units.BaseUnit;

public abstract class BaseValue {

	private String mRawValue = null;

	private Constraints mConstraints;

	public abstract BaseUnit getUnit();

	public abstract int getIconResource(IconResourceType type);

	public abstract int getActorIconResource(IconResourceType type);

	public abstract double getDoubleValue();

	/**
	 * @return "Raw" string value, which means value given in {@link #setValue(String)} method
	 * @throws IllegalStateException When mRawValue is null (probably child object didn't call'd super.setValue())
	 */
	public final String getRawValue() throws IllegalStateException {
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

	public boolean hasValue() {
		return mRawValue != null;
	}

	@Nullable
	public Constraints getConstraints() {
		return mConstraints;
	}

	public void setConstraints(@Nullable Constraints constraints) {
		mConstraints = constraints;
	}

	@NonNull
	public static BaseValue createFromModuleType(@NonNull ModuleType type, @Nullable Constraints constraints) {
		try {
			// Try to create and return new BaseValue object
			BaseValue value = type.getValueClass().newInstance();
			value.setConstraints(constraints);
			return value;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		// If creation failed, create UnknownValue object
		return new UnknownValue();
	}

	public static class Constraints {
		private final Double min;
		private final Double max;
		private final Double granularity;

		public Constraints(Double min, Double max, Double granularity) {
			this.min = min;
			this.max = max;
			this.granularity = granularity;
		}
	}

}