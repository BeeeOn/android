package cz.vutbr.fit.iha.util;

import android.content.Context;
import android.content.SharedPreferences;
import cz.vutbr.fit.iha.adapter.device.units.BaseUnit;
import cz.vutbr.fit.iha.adapter.device.values.BaseDeviceValue;

public class UnitsFormatter {
	
	private SharedPreferences mPrefs;
	private Context mContext;
	
	public UnitsFormatter(SharedPreferences prefs, Context context) {
		mPrefs = prefs;
		mContext = context;
	}
	
	public String getStringValue(BaseDeviceValue item) {
		BaseUnit.Item to = item.getUnit().fromSettings(mPrefs);
		float f = item.getUnit().convertValue(to, item.getFloatValue());
		
		return Utils.formatFloat(f);
	}
	
	/*
	public String getStringValue(BaseDeviceValue value) {
		BaseUnit.Item toUnit = getActiveUnitItem(value);
		
		if (value instanceof EmissionValue) {
			EmissionValue val = (EmissionValue) value;
			val.getUnit();
			
		} else if (value instanceof HumidityValue) {
			HumidityValue val = (HumidityValue) value;
			val.getUnit();
			
		} else if (value instanceof IlluminationValue) {
			IlluminationValue val = (IlluminationValue) value;
			val.getUnit();
			
		} else if (value instanceof NoiseValue) {
			NoiseValue val = (NoiseValue) value;
			val.getUnit();
			
		} else if (value instanceof OnOffValue) {
			OnOffValue val = (OnOffValue) value;
			val.getUnit();
			
		} else if (value instanceof OpenClosedValue) {
			OpenClosedValue val = (OpenClosedValue) value;
			val.getUnit();
			
		} else if (value instanceof PressureValue) {
			PressureValue val = (PressureValue) value;
			val.getUnit();
			
		} else if (value instanceof TemperatureValue) {
			TemperatureValue val = (TemperatureValue) value;
			
			float f = val.getUnit().convertValue(toUnit, val.getValue());
			return Utils.formatFloat(f);
	
		} else if (value instanceof UnknownValue) {
			UnknownValue val = (UnknownValue) value;
			val.getUnit();
			
		}
		
		return ""; // FIXME: return something meaningful
		
		//return value.formatValue(mPrefs);
	}*/
	
	public String getStringUnit(BaseDeviceValue value) {
		return value.getUnit().fromSettings(mPrefs).getUnit(mContext);
	}
	
	public String getStringValueUnit(BaseDeviceValue item) {
		String value = getStringValue(item);
		String unit = getStringUnit(item);
		
		if (unit.isEmpty())
			return value;
			
		return String.format("%s %s", value, unit);
	}

	public String formatRawValueUnit(float valueF, BaseDeviceValue item) {
		BaseUnit.Item to = item.getUnit().fromSettings(mPrefs);
		float f = item.getUnit().convertValue(to, valueF);
		
		String value = Utils.formatFloat(f);
		String unit = to.getUnit(mContext);

		if (unit.isEmpty())
			return value;
		
		return String.format("%s %s", value, unit);
	}
		
}
