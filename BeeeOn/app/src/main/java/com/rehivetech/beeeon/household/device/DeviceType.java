package com.rehivetech.beeeon.household.device;

import android.support.annotation.StringRes;

import com.rehivetech.beeeon.IIdentifier;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.device.values.BaseValue;
import com.rehivetech.beeeon.household.device.values.EnumValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class DeviceType implements IIdentifier {
	private static DeviceType UNKNOWN = new DeviceType(
		"", "???", R.string.device_type_unknown_device, R.string.device_type_unknown_manufacturer, new ArrayList<Module.Factory>());

	private static DeviceType[] TYPES = {
		UNKNOWN,
	/** BEGIN OF GENERATED CONTENT **/
	new DeviceType("0", "Pressure Sensor", R.string.devices__dev_0_name, R.string.devices__dev_0_vendor, Arrays.asList(
		new Module.Factory("0", 15, null, null, R.string.devices__mod_0_0, false, null, new BaseValue.Constraints(800d, 1100d, 1d), null)
	)),
	new DeviceType("1", "Temperature and Humidity Sensor", R.string.devices__dev_1_name, R.string.devices__dev_1_vendor, Arrays.asList(
		new Module.Factory("0", 2, null, null, R.string.devices__mod_1_0, false, null, new BaseValue.Constraints(0d, 100d, 1d), null),
		new Module.Factory("1", 19, null, null, R.string.devices__mod_1_1, false, null, new BaseValue.Constraints(-273.15d, 200d, 0.01d), null),
		new Module.Factory("2", 19, null, null, R.string.devices__mod_1_2, false, null, new BaseValue.Constraints(-273.15d, 200d, 0.01d), null),
		new Module.Factory("3", 8, null, null, R.string.devices__mod_1_3, false, null, new BaseValue.Constraints(0d, 100d, 1d), null)
	)),
	new DeviceType("10", "Remote Switch", R.string.devices__dev_10_name, R.string.devices__dev_10_vendor, Arrays.asList(
		new Module.Factory("0", 13, null, null, R.string.devices__mod_10_0, true, null, Arrays.asList(
			new EnumValue.Item(0, "0", R.string.devices__type_13_val_0),
			new EnumValue.Item(1, "1", R.string.devices__type_13_val_1)
		), null)
	)),
	new DeviceType("11", "Bulb", R.string.devices__dev_11_name, R.string.devices__dev_11_vendor, Arrays.asList(
		new Module.Factory("0", 13, null, null, R.string.devices__mod_11_0, true, null, Arrays.asList(
			new EnumValue.Item(0, "0", R.string.devices__type_13_val_0),
			new EnumValue.Item(1, "1", R.string.devices__type_13_val_1)
		), null),
		new Module.Factory("1", 4, null, null, R.string.devices__mod_11_1, true, null, new BaseValue.Constraints(0d, 100d, 1d),
		null)
	)),
	new DeviceType("20", "VPT Boiler Control", R.string.devices__dev_20_name, R.string.devices__dev_20_vendor, Arrays.asList(
		new Module.Factory("0", 6, null, null, R.string.devices__mod_20_0, false, null, Arrays.asList(
			new EnumValue.Item(0, "0", R.string.devices__enum_MOD_BOILER_STATUS_val_0),
			new EnumValue.Item(1, "1", R.string.devices__enum_MOD_BOILER_STATUS_val_1),
			new EnumValue.Item(2, "2", R.string.devices__enum_MOD_BOILER_STATUS_val_2),
			new EnumValue.Item(3, "3", R.string.devices__enum_MOD_BOILER_STATUS_val_3),
			new EnumValue.Item(4, "4", R.string.devices__enum_MOD_BOILER_STATUS_val_4)
		), null),
		new Module.Factory("1", 6, null, null, R.string.devices__mod_20_1, false, null, Arrays.asList(
			new EnumValue.Item(0, "0", R.string.devices__enum_MOD_BOILER_MODE_val_0),
			new EnumValue.Item(1, "1", R.string.devices__enum_MOD_BOILER_MODE_val_1),
			new EnumValue.Item(2, "2", R.string.devices__enum_MOD_BOILER_MODE_val_2)
		), null),
		new Module.Factory("2", 19, null, null, R.string.devices__mod_20_2, false, null, new BaseValue.Constraints(-273.15d, 200d, 0.01d), null),
		new Module.Factory("3", 19, null, null, R.string.devices__mod_20_3, false, null, new BaseValue.Constraints(-273.15d, 200d, 0.01d), null),
		new Module.Factory("4", 19, null, null, R.string.devices__mod_20_4, false, null, new BaseValue.Constraints(-273.15d, 200d, 0.01d), null),
		new Module.Factory("5", 14, null, null, R.string.devices__mod_20_5, false, null, new BaseValue.Constraints(0d, 100d, 1d), null),
		new Module.Factory("6", 15, null, null, R.string.devices__mod_20_6, false, null, new BaseValue.Constraints(800d, 1100d, 1d), null),
		new Module.Factory("7", 3, null, null, R.string.devices__mod_20_7, false, null, null),
		new Module.Factory("8", 3, null, null, R.string.devices__mod_20_8, false, null, null)
	)),
	new DeviceType("21", "VPT Zone Control", R.string.devices__dev_21_name, R.string.devices__dev_21_vendor, Arrays.asList(
		new Module.Factory("0", 6, null, null, R.string.devices__mod_21_0, false, null, Arrays.asList(
			new EnumValue.Item(0, "0", R.string.devices__enum_MOD_BOILER_OPERATION_TYPE_val_0),
			new EnumValue.Item(1, "1", R.string.devices__enum_MOD_BOILER_OPERATION_TYPE_val_1),
			new EnumValue.Item(2, "2", R.string.devices__enum_MOD_BOILER_OPERATION_TYPE_val_2),
			new EnumValue.Item(3, "3", R.string.devices__enum_MOD_BOILER_OPERATION_TYPE_val_3),
			new EnumValue.Item(4, "4", R.string.devices__enum_MOD_BOILER_OPERATION_TYPE_val_4)
		), null),
		new Module.Factory("1", 6, null, null, R.string.devices__mod_21_1, false, null, Arrays.asList(
			new EnumValue.Item(0, "0", R.string.devices__enum_MOD_BOILER_OPERATION_MODE_val_0),
			new EnumValue.Item(1, "1", R.string.devices__enum_MOD_BOILER_OPERATION_MODE_val_1),
			new EnumValue.Item(2, "2", R.string.devices__enum_MOD_BOILER_OPERATION_MODE_val_2)
		), null),
		new Module.Factory("2", 19, null, null, R.string.devices__mod_21_2, false, null, new BaseValue.Constraints(-273.15d, 200d, 0.01d), null),
		new Module.Factory("3", 19, null, null, R.string.devices__mod_21_3, false, null, new BaseValue.Constraints(-273.15d, 200d, 0.01d), null),
		new Module.Factory("4", 19, null, null, R.string.devices__mod_21_4, false, null, new BaseValue.Constraints(-273.15d, 200d, 0.01d), null),
		new Module.Factory("5", 19, null, null, R.string.devices__mod_21_5, false, null, new BaseValue.Constraints(-273.15d, 200d, 0.01d), null),
		new Module.Factory("6", 19, null, null, R.string.devices__mod_21_6, true, null, new BaseValue.Constraints(-273.15d, 200d, 0.01d), null),
		new Module.Factory("7", 19, null, null, R.string.devices__mod_21_7, true, null, new BaseValue.Constraints(-273.15d, 200d, 0.01d), null),
		new Module.Factory("8", 19, null, null, R.string.devices__mod_21_8, true, null, new BaseValue.Constraints(-273.15d, 200d, 0.01d), null)
	)),
	new DeviceType("30", "Unknown Device", R.string.devices__dev_30_name, R.string.devices__dev_30_vendor, Arrays.asList(
		new Module.Factory("0", 1, null, null, R.string.devices__mod_30_0, false, null, Arrays.asList(
			new EnumValue.Item(0, "0", R.string.devices__type_1_val_0),
			new EnumValue.Item(1, "1", R.string.devices__type_1_val_1)
		), null)
	)),
	new DeviceType("40", "AC-88 Wireless Mains Outlet", R.string.devices__dev_40_name, R.string.devices__dev_40_vendor, Arrays.asList(
		new Module.Factory("0", 13, null, null, R.string.devices__mod_40_0, false, null, Arrays.asList(
			new EnumValue.Item(0, "0", R.string.devices__type_13_val_0),
			new EnumValue.Item(1, "1", R.string.devices__type_13_val_1)
		), null)
	)),
	new DeviceType("41", "JA-82SH Wireless Shake Sensor", R.string.devices__dev_41_name, R.string.devices__dev_41_vendor, Arrays.asList(
		new Module.Factory("0", 18, null, null, R.string.devices__mod_41_0, false, null, Arrays.asList(
			new EnumValue.Item(0, "0", R.string.devices__type_18_val_0),
			new EnumValue.Item(1, "1", R.string.devices__type_18_val_1)
		), null),
		new Module.Factory("1", 17, null, null, R.string.devices__mod_41_1, false, null, Arrays.asList(
			new EnumValue.Item(0, "0", R.string.devices__type_17_val_0),
			new EnumValue.Item(1, "1", R.string.devices__type_17_val_1)
		), null),
		new Module.Factory("2", 2, null, null, R.string.devices__mod_41_2, false, null, new BaseValue.Constraints(0d, 100d, 1d), null)
	)),
	new DeviceType("42", "JA-85ST Wireless Fire Sensor", R.string.devices__dev_42_name, R.string.devices__dev_42_vendor, Arrays.asList(
		new Module.Factory("0", 7, null, null, R.string.devices__mod_42_0, false, null, Arrays.asList(
			new EnumValue.Item(0, "0", R.string.devices__type_7_val_0),
			new EnumValue.Item(1, "1", R.string.devices__type_7_val_1)
		), null),
		new Module.Factory("1", 17, null, null, R.string.devices__mod_42_1, false, null, Arrays.asList(
			new EnumValue.Item(0, "0", R.string.devices__type_17_val_0),
			new EnumValue.Item(1, "1", R.string.devices__type_17_val_1)
		), null),
		new Module.Factory("2", 2, null, null, R.string.devices__mod_42_2, false, null, new BaseValue.Constraints(0d, 100d, 1d), null)
	)),
	new DeviceType("43", "JA-81M Wireless Magnetic Door Contact", R.string.devices__dev_43_name, R.string.devices__dev_43_vendor, Arrays.asList(
		new Module.Factory("0", 12, null, null, R.string.devices__mod_43_0, false, null, Arrays.asList(
			new EnumValue.Item(0, "0", R.string.devices__type_12_val_0),
			new EnumValue.Item(1, "1", R.string.devices__type_12_val_1)
		), null),
		new Module.Factory("1", 17, null, null, R.string.devices__mod_43_1, false, null, Arrays.asList(
			new EnumValue.Item(0, "0", R.string.devices__type_17_val_0),
			new EnumValue.Item(1, "1", R.string.devices__type_17_val_1)
		), null),
		new Module.Factory("2", 2, null, null, R.string.devices__mod_43_2, false, null, new BaseValue.Constraints(0d, 100d, 1d), null)
	)),
	new DeviceType("44", "JA-83M Wireless Magnetic Door Contact", R.string.devices__dev_44_name, R.string.devices__dev_44_vendor, Arrays.asList(
		new Module.Factory("0", 12, null, null, R.string.devices__mod_44_0, false, null, Arrays.asList(
			new EnumValue.Item(0, "0", R.string.devices__type_12_val_0),
			new EnumValue.Item(1, "1", R.string.devices__type_12_val_1)
		), null),
		new Module.Factory("1", 17, null, null, R.string.devices__mod_44_1, false, null, Arrays.asList(
			new EnumValue.Item(0, "0", R.string.devices__type_17_val_0),
			new EnumValue.Item(1, "1", R.string.devices__type_17_val_1)
		), null),
		new Module.Factory("2", 2, null, null, R.string.devices__mod_44_2, false, null, new BaseValue.Constraints(0d, 100d, 1d), null)
	)),
	new DeviceType("45", "RC-86K Wireless Remote Control", R.string.devices__dev_45_name, R.string.devices__dev_45_vendor, Arrays.asList(
		new Module.Factory("0", 12, null, null, R.string.devices__mod_45_0, false, null, Arrays.asList(
			new EnumValue.Item(0, "0", R.string.devices__type_12_val_0),
			new EnumValue.Item(1, "1", R.string.devices__type_12_val_1)
		), null),
		new Module.Factory("1", 17, null, null, R.string.devices__mod_45_1, false, null, Arrays.asList(
			new EnumValue.Item(0, "0", R.string.devices__type_17_val_0),
			new EnumValue.Item(1, "1", R.string.devices__type_17_val_1)
		), null),
		new Module.Factory("2", 2, null, null, R.string.devices__mod_45_2, false, null, new BaseValue.Constraints(0d, 100d, 1d), null)
	)),
	new DeviceType("46", "TP-82N Wireless Thermostat", R.string.devices__dev_46_name, R.string.devices__dev_46_vendor, Arrays.asList(
		new Module.Factory("0", 19, null, null, R.string.devices__mod_46_0, true, null, new BaseValue.Constraints(-273.15d, 200d, 0.01d), null),
		new Module.Factory("1", 18, null, null, R.string.devices__mod_46_1, false, null, Arrays.asList(
			new EnumValue.Item(0, "0", R.string.devices__type_18_val_0),
			new EnumValue.Item(1, "1", R.string.devices__type_18_val_1)
		), null),
		new Module.Factory("2", 2, null, null, R.string.devices__mod_46_2, false, null, new BaseValue.Constraints(0d, 100d, 1d), null)
	)),
	new DeviceType("101", "Z-Wave Philio Wireless 3-in-1 Sensor PST02-C", R.string.devices__dev_101_name, R.string.devices__dev_101_vendor, Arrays.asList(
		new Module.Factory("0", 6, null, null, R.string.devices__mod_101_0, false, null, null),
		new Module.Factory("1", 19, null, null, R.string.devices__mod_101_1, false, null, new BaseValue.Constraints(-273.15d, 200d, 0.01d), null),
		new Module.Factory("2", 9, null, null, R.string.devices__mod_101_2, false, null, new BaseValue.Constraints(0d, 1000000d, 1d), null),
		new Module.Factory("3", 2, null, null, R.string.devices__mod_101_3, false, null, new BaseValue.Constraints(0d, 100d, 1d), null)
	))	
	};

	/** Version from specification of this devices list */
	public static final String DEVICES_VERSION = "2";

	/** Generation time (GMT) of this devices list */
	public static final long DEVICES_DATE = 1509025774000l;

	/** END OF GENERATED CONTENT **/


	private final String mTypeId;
	private final String mTypeName;
	private final int mNameRes;
	private final int mManufacturerRes;
	private final List<Module.Factory> mModuleFactory;

	DeviceType(String typeId, String typeName, @StringRes int nameRes, @StringRes int manufacturerRes, List<Module.Factory> moduleFactory) {
		mTypeId = typeId;
		mTypeName = typeName;
		mNameRes = nameRes;
		mManufacturerRes = manufacturerRes;
		mModuleFactory = moduleFactory;
	}

	public List<Module> createModules(Device device) {
		List<Module> modules = new ArrayList<Module>();

		for (Module.Factory factory : mModuleFactory)
			modules.add(factory.create(device));

		return modules;
	}

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

	public static DeviceType getUnknown() {
		return UNKNOWN;
	}

	public static DeviceType getById(String id) {
		for (DeviceType type : TYPES) {
			if (type.getId().equalsIgnoreCase(id))
				return type;
		}

		return UNKNOWN;
	}

	public static DeviceType getDemoType(int i) {
		return TYPES[1 + i];
	}

	public static DeviceType getRandomDemoType(Random rand) {
		// 1+ because we don't want unknown type, which is on beginning
		return TYPES[1 + rand.nextInt(TYPES.length - 1)];
	}
}
