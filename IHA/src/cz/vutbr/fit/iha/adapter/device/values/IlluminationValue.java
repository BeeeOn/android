package cz.vutbr.fit.iha.adapter.device.values;

import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.device.units.IlluminationUnit;

public final class IlluminationValue extends BaseValue {

	private double mValue = Double.MAX_VALUE;

	private static IlluminationUnit mUnit = new IlluminationUnit();

	@Override
	public void setValue(String value) {
		super.setValue(value);
		mValue = Double.parseDouble(value);
	}

	@Override
	public int getIconResource() {
		return R.drawable.dev_illumination;
	}

	@Override
	public IlluminationUnit getUnit() {
		return mUnit;
	}

	public double getValue() {
		return mValue;
	}

	@Override
	public double getDoubleValue() {
		return mValue;
	}

}