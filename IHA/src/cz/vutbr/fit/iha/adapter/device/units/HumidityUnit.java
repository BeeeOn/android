package cz.vutbr.fit.iha.adapter.device.units;

import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.R;

public class HumidityUnit extends BaseUnit {

	public static final int DEFAULT = 0;

	public HumidityUnit() {
		super();

		mItems.add(this.new Item(DEFAULT, R.string.dev_humidity_unit, R.string.dev_humidity_unit));
	}

	@Override
	public int getDefaultId() {
		return DEFAULT;
	}

	@Override
	public String getPersistenceKey() {
		return Constants.PERSISTENCE_PREF_HUMIDITY;
	}

	@Override
	public double convertValue(Item to, double value) {
		return value;
	}

}
