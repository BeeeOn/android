package cz.vutbr.fit.iha.adapter.device.values;

import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.device.units.EmissionUnit;

public final class EmissionValue extends BaseDeviceValue {

	private int mValue = Integer.MAX_VALUE;

	private static EmissionUnit mUnit = new EmissionUnit();

	@Override
	public void setValue(String value) {
		super.setValue(value);
		mValue = Integer.parseInt(value);
	}

	@Override
	public int getIconResource() {
		return R.drawable.dev_emission;
	}

	@Override
	public EmissionUnit getUnit() {
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
