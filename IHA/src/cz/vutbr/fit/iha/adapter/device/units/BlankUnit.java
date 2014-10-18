package cz.vutbr.fit.iha.adapter.device.units;

import android.content.Context;
import cz.vutbr.fit.iha.util.Utils;

// FIXME: I think this class shoudln't exists at all
public class BlankUnit implements IDeviceUnit {
	
	@Override
	public String getUnit(Context context) {
		return "";
	}

	@Override
	public String getName(Context context) {
		return "";
	}

	@Override
	public String getNameWithUnit(Context context) {
		return "";
	}
	
	@Override
	public String formatValue(float value) {
		return Utils.formatFloat(value);
	}

}
