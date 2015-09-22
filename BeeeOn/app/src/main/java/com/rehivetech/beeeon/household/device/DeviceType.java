package com.rehivetech.beeeon.household.device;

import android.annotation.SuppressLint;
import android.support.annotation.StringRes;

import com.rehivetech.beeeon.IIdentifier;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.device.values.BaseValue;
import com.rehivetech.beeeon.household.device.values.EnumValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum DeviceType implements IIdentifier {
	TYPE_UNKNOWN("", "???", R.string.device_type_unknown_device, R.string.device_type_unknown_manufacturer) {
		@Override
		public List<Module> createModules(Device device) {
			return new ArrayList<>();
		}
	},

	/** BEGIN OF GENERATED CONTENT **/
	TYPE_0("0", "BeeeOn v1.0", R.string.devices__dev_temperature_humidity, R.string.devices__manufacturer_but) {
		@Override
		public List<Module> createModules(Device device) {
			return Arrays.asList(
					new Module(device, "0", 0x02, null, null, R.string.devices__mod_room_temperature, false, null, null),
					new Module(device, "1", 0x02, null, null, R.string.devices__mod_outside_temperature, false, null, null),
					new Module(device, "2", 0x03, null, null, R.string.devices__mod_room_humidity, false, null, null),
					new Module(device, "3", 0x08, null, null, null, false, null, null),
					new Module(device, "4", 0x09, null, null, null, false, null, null),
					new Module(device, "5", 0x0A, null, null, null, true, null, new BaseValue.Constraints(5.0, 3600.0, 1.0), "30")
			);
		}
	},
	TYPE_1("1", "Regulator VPT v1.0", R.string.devices__dev_regulator_vpt, R.string.devices__manufacturer_thermona) {
		@Override
		public List<Module> createModules(Device device) {
			return Arrays.asList(
					new Module(device, "0", 0x01, null, R.string.devices__zone_1, R.string.devices__mod_boiler_operation_type, true, Arrays.asList(
							new Module.Rule(0, new int[] {1, 2, 3, 4, 5}),
							new Module.Rule(1, new int[] {4, 5}),
							new Module.Rule(2, new int[] {4, 5}),
							new Module.Rule(3, new int[] {2, 3}),
							new Module.Rule(4, new int[] {2, 3})
					), Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__val_boiler_operation_type_off),
							new EnumValue.Item(1, "1", R.string.devices__val_boiler_operation_type_room_regulator),
							new EnumValue.Item(2, "2", R.string.devices__val_boiler_operation_type_equiterm_regulator),
							new EnumValue.Item(3, "3", R.string.devices__val_boiler_operation_type_constant_water_temperature),
							new EnumValue.Item(4, "4", R.string.devices__val_boiler_operation_type_hot_water)
					), null),
					new Module(device, "1", 0x01, null, R.string.devices__zone_1, R.string.devices__mod_boiler_operation_mode, true, null, Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__val_boiler_operation_mode_automatic),
							new EnumValue.Item(1, "1", R.string.devices__val_boiler_operation_mode_manual),
							new EnumValue.Item(2, "2", R.string.devices__val_boiler_operation_mode_vacation)
					), null),
					new Module(device, "2", 0x02, null, R.string.devices__zone_1, R.string.devices__mod_requested_room_temperature, true, null, new BaseValue.Constraints(0.0, 160.0, 0.5), null),
					new Module(device, "3", 0x02, null, R.string.devices__zone_1, R.string.devices__mod_current_room_temperature, false, null, null),
					new Module(device, "4", 0x02, null, R.string.devices__zone_1, R.string.devices__mod_requested_water_temperature, true, null, new BaseValue.Constraints(20.0, 90.0, 0.5), null),
					new Module(device, "5", 0x02, null, R.string.devices__zone_1, R.string.devices__mod_current_water_temperature, false, null, null),
					new Module(device, "6", 0x01, null, R.string.devices__zone_2, R.string.devices__mod_boiler_operation_type, true, Arrays.asList(
							new Module.Rule(0, new int[] {7, 8, 9, 10, 11}),
							new Module.Rule(1, new int[] {10, 11}),
							new Module.Rule(2, new int[] {10, 11}),
							new Module.Rule(3, new int[] {8, 9}),
							new Module.Rule(4, new int[] {8, 9})
					), Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__val_boiler_operation_type_off),
							new EnumValue.Item(1, "1", R.string.devices__val_boiler_operation_type_room_regulator),
							new EnumValue.Item(2, "2", R.string.devices__val_boiler_operation_type_equiterm_regulator),
							new EnumValue.Item(3, "3", R.string.devices__val_boiler_operation_type_constant_water_temperature),
							new EnumValue.Item(4, "4", R.string.devices__val_boiler_operation_type_hot_water)
					), null),
					new Module(device, "7", 0x01, null, R.string.devices__zone_2, R.string.devices__mod_boiler_operation_mode, true, null, Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__val_boiler_operation_mode_automatic),
							new EnumValue.Item(1, "1", R.string.devices__val_boiler_operation_mode_manual),
							new EnumValue.Item(2, "2", R.string.devices__val_boiler_operation_mode_vacation)
					), null),
					new Module(device, "8", 0x02, null, R.string.devices__zone_2, R.string.devices__mod_requested_room_temperature, true, null, new BaseValue.Constraints(0.0, 160.0, 0.5), null),
					new Module(device, "9", 0x02, null, R.string.devices__zone_2, R.string.devices__mod_current_room_temperature, false, null, null),
					new Module(device, "10", 0x02, null, R.string.devices__zone_2, R.string.devices__mod_requested_water_temperature, true, null, new BaseValue.Constraints(20.0, 90.0, 0.5), null),
					new Module(device, "11", 0x02, null, R.string.devices__zone_2, R.string.devices__mod_current_water_temperature, false, null, null),
					new Module(device, "12", 0x01, null, R.string.devices__zone_3, R.string.devices__mod_boiler_operation_type, true, Arrays.asList(
							new Module.Rule(0, new int[] {13, 14, 15, 16, 17}),
							new Module.Rule(1, new int[] {16, 17}),
							new Module.Rule(2, new int[] {16, 17}),
							new Module.Rule(3, new int[] {14, 15}),
							new Module.Rule(4, new int[] {14, 15})
					), Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__val_boiler_operation_type_off),
							new EnumValue.Item(1, "1", R.string.devices__val_boiler_operation_type_room_regulator),
							new EnumValue.Item(2, "2", R.string.devices__val_boiler_operation_type_equiterm_regulator),
							new EnumValue.Item(3, "3", R.string.devices__val_boiler_operation_type_constant_water_temperature),
							new EnumValue.Item(4, "4", R.string.devices__val_boiler_operation_type_hot_water)
					), null),
					new Module(device, "13", 0x01, null, R.string.devices__zone_3, R.string.devices__mod_boiler_operation_mode, true, null, Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__val_boiler_operation_mode_automatic),
							new EnumValue.Item(1, "1", R.string.devices__val_boiler_operation_mode_manual),
							new EnumValue.Item(2, "2", R.string.devices__val_boiler_operation_mode_vacation)
					), null),
					new Module(device, "14", 0x02, null, R.string.devices__zone_3, R.string.devices__mod_requested_room_temperature, true, null, new BaseValue.Constraints(0.0, 160.0, 0.5), null),
					new Module(device, "15", 0x02, null, R.string.devices__zone_3, R.string.devices__mod_current_room_temperature, false, null, null),
					new Module(device, "16", 0x02, null, R.string.devices__zone_3, R.string.devices__mod_requested_water_temperature, true, null, new BaseValue.Constraints(20.0, 90.0, 0.5), null),
					new Module(device, "17", 0x02, null, R.string.devices__zone_3, R.string.devices__mod_current_water_temperature, false, null, null),
					new Module(device, "18", 0x01, null, R.string.devices__zone_4, R.string.devices__mod_boiler_operation_type, true, Arrays.asList(
							new Module.Rule(0, new int[] {19, 20, 21, 22, 23}),
							new Module.Rule(1, new int[] {22, 23}),
							new Module.Rule(2, new int[] {22, 23}),
							new Module.Rule(3, new int[] {20, 21}),
							new Module.Rule(4, new int[] {20, 21})
					), Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__val_boiler_operation_type_off),
							new EnumValue.Item(1, "1", R.string.devices__val_boiler_operation_type_room_regulator),
							new EnumValue.Item(2, "2", R.string.devices__val_boiler_operation_type_equiterm_regulator),
							new EnumValue.Item(3, "3", R.string.devices__val_boiler_operation_type_constant_water_temperature),
							new EnumValue.Item(4, "4", R.string.devices__val_boiler_operation_type_hot_water)
					), null),
					new Module(device, "19", 0x01, null, R.string.devices__zone_4, R.string.devices__mod_boiler_operation_mode, true, null, Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__val_boiler_operation_mode_automatic),
							new EnumValue.Item(1, "1", R.string.devices__val_boiler_operation_mode_manual),
							new EnumValue.Item(2, "2", R.string.devices__val_boiler_operation_mode_vacation)
					), null),
					new Module(device, "20", 0x02, null, R.string.devices__zone_4, R.string.devices__mod_requested_room_temperature, true, null, new BaseValue.Constraints(0.0, 160.0, 0.5), null),
					new Module(device, "21", 0x02, null, R.string.devices__zone_4, R.string.devices__mod_current_room_temperature, false, null, null),
					new Module(device, "22", 0x02, null, R.string.devices__zone_4, R.string.devices__mod_requested_water_temperature, true, null, new BaseValue.Constraints(20.0, 90.0, 0.5), null),
					new Module(device, "23", 0x02, null, R.string.devices__zone_4, R.string.devices__mod_current_water_temperature, false, null, null),
					new Module(device, "24", 0x01, 0, null, R.string.devices__mod_boiler_status, false, null, Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__val_boiler_status_undefined),
							new EnumValue.Item(1, "1", R.string.devices__val_boiler_status_heating),
							new EnumValue.Item(2, "2", R.string.devices__val_boiler_status_hot_water),
							new EnumValue.Item(3, "3", R.string.devices__val_boiler_status_failure),
							new EnumValue.Item(4, "4", R.string.devices__val_boiler_status_shutdown)
					), null),
					new Module(device, "25", 0x09, null, null, null, false, null, null)
			);
		}
	},
	TYPE_2("2", "BeeeOn Internal Pressure v1.0", R.string.devices__dev_internal_pressure, R.string.devices__manufacturer_but) {
		@Override
		public List<Module> createModules(Device device) {
			return Arrays.asList(
					new Module(device, "0", 0x04, null, null, R.string.devices__type_pressure, false, null, null),
					new Module(device, "1", 0x0A, null, null, null, true, null, new BaseValue.Constraints(5.0, 3600.0, 1.0), "30")
			);
		}
	};

	/** Version from specification of this devices list */
	public static final String DEVICES_VERSION = "1";

	/** Generation time (GMT) of this devices list */
	public static final long DEVICES_DATE = 1439814118912l;

	/** END OF GENERATED CONTENT **/

	private final String mTypeId;
	private final String mTypeName;
	private final int mNameRes;
	private final int mManufacturerRes;

	DeviceType(String typeId, String typeName, @StringRes int nameRes, @StringRes int manufacturerRes) {
		mTypeId = typeId;
		mTypeName = typeName;
		mNameRes = nameRes;
		mManufacturerRes = manufacturerRes;
	}

	public abstract List<Module> createModules(Device device);

	@Override
	public String getId() {
		return mTypeId;
	}

	@SuppressWarnings("unused")
	public String getTypeName() {
		return mTypeName;
	}

	public int getNameRes() {
		return mNameRes;
	}

	public int getManufacturerRes() {
		return mManufacturerRes;
	}
}
