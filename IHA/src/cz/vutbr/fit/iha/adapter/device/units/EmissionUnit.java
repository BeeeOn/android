package cz.vutbr.fit.iha.adapter.device.units;

import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.R;

public class EmissionUnit extends BaseUnit {
	
	public static final int DEFAULT = 0;
	
	public EmissionUnit() {
		super();

		mItems.add(this.new Item(DEFAULT, R.string.dev_emission_unit, R.string.dev_emission_unit));
	}
	
	@Override
	public int getDefaultId() {
		return DEFAULT;
	}

	@Override
	public String getPersistenceKey() {
		return Constants.PERSISTENCE_PREF_EMISSION;
	}

	@Override
	public float convertValue(Item to, float value) {
		return value;
	}

}
