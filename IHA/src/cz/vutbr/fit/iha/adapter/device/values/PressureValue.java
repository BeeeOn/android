package cz.vutbr.fit.iha.adapter.device.values;

import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.device.units.PressureUnit;

public final class PressureValue extends BaseDeviceValue {

	private int mValue = Integer.MAX_VALUE;

	private static PressureUnit mUnit = new PressureUnit();

	@Override
	public void setValue(String value) {
		super.setValue(value);
		mValue = Integer.parseInt(value);
	}

	@Override
	public int getIconResource() {
		return R.drawable.dev_pressure;
	}

	@Override
	public PressureUnit getUnit() {
		return mUnit;
	}

	public int getValue() {
		return mValue;
	}

	@Override
	public float getFloatValue() {
		return mValue;
	}

}
