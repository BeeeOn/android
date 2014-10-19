package cz.vutbr.fit.iha.adapter.device.units;

import cz.vutbr.fit.iha.R;

public class UnknownUnit extends BaseUnit {
	
	public static final int DEFAULT = 0;
	
	public UnknownUnit() {		
		super();

		mItems.add(this.new Item(DEFAULT, R.string.dev_unknown_unit, R.string.dev_unknown_unit));
	}
	
	@Override
	public int getDefaultId() {
		return DEFAULT;
	}

	@Override
	public String getPersistenceKey() {
		return "";
	}

	@Override
	public float convertValue(Item to, float value) {
		return value;
	}

}
