package cz.vutbr.fit.iha.adapter.device.values;

import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.device.units.NoiseUnit;

public final class NoiseValue extends BaseDeviceValue {

	private int mValue = Integer.MAX_VALUE;
	
	private static NoiseUnit mUnit = new NoiseUnit();

	@Override
	public void setValue(String value) {
		mValue = Integer.parseInt(value);
	}
	
	@Override
	public String getStringValue() {
		return String.valueOf(mValue);
	}

	@Override
	public int getIconResource() {
		return R.drawable.dev_noise;
	}
	
	@Override
	public NoiseUnit getUnit() {
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
