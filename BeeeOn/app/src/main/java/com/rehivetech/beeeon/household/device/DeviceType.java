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
					new Module(device, "0", 0x01, 11, R.string.devices__zone_1, R.string.devices__mod_boiler_operation_type, true, Arrays.asList(
							new Module.Rule(0, new int[] {1, 2, 3, 5, 6, 7, 11, 12, 13, 65, 73, 74, 75, 76}),
							new Module.Rule(1, new int[] {6, 7, 74, 75}),
							new Module.Rule(2, new int[] {2, 6, 7, 73, 74, 75}),
							new Module.Rule(3, new int[] {2, 3, 6, 7, 73, 75, 76}),
							new Module.Rule(4, new int[] {2, 3, 65, 73, 74, 76})
					), Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__val_boiler_operation_type_off),
							new EnumValue.Item(1, "1", R.string.devices__val_boiler_operation_type_room_regulator),
							new EnumValue.Item(2, "2", R.string.devices__val_boiler_operation_type_equiterm_regulator),
							new EnumValue.Item(3, "3", R.string.devices__val_boiler_operation_type_constant_water_temperature),
							new EnumValue.Item(4, "4", R.string.devices__val_boiler_operation_type_hot_water)
					), null),
					new Module(device, "1", 0x01, 12, R.string.devices__zone_1, R.string.devices__mod_boiler_operation_mode, true, Arrays.asList(
							new Module.Rule(0, new int[] {6, 7, 73, 74, 75, 76}),
							new Module.Rule(2, new int[] {6, 7, 73, 74, 75, 76})
					), Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__val_boiler_operation_mode_automatic),
							new EnumValue.Item(1, "1", R.string.devices__val_boiler_operation_mode_manual),
							new EnumValue.Item(2, "2", R.string.devices__val_boiler_operation_mode_vacation)
					), null),
					new Module(device, "2", 0x02, 2, R.string.devices__zone_1, R.string.devices__mod_requested_room_temperature, false, null, new BaseValue.Constraints(0.0, 160.0, 1.0), null),
					new Module(device, "3", 0x02, 1, R.string.devices__zone_1, R.string.devices__mod_current_room_temperature, false, null, new BaseValue.Constraints(-20.0, 40.0, 1.0), null),
					new Module(device, "65", 0x02, 5, R.string.devices__zone_1, R.string.devices__mod_current_requested_water_temperature, false, null, new BaseValue.Constraints(10.0, 110.0, 1.0), null),
					new Module(device, "5", 0x02, 4, R.string.devices__zone_1, R.string.devices__mod_current_water_temperature, false, null, new BaseValue.Constraints(20.0, 120.0, 1.0), null),
					new Module(device, "6", 0x02, 9, R.string.devices__zone_1, R.string.devices__mod_minimal_water_temperature, true, null, new BaseValue.Constraints(20.0, 50.0, 1.0), null),
					new Module(device, "7", 0x02, 10, R.string.devices__zone_1, R.string.devices__mod_maximal_water_temperature, true, null, new BaseValue.Constraints(40.0, 90.0, 1.0), null),
					new Module(device, "11", 0x01, 13, R.string.devices__zone_1, R.string.devices__frost_protection, true, Arrays.asList(
							new Module.Rule(0, new int[] {12, 13})
					), Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__val_frost_protection_off),
							new EnumValue.Item(1, "1", R.string.devices__val_frost_protection_on)
					), null),
					new Module(device, "12", 0x02, 14, R.string.devices__zone_1, R.string.devices__frost_protection_water_temperature, true, null, new BaseValue.Constraints(25.0, 50.0, 1.0), null),
					new Module(device, "13", 0x02, 15, R.string.devices__zone_1, R.string.devices__frost_protection_threshold, true, null, new BaseValue.Constraints(0.0, 15.0, 1.0), null),
					new Module(device, "73", 0x02, 3, R.string.devices__zone_1, R.string.devices__manual_requested_room_temperature, true, null, new BaseValue.Constraints(0.0, 160.0, 1.0), null),
					new Module(device, "74", 0x02, 6, R.string.devices__zone_1, R.string.devices__manual_requested_water_temperature, true, null, new BaseValue.Constraints(20.0, 90.0, 1.0), null),
					new Module(device, "75", 0x02, 7, R.string.devices__zone_1, R.string.devices__manual_requested_tuv_temperature, true, null, new BaseValue.Constraints(20.0, 90.0, 1.0), null),
					new Module(device, "76", 0x02, 8, R.string.devices__zone_1, R.string.devices__manual_offset_ekviterm_curve, true, null, new BaseValue.Constraints(0.0, 60.0, 1.0), null),
					new Module(device, "14", 0x01, 61, R.string.devices__zone_2, R.string.devices__mod_boiler_operation_type, true, Arrays.asList(
							new Module.Rule(0, new int[] {15, 16, 17, 19, 20, 21, 25, 26, 27, 66, 77, 78, 79, 80}),
							new Module.Rule(1, new int[] {20, 21, 78, 79}),
							new Module.Rule(2, new int[] {16, 20, 21, 77, 78, 79}),
							new Module.Rule(3, new int[] {16, 17, 20, 21, 77, 79, 80}),
							new Module.Rule(4, new int[] {16, 17, 66, 77, 78, 80})
					), Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__val_boiler_operation_type_off),
							new EnumValue.Item(1, "1", R.string.devices__val_boiler_operation_type_room_regulator),
							new EnumValue.Item(2, "2", R.string.devices__val_boiler_operation_type_equiterm_regulator),
							new EnumValue.Item(3, "3", R.string.devices__val_boiler_operation_type_constant_water_temperature),
							new EnumValue.Item(4, "4", R.string.devices__val_boiler_operation_type_hot_water)
					), null),
					new Module(device, "15", 0x01, 62, R.string.devices__zone_2, R.string.devices__mod_boiler_operation_mode, true, Arrays.asList(
							new Module.Rule(0, new int[] {20, 21, 77, 78, 79, 80}),
							new Module.Rule(2, new int[] {20, 21, 77, 78, 79, 80})
					), Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__val_boiler_operation_mode_automatic),
							new EnumValue.Item(1, "1", R.string.devices__val_boiler_operation_mode_manual),
							new EnumValue.Item(2, "2", R.string.devices__val_boiler_operation_mode_vacation)
					), null),
					new Module(device, "16", 0x02, 52, R.string.devices__zone_2, R.string.devices__mod_requested_room_temperature, false, null, new BaseValue.Constraints(0.0, 160.0, 1.0), null),
					new Module(device, "17", 0x02, 51, R.string.devices__zone_2, R.string.devices__mod_current_room_temperature, false, null, new BaseValue.Constraints(-20.0, 40.0, 1.0), null),
					new Module(device, "66", 0x02, 55, R.string.devices__zone_2, R.string.devices__mod_current_requested_water_temperature, false, null, new BaseValue.Constraints(10.0, 110.0, 1.0), null),
					new Module(device, "19", 0x02, 54, R.string.devices__zone_2, R.string.devices__mod_current_water_temperature, false, null, new BaseValue.Constraints(20.0, 120.0, 1.0), null),
					new Module(device, "20", 0x02, 59, R.string.devices__zone_2, R.string.devices__mod_minimal_water_temperature, true, null, new BaseValue.Constraints(20.0, 50.0, 1.0), null),
					new Module(device, "21", 0x02, 60, R.string.devices__zone_2, R.string.devices__mod_maximal_water_temperature, true, null, new BaseValue.Constraints(40.0, 90.0, 1.0), null),
					new Module(device, "25", 0x01, 63, R.string.devices__zone_2, R.string.devices__frost_protection, true, Arrays.asList(
							new Module.Rule(0, new int[] {26, 27})
					), Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__val_frost_protection_off),
							new EnumValue.Item(1, "1", R.string.devices__val_frost_protection_on)
					), null),
					new Module(device, "26", 0x02, 64, R.string.devices__zone_2, R.string.devices__frost_protection_water_temperature, true, null, new BaseValue.Constraints(25.0, 50.0, 1.0), null),
					new Module(device, "27", 0x02, 65, R.string.devices__zone_2, R.string.devices__frost_protection_threshold, true, null, new BaseValue.Constraints(0.0, 15.0, 1.0), null),
					new Module(device, "77", 0x02, 53, R.string.devices__zone_2, R.string.devices__manual_requested_room_temperature, true, null, new BaseValue.Constraints(0.0, 160.0, 1.0), null),
					new Module(device, "78", 0x02, 56, R.string.devices__zone_2, R.string.devices__manual_requested_water_temperature, true, null, new BaseValue.Constraints(20.0, 90.0, 1.0), null),
					new Module(device, "79", 0x02, 57, R.string.devices__zone_2, R.string.devices__manual_requested_tuv_temperature, true, null, new BaseValue.Constraints(20.0, 90.0, 1.0), null),
					new Module(device, "80", 0x02, 58, R.string.devices__zone_2, R.string.devices__manual_offset_ekviterm_curve, true, null, new BaseValue.Constraints(0.0, 60.0, 1.0), null),
					new Module(device, "28", 0x01, 111, R.string.devices__zone_3, R.string.devices__mod_boiler_operation_type, true, Arrays.asList(
							new Module.Rule(0, new int[] {29, 30, 31, 33, 34, 35, 39, 40, 41, 67, 81, 82, 83, 84}),
							new Module.Rule(1, new int[] {34, 35, 82, 83}),
							new Module.Rule(2, new int[] {30, 34, 35, 81, 82, 83}),
							new Module.Rule(3, new int[] {30, 31, 34, 35, 81, 83, 84}),
							new Module.Rule(4, new int[] {30, 31, 67, 81, 82, 84})
					), Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__val_boiler_operation_type_off),
							new EnumValue.Item(1, "1", R.string.devices__val_boiler_operation_type_room_regulator),
							new EnumValue.Item(2, "2", R.string.devices__val_boiler_operation_type_equiterm_regulator),
							new EnumValue.Item(3, "3", R.string.devices__val_boiler_operation_type_constant_water_temperature),
							new EnumValue.Item(4, "4", R.string.devices__val_boiler_operation_type_hot_water)
					), null),
					new Module(device, "29", 0x01, 112, R.string.devices__zone_3, R.string.devices__mod_boiler_operation_mode, true, Arrays.asList(
							new Module.Rule(0, new int[] {34, 35, 81, 82, 83, 84}),
							new Module.Rule(2, new int[] {34, 35, 81, 82, 83, 84})
					), Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__val_boiler_operation_mode_automatic),
							new EnumValue.Item(1, "1", R.string.devices__val_boiler_operation_mode_manual),
							new EnumValue.Item(2, "2", R.string.devices__val_boiler_operation_mode_vacation)
					), null),
					new Module(device, "30", 0x02, 102, R.string.devices__zone_3, R.string.devices__mod_requested_room_temperature, false, null, new BaseValue.Constraints(0.0, 160.0, 1.0), null),
					new Module(device, "31", 0x02, 101, R.string.devices__zone_3, R.string.devices__mod_current_room_temperature, false, null, new BaseValue.Constraints(-20.0, 40.0, 1.0), null),
					new Module(device, "67", 0x02, 105, R.string.devices__zone_3, R.string.devices__mod_current_requested_water_temperature, false, null, new BaseValue.Constraints(10.0, 110.0, 1.0), null),
					new Module(device, "33", 0x02, 104, R.string.devices__zone_3, R.string.devices__mod_current_water_temperature, false, null, new BaseValue.Constraints(20.0, 120.0, 1.0), null),
					new Module(device, "34", 0x02, 109, R.string.devices__zone_3, R.string.devices__mod_minimal_water_temperature, true, null, new BaseValue.Constraints(20.0, 50.0, 1.0), null),
					new Module(device, "35", 0x02, 110, R.string.devices__zone_3, R.string.devices__mod_maximal_water_temperature, true, null, new BaseValue.Constraints(40.0, 90.0, 1.0), null),
					new Module(device, "39", 0x01, 113, R.string.devices__zone_3, R.string.devices__frost_protection, true, Arrays.asList(
							new Module.Rule(0, new int[] {40, 41})
					), Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__val_frost_protection_off),
							new EnumValue.Item(1, "1", R.string.devices__val_frost_protection_on)
					), null),
					new Module(device, "40", 0x02, 114, R.string.devices__zone_3, R.string.devices__frost_protection_water_temperature, true, null, new BaseValue.Constraints(25.0, 50.0, 1.0), null),
					new Module(device, "41", 0x02, 115, R.string.devices__zone_3, R.string.devices__frost_protection_threshold, true, null, new BaseValue.Constraints(0.0, 15.0, 1.0), null),
					new Module(device, "81", 0x02, 103, R.string.devices__zone_3, R.string.devices__manual_requested_room_temperature, true, null, new BaseValue.Constraints(0.0, 160.0, 1.0), null),
					new Module(device, "82", 0x02, 106, R.string.devices__zone_3, R.string.devices__manual_requested_water_temperature, true, null, new BaseValue.Constraints(20.0, 90.0, 1.0), null),
					new Module(device, "83", 0x02, 107, R.string.devices__zone_3, R.string.devices__manual_requested_tuv_temperature, true, null, new BaseValue.Constraints(20.0, 90.0, 1.0), null),
					new Module(device, "84", 0x02, 108, R.string.devices__zone_3, R.string.devices__manual_offset_ekviterm_curve, true, null, new BaseValue.Constraints(0.0, 60.0, 1.0), null),
					new Module(device, "42", 0x01, 161, R.string.devices__zone_4, R.string.devices__mod_boiler_operation_type, true, Arrays.asList(
							new Module.Rule(0, new int[] {43, 44, 45, 47, 48, 49, 53, 54, 55, 68, 85, 86, 87, 88}),
							new Module.Rule(1, new int[] {48, 49, 86, 87}),
							new Module.Rule(2, new int[] {44, 48, 49, 85, 86, 87}),
							new Module.Rule(3, new int[] {44, 45, 48, 49, 85, 87, 88}),
							new Module.Rule(4, new int[] {44, 45, 68, 85, 86, 88})
					), Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__val_boiler_operation_type_off),
							new EnumValue.Item(1, "1", R.string.devices__val_boiler_operation_type_room_regulator),
							new EnumValue.Item(2, "2", R.string.devices__val_boiler_operation_type_equiterm_regulator),
							new EnumValue.Item(3, "3", R.string.devices__val_boiler_operation_type_constant_water_temperature),
							new EnumValue.Item(4, "4", R.string.devices__val_boiler_operation_type_hot_water)
					), null),
					new Module(device, "43", 0x01, 162, R.string.devices__zone_4, R.string.devices__mod_boiler_operation_mode, true, Arrays.asList(
							new Module.Rule(0, new int[] {48, 49, 85, 86, 87, 88}),
							new Module.Rule(2, new int[] {48, 49, 85, 86, 87, 88})
					), Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__val_boiler_operation_mode_automatic),
							new EnumValue.Item(1, "1", R.string.devices__val_boiler_operation_mode_manual),
							new EnumValue.Item(2, "2", R.string.devices__val_boiler_operation_mode_vacation)
					), null),
					new Module(device, "44", 0x02, 152, R.string.devices__zone_4, R.string.devices__mod_requested_room_temperature, false, null, new BaseValue.Constraints(0.0, 160.0, 1.0), null),
					new Module(device, "45", 0x02, 151, R.string.devices__zone_4, R.string.devices__mod_current_room_temperature, false, null, new BaseValue.Constraints(-20.0, 40.0, 1.0), null),
					new Module(device, "68", 0x02, 155, R.string.devices__zone_4, R.string.devices__mod_current_requested_water_temperature, false, null, new BaseValue.Constraints(10.0, 110.0, 1.0), null),
					new Module(device, "47", 0x02, 154, R.string.devices__zone_4, R.string.devices__mod_current_water_temperature, false, null, new BaseValue.Constraints(20.0, 120.0, 1.0), null),
					new Module(device, "48", 0x02, 159, R.string.devices__zone_4, R.string.devices__mod_minimal_water_temperature, true, null, new BaseValue.Constraints(20.0, 50.0, 1.0), null),
					new Module(device, "49", 0x02, 160, R.string.devices__zone_4, R.string.devices__mod_maximal_water_temperature, true, null, new BaseValue.Constraints(40.0, 90.0, 1.0), null),
					new Module(device, "53", 0x01, 163, R.string.devices__zone_4, R.string.devices__frost_protection, true, Arrays.asList(
							new Module.Rule(0, new int[] {54, 55})
					), Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__val_frost_protection_off),
							new EnumValue.Item(1, "1", R.string.devices__val_frost_protection_on)
					), null),
					new Module(device, "54", 0x02, 164, R.string.devices__zone_4, R.string.devices__frost_protection_water_temperature, true, null, new BaseValue.Constraints(25.0, 50.0, 1.0), null),
					new Module(device, "55", 0x02, 165, R.string.devices__zone_4, R.string.devices__frost_protection_threshold, true, null, new BaseValue.Constraints(0.0, 15.0, 1.0), null),
					new Module(device, "85", 0x02, 153, R.string.devices__zone_4, R.string.devices__manual_requested_room_temperature, true, null, new BaseValue.Constraints(0.0, 160.0, 1.0), null),
					new Module(device, "86", 0x02, 156, R.string.devices__zone_4, R.string.devices__manual_requested_water_temperature, true, null, new BaseValue.Constraints(20.0, 90.0, 1.0), null),
					new Module(device, "87", 0x02, 157, R.string.devices__zone_4, R.string.devices__manual_requested_tuv_temperature, true, null, new BaseValue.Constraints(20.0, 90.0, 1.0), null),
					new Module(device, "88", 0x02, 158, R.string.devices__zone_4, R.string.devices__manual_offset_ekviterm_curve, true, null, new BaseValue.Constraints(0.0, 60.0, 1.0), null),
					new Module(device, "56", 0x01, 206, R.string.devices__boiler, R.string.devices__mod_boiler_status, false, null, Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__val_boiler_status_undefined),
							new EnumValue.Item(1, "1", R.string.devices__val_boiler_status_heating),
							new EnumValue.Item(2, "2", R.string.devices__val_boiler_status_hot_water),
							new EnumValue.Item(3, "3", R.string.devices__val_boiler_status_failure),
							new EnumValue.Item(4, "4", R.string.devices__val_boiler_status_shutdown)
					), null),
					new Module(device, "57", 0x01, 205, R.string.devices__boiler, R.string.devices__mod_boiler_mode, false, null, Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__val_boiler_mode_undefined),
							new EnumValue.Item(1, "1", R.string.devices__val_boiler_mode_on),
							new EnumValue.Item(2, "2", R.string.devices__val_boiler_mode_off)
					), null),
					new Module(device, "58", 0x02, -10, R.string.devices__boiler, R.string.devices__mod_current_water_temperature, false, null, new BaseValue.Constraints(20.0, 120.0, 1.0), null),
					new Module(device, "59", 0x02, -9, R.string.devices__boiler, R.string.devices__mod_current_outside_temperature, false, null, new BaseValue.Constraints(-50.0, 60.0, 1.0), null),
					new Module(device, "60", 0x02, -8, R.string.devices__boiler, R.string.devices__mod_average_outside_temperature, false, null, new BaseValue.Constraints(-40.0, 40.0, 1.0), null),
					new Module(device, "61", 0x03, -7, R.string.devices__boiler, R.string.devices__mod_current_boiler_performance, false, null, new BaseValue.Constraints(0.0, 100.0, 1.0), null),
					new Module(device, "62", 0x04, -6, R.string.devices__boiler, R.string.devices__mod_current_boiler_pressure, false, null, new BaseValue.Constraints(0.0, 10.0, 0.02), null),
					new Module(device, "63", 0x0B, -5, R.string.devices__boiler, R.string.devices__mod_current_boiler_error, false, null, null)
			);
		}
	},
	TYPE_7("7", "Conrad FS20 Wireless switch", R.string.devices__dev_fs20_switch, R.string.devices__manufacturer_conrad) {
		@Override
		public List<Module> createModules(Device device) {
			return Arrays.asList(
					new Module(device, "0", 0x01, null, null, R.string.devices__fs20_switch, true, null, Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__fs20_switch_state_off),
							new EnumValue.Item(1, "1", R.string.devices__fs20_switch_state_on)
					), null)
			);
		}
	},
	TYPE_8("8", "Jablotron AC-88 Wireless mains outlet", R.string.devices__dev_ac_88_mains_outlet, R.string.devices__manufacturer_jablotron) {
		@Override
		public List<Module> createModules(Device device) {
			return Arrays.asList(
					new Module(device, "0", 0x01, null, null, R.string.devices__ac_88_mains_outlet, true, null, Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__ac_88_mains_outlet_state_off),
							new EnumValue.Item(1, "1", R.string.devices__ac_88_mains_outlet_state_on)
					), null)
			);
		}
	},
	TYPE_9("9", "Jablotron JA-80L Wireless internal siren", R.string.devices__dev_ja_80l_internal_siren, R.string.devices__manufacturer_jablotron) {
		@Override
		public List<Module> createModules(Device device) {
			return Arrays.asList(
					new Module(device, "0", 0x01, null, null, R.string.devices__ja_80l_internal_siren, true, null, Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__ja_80l_internal_siren_state_off),
							new EnumValue.Item(1, "1", R.string.devices__ja_80l_internal_siren_state_on)
					), null)
			);
		}
	},
	TYPE_10("10", "Jablotron TP-82N Wireless thermostat", R.string.devices__dev_tp_82n_thermostat, R.string.devices__manufacturer_jablotron) {
		@Override
		public List<Module> createModules(Device device) {
			return Arrays.asList(
					new Module(device, "0", 0x02, null, null, R.string.devices__mod_current_room_temperature, false, null, null),
					new Module(device, "1", 0x02, null, null, R.string.devices__mod_requested_room_temperature, false, null, null),
					new Module(device, "2", 0x08, null, null, null, false, null, null)
			);
		}
	},
	TYPE_11("11", "Jablotron JA-83M Wireless magnetic door contact", R.string.devices__dev_ja_83m_magnetic_door_contact, R.string.devices__manufacturer_jablotron) {
		@Override
		public List<Module> createModules(Device device) {
			return Arrays.asList(
					new Module(device, "0", 0x01, null, null, R.string.devices__ja_83m_magnetic_door_contact, false, null, Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__ja_83m_magnetic_door_contact_state_unknown),
							new EnumValue.Item(1, "1", R.string.devices__ja_83m_magnetic_door_contact_state_opened),
							new EnumValue.Item(2, "2", R.string.devices__ja_83m_magnetic_door_contact_state_closed)
					), null),
					new Module(device, "1", 0x08, null, null, null, false, null, null)
			);
		}
	},
	TYPE_12("12", "HomeMatic wireless switch with power meter HM-ES-PMSw1-PI", R.string.devices__dev_hm_switch, R.string.devices__manufacturer_eq3) {
		@Override
		public List<Module> createModules(Device device) {
			return Arrays.asList(
					new Module(device, "0", 0x01, null, null, R.string.devices__hm_switch, true, null, Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__hm_switch_state_off),
							new EnumValue.Item(1, "1", R.string.devices__hm_switch_state_on)
					), null),
					new Module(device, "1", 0x0E, null, null, R.string.devices__type_voltage, false, null, null),
					new Module(device, "2", 0x0F, null, null, R.string.devices__type_current, false, null, null),
					new Module(device, "3", 0x10, null, null, R.string.devices__type_frequency, false, null, null),
					new Module(device, "4", 0x11, null, null, R.string.devices__type_power, false, null, null),
					new Module(device, "5", 0x12, null, null, R.string.devices__type_powermeter, false, null, null),
					new Module(device, "6", 0x09, null, null, null, false, null, null)
			);
		}
	},
	TYPE_13("13", "HomeMatic wireless magnetic door contact HM-SEC-SC-2", R.string.devices__dev_hm_magnetic_door_contact, R.string.devices__manufacturer_eq3) {
		@Override
		public List<Module> createModules(Device device) {
			return Arrays.asList(
					new Module(device, "0", 0x01, null, null, R.string.devices__hm_magnetic_door_contact, false, null, Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__hm_magnetic_door_contact_state_unknown),
							new EnumValue.Item(1, "1", R.string.devices__hm_magnetic_door_contact_state_opened),
							new EnumValue.Item(2, "2", R.string.devices__hm_magnetic_door_contact_state_closed)
					), null),
					new Module(device, "1", 0x09, null, null, null, false, null, null),
					new Module(device, "2", 0x08, null, null, null, false, null, null)
			);
		}
	},
	TYPE_14("14", "HomeMatic wireless radiator thermostat HM-CC-RT-DN", R.string.devices__dev_hm_radiator_thermostat, R.string.devices__manufacturer_eq3) {
		@Override
		public List<Module> createModules(Device device) {
			return Arrays.asList(
					new Module(device, "0", 0x02, null, null, R.string.devices__manual_requested_room_temperature, true, null, new BaseValue.Constraints(5.0, 30.0, 1.0), null),
					new Module(device, "1", 0x02, null, null, R.string.devices__mod_current_room_temperature, false, null, new BaseValue.Constraints(-20.0, 40.0, 1.0), null),
					new Module(device, "2", 0x13, null, null, R.string.devices__hm_valve_position, false, null, new BaseValue.Constraints(0.0, 100.0, 1.0), null),
					new Module(device, "3", 0x09, null, null, null, false, null, null),
					new Module(device, "4", 0x08, null, null, null, false, null, null)
			);
		}
	},
	TYPE_15("15", "Wireless radiator thermostat eQ-3 MAX!", R.string.devices__dev_max_radiator_thermostat, R.string.devices__manufacturer_eq3) {
		@Override
		public List<Module> createModules(Device device) {
			return Arrays.asList(
					new Module(device, "0", 0x02, null, null, R.string.devices__manual_requested_room_temperature, true, null, new BaseValue.Constraints(5.0, 30.0, 1.0), null),
					new Module(device, "1", 0x02, null, null, R.string.devices__mod_current_room_temperature, false, null, new BaseValue.Constraints(-20.0, 40.0, 1.0), null),
					new Module(device, "2", 0x13, null, null, R.string.devices__max_valve_position, false, null, new BaseValue.Constraints(0.0, 100.0, 1.0), null),
					new Module(device, "3", 0x09, null, null, null, false, null, null),
					new Module(device, "4", 0x08, null, null, null, false, null, null)
			);
		}
	},
	TYPE_16("16", "Z-Wave Philio wireless 3in1 sensor PST02-C", R.string.devices__dev_zw_pst02c, R.string.devices__manufacturer_philio) {
		@Override
		public List<Module> createModules(Device device) {
			return Arrays.asList(
					new Module(device, "0", 0x01, null, null, R.string.devices__hm_magnetic_door_contact, false, null, Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__hm_magnetic_door_contact_state_unknown),
							new EnumValue.Item(1, "1", R.string.devices__hm_magnetic_door_contact_state_opened),
							new EnumValue.Item(2, "2", R.string.devices__hm_magnetic_door_contact_state_closed)
					), null),
					new Module(device, "1", 0x02, null, null, R.string.devices__type_temperature, false, null, null),
					new Module(device, "2", 0x05, null, null, R.string.devices__type_light, false, null, null),
					new Module(device, "3", 0x08, null, null, null, false, null, null)
			);
		}
	},
	TYPE_17("17", "Z-Wave Popp Wireless switch", R.string.devices__dev_popp_switch, R.string.devices__manufacturer_popp) {
		@Override
		public List<Module> createModules(Device device) {
			return Arrays.asList(
					new Module(device, "0", 0x01, null, null, R.string.devices__fs20_switch, true, null, Arrays.asList(
							new EnumValue.Item(0, "0", R.string.devices__fs20_switch_state_off),
							new EnumValue.Item(1, "1", R.string.devices__fs20_switch_state_on)
					), null)
			);
		}
	};

	/** Version from specification of this devices list */
	public static final String DEVICES_VERSION = "1";

	/** Generation time (GMT) of this devices list */
	public static final long DEVICES_DATE = 1458250488039l;

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
