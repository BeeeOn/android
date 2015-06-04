package com.rehivetech.beeeon.household.device;

import android.support.annotation.StringRes;

import com.rehivetech.beeeon.IIdentifier;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.device.values.EnumValue;
import com.rehivetech.beeeon.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum DeviceType implements IIdentifier {

	/** BEGIN OF GENERATED CONTENT **/
	// FIXME: Features now share the object... It could have the default refresh but cannot hold the real value. So maybe rework it to enum and mark only exists/not exists? .. hm, but then how to save the default timeout?
	// FIXME: There is another possibility to not use these enums at all. And create everything on runtime and separate it amongst every instances of the Device object. But probably keep it this way. It will be better to have only one instance of DeviceType.
	TYPE_0("0", "BeeeOn v1.0", R.string.devices__dev_temperature_humidity, R.string.devices__manufacturer_but, new DeviceFeatures(30, true, true)) {
		@Override
		protected List<Module> createModules(Device device) {
			return Arrays.asList(
					new Module(device, "0", 0x02, 0, null, 0, R.string.devices__mod_room_temperature, false),
					new Module(device, "1", 0x02, 1, null, 0, R.string.devices__mod_outside_temperature, false),
					new Module(device, "2", 0x03, 0, null, 0, R.string.devices__mod_room_humidity, false)
			);
		}
	},
	TYPE_1("1", "Regulator VPT v1.0", R.string.devices__dev_regulator_vpt, R.string.devices__manufacturer_thermona, new DeviceFeatures(null, false, false)) {
		@Override
		protected List<Module> createModules(Device device) {
			return Arrays.asList(
					new Module(device, "0", 0x01, 0, null, R.string.devices__zone_1, R.string.devices__mod_boiler_operation_type, true, Arrays.asList(
							new EnumValue.Item(0, "0", R.drawable.ic_val_unknown, R.drawable.ic_val_unknown_gray, R.string.val_boiler_operation_type_off, R.color.black),
							new EnumValue.Item(1, "1", R.drawable.ic_val_unknown, R.drawable.ic_val_unknown_gray, R.string.val_boiler_operation_type_room_regulator, R.color.black),
							new EnumValue.Item(2, "2", R.drawable.ic_val_unknown, R.drawable.ic_val_unknown_gray, R.string.val_boiler_operation_type_equiterm_regulator, R.color.black),
							new EnumValue.Item(3, "3", R.drawable.ic_val_unknown, R.drawable.ic_val_unknown_gray, R.string.val_boiler_operation_type_constant_water_temperature, R.color.black),
							new EnumValue.Item(4, "4", R.drawable.ic_val_unknown, R.drawable.ic_val_unknown_gray, R.string.val_boiler_operation_type_hot_water, R.color.black)
					))
					// TODO: more generated modules
			);
		}
	},
	/** END OF GENERATED CONTENT **/

	TYPE_UNKNOWN("", "???", R.string.unknown_device, R.string.unknown_manufacturer, new DeviceFeatures(null, false, false)) {
		@Override
		protected List<Module> createModules(Device device) {
			// TODO: What return here? Support unknown types at all?
			return new ArrayList<Module>();
		}
	};

	private final String mTypeId;
	private final String mTypeName;
	private final int mNameRes;
	private final int mManufacturerRes;
	private final DeviceFeatures mFeatures;

	DeviceType(String typeId, String typeName, @StringRes int nameRes, @StringRes int manufacturerRes, DeviceFeatures features) {
		mTypeId = typeId;
		mTypeName = typeName;
		mNameRes = nameRes;
		mManufacturerRes = manufacturerRes;
		mFeatures = features;
	}

	protected abstract List<Module> createModules(Device device);

	@Override
	public String getId() {
		return mTypeId;
	}

	public String getTypeName() {
		return mTypeName;
	}

	public int getNameRes() {
		return mNameRes;
	}

	public int getManufacturerRes() {
		return mManufacturerRes;
	}

	public DeviceFeatures getFeatures() {
		return mFeatures;
	}
}
