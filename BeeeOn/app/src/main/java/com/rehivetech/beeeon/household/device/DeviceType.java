package com.rehivetech.beeeon.household.device;

import android.support.annotation.StringRes;

import com.rehivetech.beeeon.IIdentifier;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.device.values.EnumValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum DeviceType implements IIdentifier {
	// FIXME: Features now share the object... It could have the default refresh but cannot hold the real value. So maybe rework it to enum and mark only exists/not exists? .. hm, but then how to save the default timeout?
	// FIXME: There is another possibility to not use these enums at all. And create everything on runtime and separate it amongst every instances of the Device object. But probably keep it this way. It will be better to have only one instance of DeviceType.

	TYPE_UNKNOWN("", "???", R.string.unknown_device, R.string.unknown_manufacturer, new DeviceFeatures(null, false, false)) {
		@Override
		public List<Module> createModules(Device device) {
			// TODO: What return here? Support unknown types at all?
			return new ArrayList<>();
		}
	},

	/** BEGIN OF GENERATED CONTENT **/
	TYPE_0("0", "BeeeOn v1.0", R.string.devices__dev_temperature_humidity, R.string.devices__manufacturer_but, new DeviceFeatures(30, true, true)) {
		@Override
		public List<Module> createModules(Device device) {
			return Arrays.asList(
					new Module(device, "0", 0x02, 0, null, null, R.string.devices__mod_room_temperature, false),
					new Module(device, "1", 0x02, 1, null, null, R.string.devices__mod_outside_temperature, false),
					new Module(device, "2", 0x03, 0, null, null, R.string.devices__mod_room_humidity, false)
			);
		}
	},
	TYPE_1("1", "Regulator VPT v1.0", R.string.devices__dev_regulator_vpt, R.string.devices__manufacturer_thermona, new DeviceFeatures(null, false, false)) {
		@Override
		public List<Module> createModules(Device device) {
			return Arrays.asList(
					new Module(device, "0", 0x01, 0, null, R.string.devices__zone_1, R.string.devices__mod_boiler_operation_type, true, Arrays.asList(
							new EnumValue.Item(0, "0", 0, 0, 0, 0),
							new EnumValue.Item(1, "1", 0, 0, 0, 0),
							new EnumValue.Item(2, "2", 0, 0, 0, 0),
							new EnumValue.Item(3, "3", 0, 0, 0, 0),
							new EnumValue.Item(4, "4", 0, 0, 0, 0)
					)),
					new Module(device, "1", 0x01, 1, null, R.string.devices__zone_1, R.string.devices__mod_boiler_operation_mode, true, Arrays.asList(
							new EnumValue.Item(0, "0", 0, 0, 0, 0),
							new EnumValue.Item(1, "1", 0, 0, 0, 0),
							new EnumValue.Item(2, "2", 0, 0, 0, 0)
					)),
					new Module(device, "2", 0x02, 0, null, R.string.devices__zone_1, R.string.devices__mod_requested_room_temperature, true),
					new Module(device, "3", 0x02, 0, null, R.string.devices__zone_1, R.string.devices__mod_current_room_temperature, false),
					new Module(device, "4", 0x02, 1, null, R.string.devices__zone_1, R.string.devices__mod_requested_water_temperature, true),
					new Module(device, "5", 0x02, 1, null, R.string.devices__zone_1, R.string.devices__mod_current_water_temperature, false),
					new Module(device, "6", 0x01, 2, null, R.string.devices__zone_2, R.string.devices__mod_boiler_operation_type, true, Arrays.asList(
							new EnumValue.Item(0, "0", 0, 0, 0, 0),
							new EnumValue.Item(1, "1", 0, 0, 0, 0),
							new EnumValue.Item(2, "2", 0, 0, 0, 0),
							new EnumValue.Item(3, "3", 0, 0, 0, 0),
							new EnumValue.Item(4, "4", 0, 0, 0, 0)
					)),
					new Module(device, "7", 0x01, 3, null, R.string.devices__zone_2, R.string.devices__mod_boiler_operation_mode, true, Arrays.asList(
							new EnumValue.Item(0, "0", 0, 0, 0, 0),
							new EnumValue.Item(1, "1", 0, 0, 0, 0),
							new EnumValue.Item(2, "2", 0, 0, 0, 0)
					)),
					new Module(device, "8", 0x02, 2, null, R.string.devices__zone_2, R.string.devices__mod_requested_room_temperature, true),
					new Module(device, "9", 0x02, 2, null, R.string.devices__zone_2, R.string.devices__mod_current_room_temperature, false),
					new Module(device, "10", 0x02, 3, null, R.string.devices__zone_2, R.string.devices__mod_requested_water_temperature, true),
					new Module(device, "11", 0x02, 3, null, R.string.devices__zone_2, R.string.devices__mod_current_water_temperature, false),
					new Module(device, "12", 0x01, 4, null, R.string.devices__zone_3, R.string.devices__mod_boiler_operation_type, true, Arrays.asList(
							new EnumValue.Item(0, "0", 0, 0, 0, 0),
							new EnumValue.Item(1, "1", 0, 0, 0, 0),
							new EnumValue.Item(2, "2", 0, 0, 0, 0),
							new EnumValue.Item(3, "3", 0, 0, 0, 0),
							new EnumValue.Item(4, "4", 0, 0, 0, 0)
					)),
					new Module(device, "13", 0x01, 5, null, R.string.devices__zone_3, R.string.devices__mod_boiler_operation_mode, true, Arrays.asList(
							new EnumValue.Item(0, "0", 0, 0, 0, 0),
							new EnumValue.Item(1, "1", 0, 0, 0, 0),
							new EnumValue.Item(2, "2", 0, 0, 0, 0)
					)),
					new Module(device, "14", 0x02, 4, null, R.string.devices__zone_3, R.string.devices__mod_requested_room_temperature, true),
					new Module(device, "15", 0x02, 4, null, R.string.devices__zone_3, R.string.devices__mod_current_room_temperature, false),
					new Module(device, "16", 0x02, 5, null, R.string.devices__zone_3, R.string.devices__mod_requested_water_temperature, true),
					new Module(device, "17", 0x02, 5, null, R.string.devices__zone_3, R.string.devices__mod_current_water_temperature, false),
					new Module(device, "18", 0x01, 6, null, R.string.devices__zone_4, R.string.devices__mod_boiler_operation_type, true, Arrays.asList(
							new EnumValue.Item(0, "0", 0, 0, 0, 0),
							new EnumValue.Item(1, "1", 0, 0, 0, 0),
							new EnumValue.Item(2, "2", 0, 0, 0, 0),
							new EnumValue.Item(3, "3", 0, 0, 0, 0),
							new EnumValue.Item(4, "4", 0, 0, 0, 0)
					)),
					new Module(device, "19", 0x01, 7, null, R.string.devices__zone_4, R.string.devices__mod_boiler_operation_mode, true, Arrays.asList(
							new EnumValue.Item(0, "0", 0, 0, 0, 0),
							new EnumValue.Item(1, "1", 0, 0, 0, 0),
							new EnumValue.Item(2, "2", 0, 0, 0, 0)
					)),
					new Module(device, "20", 0x02, 6, null, R.string.devices__zone_4, R.string.devices__mod_requested_room_temperature, true),
					new Module(device, "21", 0x02, 6, null, R.string.devices__zone_4, R.string.devices__mod_current_room_temperature, false),
					new Module(device, "22", 0x02, 7, null, R.string.devices__zone_4, R.string.devices__mod_requested_water_temperature, true),
					new Module(device, "23", 0x02, 7, null, R.string.devices__zone_4, R.string.devices__mod_current_water_temperature, false),
					new Module(device, "24", 0x01, 8, 0, null, R.string.devices__mod_boiler_status, false, Arrays.asList(
							new EnumValue.Item(0, "0", 0, 0, 0, 0),
							new EnumValue.Item(1, "1", 0, 0, 0, 0),
							new EnumValue.Item(2, "2", 0, 0, 0, 0),
							new EnumValue.Item(3, "3", 0, 0, 0, 0),
							new EnumValue.Item(4, "4", 0, 0, 0, 0)
					))
			);
		}
	};
	/** END OF GENERATED CONTENT **/

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

	public abstract List<Module> createModules(Device device);

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
