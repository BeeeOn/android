package com.rehivetech.beeeon.household.device;

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
	},
	TYPE_3("3", "Virtual THN v1.0", R.string.devices__dev_virtual_thn, R.string.devices__manufacturer_virtual) {
		@Override
		public List<Module> createModules(Device device) {
			return Arrays.asList(
					new Module(device, "0", 0x02, null, null, R.string.devices__type_temperature, false, null, null),
					new Module(device, "1", 0x03, null, null, R.string.devices__type_humidity, false, null, null),
					new Module(device, "2", 0x06, null, null, R.string.devices__type_noise, false, null, null)
			);
		}
	},
	TYPE_4("4", "Virtual THNx3 v1.0", R.string.devices__dev_virtual_thn_x3, R.string.devices__manufacturer_virtual) {
		@Override
		public List<Module> createModules(Device device) {
			return Arrays.asList(
					new Module(device, "0", 0x02, null, R.string.devices__zone_1, R.string.devices__type_temperature, false, null, null),
					new Module(device, "1", 0x03, null, R.string.devices__zone_1, R.string.devices__type_humidity, false, null, null),
					new Module(device, "2", 0x06, null, R.string.devices__zone_1, R.string.devices__type_noise, false, null, null),
					new Module(device, "3", 0x02, null, R.string.devices__zone_2, R.string.devices__type_temperature, false, null, null),
					new Module(device, "4", 0x03, null, R.string.devices__zone_2, R.string.devices__type_humidity, false, null, null),
					new Module(device, "5", 0x06, null, R.string.devices__zone_2, R.string.devices__type_noise, false, null, null),
					new Module(device, "6", 0x02, null, R.string.devices__zone_3, R.string.devices__type_temperature, false, null, null),
					new Module(device, "7", 0x03, null, R.string.devices__zone_3, R.string.devices__type_humidity, false, null, null),
					new Module(device, "8", 0x06, null, R.string.devices__zone_3, R.string.devices__type_noise, false, null, null)
			);
		}
	},
	TYPE_5("5", "OpenHAB Presence v1.0", R.string.devices__dev_presence_monitor, R.string.devices__manufacturer_openhab) {
		@Override
		public List<Module> createModules(Device device) {
			return Arrays.asList(
					new Module(device, "0", 0x01, null, null, R.string.devices__mod_presence_status, false, null, Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__val_presence_status_undefined),
							new EnumValue.Item(1, "1", R.string.devices__val_presence_status_present),
							new EnumValue.Item(2, "2", R.string.devices__val_presence_status_not_present)
					), null)
			);
		}
	},
	TYPE_6("6", "Regulator VPT LAN v1.0", R.string.devices__dev_regulator_vpt_lan, R.string.devices__manufacturer_thermona) {
		@Override
		public List<Module> createModules(Device device) {
			return Arrays.asList(
					new Module(device, "0", 0x01, 13, R.string.devices__zone_1, R.string.devices__mod_boiler_operation_type, true, Arrays.asList(
							new Module.Rule(0, new int[] {1, 2, 3, 4, 5, 11, 12, 13, 65}),
							new Module.Rule(1, new int[] {4}),
							new Module.Rule(2, new int[] {2, 4}),
							new Module.Rule(3, new int[] {2, 4}),
							new Module.Rule(4, new int[] {2, 65})
					), Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__val_boiler_operation_type_off),
							new EnumValue.Item(1, "1", R.string.devices__val_boiler_operation_type_room_regulator),
							new EnumValue.Item(2, "2", R.string.devices__val_boiler_operation_type_equiterm_regulator),
							new EnumValue.Item(3, "3", R.string.devices__val_boiler_operation_type_constant_water_temperature),
							new EnumValue.Item(4, "4", R.string.devices__val_boiler_operation_type_hot_water)
					), null),
					new Module(device, "1", 0x01, 14, R.string.devices__zone_1, R.string.devices__mod_boiler_operation_mode, true, null, Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__val_boiler_operation_mode_automatic),
							new EnumValue.Item(1, "1", R.string.devices__val_boiler_operation_mode_manual),
							new EnumValue.Item(2, "2", R.string.devices__val_boiler_operation_mode_vacation)
					), null),
					new Module(device, "2", 0x02, 9, R.string.devices__zone_1, R.string.devices__mod_requested_room_temperature, false, null, new BaseValue.Constraints(0.0, 160.0, 0.5), null),
					new Module(device, "3", 0x02, 8, R.string.devices__zone_1, R.string.devices__mod_current_room_temperature, false, null, new BaseValue.Constraints(-20.0, 40.0, 0.5), null),
					new Module(device, "4", 0x02, 12, R.string.devices__zone_1, R.string.devices__mod_requested_water_temperature_set, true, null, new BaseValue.Constraints(50.0, 90.0, 1.0), null),
					new Module(device, "65", 0x02, 11, R.string.devices__zone_1, R.string.devices__mod_requested_water_temperature_read, false, null, new BaseValue.Constraints(10.0, 110.0, 0.5), null),
					new Module(device, "5", 0x02, 10, R.string.devices__zone_1, R.string.devices__mod_current_water_temperature, false, null, new BaseValue.Constraints(20.0, 120.0, 0.5), null),
					new Module(device, "11", 0x01, 15, R.string.devices__zone_1, R.string.devices__frost_protection, true, Arrays.asList(
							new Module.Rule(0, new int[] {12, 13})
					), Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__val_frost_protection_off),
							new EnumValue.Item(1, "1", R.string.devices__val_frost_protection_on)
					), null),
					new Module(device, "12", 0x02, 16, R.string.devices__zone_1, R.string.devices__frost_protection_water_temperature, true, null, new BaseValue.Constraints(25.0, 50.0, 1.0), null),
					new Module(device, "13", 0x02, 17, R.string.devices__zone_1, R.string.devices__frost_protection_threshold, true, null, new BaseValue.Constraints(0.0, 15.0, 1.0), null),
					new Module(device, "14", 0x01, 23, R.string.devices__zone_2, R.string.devices__mod_boiler_operation_type, true, Arrays.asList(
							new Module.Rule(0, new int[] {15, 16, 17, 18, 19, 25, 26, 27, 66}),
							new Module.Rule(1, new int[] {18}),
							new Module.Rule(2, new int[] {16, 18}),
							new Module.Rule(3, new int[] {16, 18}),
							new Module.Rule(4, new int[] {16, 66})
					), Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__val_boiler_operation_type_off),
							new EnumValue.Item(1, "1", R.string.devices__val_boiler_operation_type_room_regulator),
							new EnumValue.Item(2, "2", R.string.devices__val_boiler_operation_type_equiterm_regulator),
							new EnumValue.Item(3, "3", R.string.devices__val_boiler_operation_type_constant_water_temperature),
							new EnumValue.Item(4, "4", R.string.devices__val_boiler_operation_type_hot_water)
					), null),
					new Module(device, "15", 0x01, 24, R.string.devices__zone_2, R.string.devices__mod_boiler_operation_mode, true, null, Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__val_boiler_operation_mode_automatic),
							new EnumValue.Item(1, "1", R.string.devices__val_boiler_operation_mode_manual),
							new EnumValue.Item(2, "2", R.string.devices__val_boiler_operation_mode_vacation)
					), null),
					new Module(device, "16", 0x02, 19, R.string.devices__zone_2, R.string.devices__mod_requested_room_temperature, false, null, new BaseValue.Constraints(0.0, 160.0, 0.5), null),
					new Module(device, "17", 0x02, 18, R.string.devices__zone_2, R.string.devices__mod_current_room_temperature, false, null, new BaseValue.Constraints(-20.0, 40.0, 0.5), null),
					new Module(device, "18", 0x02, 22, R.string.devices__zone_2, R.string.devices__mod_requested_water_temperature_set, true, null, new BaseValue.Constraints(50.0, 90.0, 1.0), null),
					new Module(device, "66", 0x02, 21, R.string.devices__zone_2, R.string.devices__mod_requested_water_temperature_read, false, null, new BaseValue.Constraints(10.0, 110.0, 0.5), null),
					new Module(device, "19", 0x02, 20, R.string.devices__zone_2, R.string.devices__mod_current_water_temperature, false, null, new BaseValue.Constraints(20.0, 120.0, 0.5), null),
					new Module(device, "25", 0x01, 25, R.string.devices__zone_2, R.string.devices__frost_protection, true, Arrays.asList(
							new Module.Rule(0, new int[] {26, 27})
					), Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__val_frost_protection_off),
							new EnumValue.Item(1, "1", R.string.devices__val_frost_protection_on)
					), null),
					new Module(device, "26", 0x02, 26, R.string.devices__zone_2, R.string.devices__frost_protection_water_temperature, true, null, new BaseValue.Constraints(25.0, 50.0, 1.0), null),
					new Module(device, "27", 0x02, 27, R.string.devices__zone_2, R.string.devices__frost_protection_threshold, true, null, new BaseValue.Constraints(0.0, 15.0, 1.0), null),
					new Module(device, "28", 0x01, 33, R.string.devices__zone_3, R.string.devices__mod_boiler_operation_type, true, Arrays.asList(
							new Module.Rule(0, new int[] {29, 30, 31, 32, 33, 39, 40, 41, 67}),
							new Module.Rule(1, new int[] {32}),
							new Module.Rule(2, new int[] {30, 32}),
							new Module.Rule(3, new int[] {30, 32}),
							new Module.Rule(4, new int[] {30, 67})
					), Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__val_boiler_operation_type_off),
							new EnumValue.Item(1, "1", R.string.devices__val_boiler_operation_type_room_regulator),
							new EnumValue.Item(2, "2", R.string.devices__val_boiler_operation_type_equiterm_regulator),
							new EnumValue.Item(3, "3", R.string.devices__val_boiler_operation_type_constant_water_temperature),
							new EnumValue.Item(4, "4", R.string.devices__val_boiler_operation_type_hot_water)
					), null),
					new Module(device, "29", 0x01, 34, R.string.devices__zone_3, R.string.devices__mod_boiler_operation_mode, true, null, Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__val_boiler_operation_mode_automatic),
							new EnumValue.Item(1, "1", R.string.devices__val_boiler_operation_mode_manual),
							new EnumValue.Item(2, "2", R.string.devices__val_boiler_operation_mode_vacation)
					), null),
					new Module(device, "30", 0x02, 29, R.string.devices__zone_3, R.string.devices__mod_requested_room_temperature, false, null, new BaseValue.Constraints(0.0, 160.0, 0.5), null),
					new Module(device, "31", 0x02, 28, R.string.devices__zone_3, R.string.devices__mod_current_room_temperature, false, null, new BaseValue.Constraints(-20.0, 40.0, 0.5), null),
					new Module(device, "32", 0x02, 32, R.string.devices__zone_3, R.string.devices__mod_requested_water_temperature_set, true, null, new BaseValue.Constraints(50.0, 90.0, 1.0), null),
					new Module(device, "67", 0x02, 31, R.string.devices__zone_3, R.string.devices__mod_requested_water_temperature_read, false, null, new BaseValue.Constraints(10.0, 110.0, 0.5), null),
					new Module(device, "33", 0x02, 30, R.string.devices__zone_3, R.string.devices__mod_current_water_temperature, false, null, new BaseValue.Constraints(20.0, 120.0, 0.5), null),
					new Module(device, "39", 0x01, 35, R.string.devices__zone_3, R.string.devices__frost_protection, true, Arrays.asList(
							new Module.Rule(0, new int[] {40, 41})
					), Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__val_frost_protection_off),
							new EnumValue.Item(1, "1", R.string.devices__val_frost_protection_on)
					), null),
					new Module(device, "40", 0x02, 36, R.string.devices__zone_3, R.string.devices__frost_protection_water_temperature, true, null, new BaseValue.Constraints(25.0, 50.0, 1.0), null),
					new Module(device, "41", 0x02, 37, R.string.devices__zone_3, R.string.devices__frost_protection_threshold, true, null, new BaseValue.Constraints(0.0, 15.0, 1.0), null),
					new Module(device, "42", 0x01, 43, R.string.devices__zone_4, R.string.devices__mod_boiler_operation_type, true, Arrays.asList(
							new Module.Rule(0, new int[] {43, 44, 45, 46, 47, 53, 54, 55, 68}),
							new Module.Rule(1, new int[] {46}),
							new Module.Rule(2, new int[] {44, 46}),
							new Module.Rule(3, new int[] {44, 46}),
							new Module.Rule(4, new int[] {44, 68})
					), Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__val_boiler_operation_type_off),
							new EnumValue.Item(1, "1", R.string.devices__val_boiler_operation_type_room_regulator),
							new EnumValue.Item(2, "2", R.string.devices__val_boiler_operation_type_equiterm_regulator),
							new EnumValue.Item(3, "3", R.string.devices__val_boiler_operation_type_constant_water_temperature),
							new EnumValue.Item(4, "4", R.string.devices__val_boiler_operation_type_hot_water)
					), null),
					new Module(device, "43", 0x01, 44, R.string.devices__zone_4, R.string.devices__mod_boiler_operation_mode, true, null, Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__val_boiler_operation_mode_automatic),
							new EnumValue.Item(1, "1", R.string.devices__val_boiler_operation_mode_manual),
							new EnumValue.Item(2, "2", R.string.devices__val_boiler_operation_mode_vacation)
					), null),
					new Module(device, "44", 0x02, 39, R.string.devices__zone_4, R.string.devices__mod_requested_room_temperature, false, null, new BaseValue.Constraints(0.0, 160.0, 0.5), null),
					new Module(device, "45", 0x02, 38, R.string.devices__zone_4, R.string.devices__mod_current_room_temperature, false, null, new BaseValue.Constraints(-20.0, 40.0, 0.5), null),
					new Module(device, "46", 0x02, 42, R.string.devices__zone_4, R.string.devices__mod_requested_water_temperature_set, true, null, new BaseValue.Constraints(50.0, 90.0, 1.0), null),
					new Module(device, "68", 0x02, 41, R.string.devices__zone_4, R.string.devices__mod_requested_water_temperature_read, false, null, new BaseValue.Constraints(10.0, 110.0, 0.5), null),
					new Module(device, "47", 0x02, 40, R.string.devices__zone_4, R.string.devices__mod_current_water_temperature, false, null, new BaseValue.Constraints(20.0, 120.0, 0.5), null),
					new Module(device, "53", 0x01, 45, R.string.devices__zone_4, R.string.devices__frost_protection, true, Arrays.asList(
							new Module.Rule(0, new int[] {54, 55})
					), Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__val_frost_protection_off),
							new EnumValue.Item(1, "1", R.string.devices__val_frost_protection_on)
					), null),
					new Module(device, "54", 0x02, 46, R.string.devices__zone_4, R.string.devices__frost_protection_water_temperature, true, null, new BaseValue.Constraints(25.0, 50.0, 1.0), null),
					new Module(device, "55", 0x02, 47, R.string.devices__zone_4, R.string.devices__frost_protection_threshold, true, null, new BaseValue.Constraints(0.0, 15.0, 1.0), null),
					new Module(device, "56", 0x01, 6, R.string.devices__boiler, R.string.devices__mod_boiler_status, false, null, Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__val_boiler_status_undefined),
							new EnumValue.Item(1, "1", R.string.devices__val_boiler_status_heating),
							new EnumValue.Item(2, "2", R.string.devices__val_boiler_status_hot_water),
							new EnumValue.Item(3, "3", R.string.devices__val_boiler_status_failure),
							new EnumValue.Item(4, "4", R.string.devices__val_boiler_status_shutdown)
					), null),
					new Module(device, "57", 0x01, 5, R.string.devices__boiler, R.string.devices__mod_boiler_mode, false, null, Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__val_boiler_mode_undefined),
							new EnumValue.Item(1, "1", R.string.devices__val_boiler_mode_on),
							new EnumValue.Item(2, "2", R.string.devices__val_boiler_mode_off)
					), null),
					new Module(device, "58", 0x02, 0, R.string.devices__boiler, R.string.devices__mod_current_water_temperature, false, null, new BaseValue.Constraints(20.0, 120.0, 1.0), null),
					new Module(device, "59", 0x02, 1, R.string.devices__boiler, R.string.devices__mod_current_outside_temperature, false, null, new BaseValue.Constraints(-50.0, 60.0, 1.0), null),
					new Module(device, "60", 0x02, 2, R.string.devices__boiler, R.string.devices__mod_average_outside_temperature, false, null, new BaseValue.Constraints(-40.0, 40.0, 1.0), null),
					new Module(device, "61", 0x03, 4, R.string.devices__boiler, R.string.devices__mod_current_boiler_performance, false, null, new BaseValue.Constraints(0.0, 100.0, 1.0), null),
					new Module(device, "62", 0x04, 3, R.string.devices__boiler, R.string.devices__mod_current_boiler_pressure, false, null, new BaseValue.Constraints(0.0, 10.0, 0.02), null),
					new Module(device, "63", 0x0B, 7, R.string.devices__boiler, R.string.devices__mod_current_boiler_error, false, null, null)
			);
		}
	};

	/** Version from specification of this devices list */
	public static final String DEVICES_VERSION = "1";

	/** Generation time (GMT) of this devices list */
	public static final long DEVICES_DATE = 1448486007727l;

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
