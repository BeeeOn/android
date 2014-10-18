package cz.vutbr.fit.iha.adapter.device.units;

import android.content.Context;

public interface IDeviceUnit {
	
	public String formatValue(float value);
	public String getUnit(Context context);
	public String getName(Context context);
	public String getNameWithUnit(Context context);
	
}
