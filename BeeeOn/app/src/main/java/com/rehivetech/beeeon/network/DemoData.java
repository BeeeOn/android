package com.rehivetech.beeeon.network;

import android.content.Context;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.household.watchdog.Watchdog;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by vico on 8.7.2015.
 */
public class DemoData {

	private static final String GATE_1_ID = "64206";
	private static final String GATE_2_ID = "65260";
	private static final String GATE_3_ID = "12345";

	private static final String GATE_1_LOCATION1 = "5555";
	private static final String GATE_1_LOCATION2 = "2222";
	private static final String GATE_1_LOCATION3 = "4444";
	private static final String GATE_1_LOCATION4 = "6666";
	private static final String GATE_2_LOCATION1 = "1111";
	private static final String GATE_2_LOCATION2 = "2";
	private static final String GATE_3_LOCATION1 = "1";

	public List<Gate> getGates(Context context) {

		List<Gate> gateList = new ArrayList<>();

		Gate demoGate1 = new Gate();
		String demoGateName1 = context.getString(R.string.gate_1_name);
		demoGate1.setId(GATE_1_ID);
		demoGate1.setName(demoGateName1);
		demoGate1.setRole(User.Role.Admin);
		demoGate1.setUtcOffset(-60);
		gateList.add(demoGate1);

		Gate demoGate2 = new Gate();
		String demoGateName2 = context.getString(R.string.gate_2_name);
		demoGate2.setName(demoGateName2);
		demoGate2.setId(GATE_2_ID);
		demoGate2.setRole(User.Role.Superuser);
		demoGate2.setUtcOffset(60);
		gateList.add(demoGate2);

		Gate demoGate3 = new Gate();
		String demoGateName3 = context.getString(R.string.gate_3_name);
		demoGate3.setId(GATE_3_ID);
		demoGate3.setName(demoGateName3);
		demoGate3.setRole(User.Role.Superuser);
		demoGate3.setUtcOffset(60);
		gateList.add(demoGate3);


		return gateList;
	}

	public List<Location> getLocation(Context context, String gateId) {

		List<Location> locationList = new ArrayList<>();
		switch (gateId) {
			case GATE_1_ID: {
				Location location1_livingRoom = new Location(GATE_1_LOCATION1, context.getString(R.string.loc_name_demo_living_room), GATE_1_ID, "5");
				Location location1_bedRoom = new Location(GATE_1_LOCATION2, context.getString(R.string.loc_name_demo_bedroom), GATE_1_ID, "2");
				Location location1_kitchen = new Location(GATE_1_LOCATION3, context.getString(R.string.loc_name_demo_kitchen), GATE_1_ID, "4");
				Location location1_wc = new Location(GATE_1_LOCATION4, context.getString(R.string.loc_name_demo_wc), GATE_1_ID, "6");

				locationList.add(location1_livingRoom);
				locationList.add(location1_bedRoom);
				locationList.add(location1_kitchen);
				locationList.add(location1_wc);
				break;
			}

			case GATE_2_ID: {
				Location location2_greenHouse = new Location(GATE_2_LOCATION1, context.getString(R.string.loc_name_demo_green_house), GATE_2_ID, "3");
				Location location2_sauna = new Location(GATE_2_LOCATION2, context.getString(R.string.loc_name_demo_sauna), GATE_2_ID, "1");

				locationList.add(location2_greenHouse);
				locationList.add(location2_sauna);
				break;
			}

			case GATE_3_ID: {
				Location location3 = new Location(GATE_3_LOCATION1, context.getString(R.string.loc_name_demo_room), GATE_3_ID, "5");

				locationList.add(location3);
				break;
			}

			default: {
				//do nothing
				break;
			}
		}


		return locationList;
	}

	public List<Device> getDevices(Context context, String gateId) {
		List<Device> deviceList = new ArrayList<>();

		int timeLastUpdate = 1377684610;
		DateTime timeData = new DateTime((long) (timeLastUpdate * 1000), DateTimeZone.UTC);

		switch (gateId) {

			case GATE_1_ID: {
				Device device1_WindowTemp = new Device();
				device1_WindowTemp.setGateId(gateId);
				device1_WindowTemp.setInitialized(true);
				device1_WindowTemp.setAddress("100:00:FF:000:FF0");
				device1_WindowTemp.setLocationId(GATE_1_LOCATION1);
				device1_WindowTemp.setRefresh(RefreshInterval.SEC_5);
				device1_WindowTemp.setBattery(100);
				device1_WindowTemp.setLastUpdate(DateTime.now());
				device1_WindowTemp.setInvolveTime(timeData);
				device1_WindowTemp.setNetworkQuality(52);


				Module module_WindowTemp = Module.createFromModuleTypeId("10");
				module_WindowTemp.setVisibility(true);
				module_WindowTemp.setName(context.getString(R.string.demo_name_module_window));
				module_WindowTemp.setValue("28");

				device1_WindowTemp.addModule(module_WindowTemp);
				deviceList.add(device1_WindowTemp);

				Device device1_Radiator = new Device();
				device1_Radiator.setGateId(gateId);
				device1_Radiator.setInitialized(true);
				device1_Radiator.setAddress("100:00:FF:000:FF1");
				device1_Radiator.setLocationId(GATE_1_LOCATION1);
				device1_Radiator.setRefresh(RefreshInterval.SEC_30);
				device1_Radiator.setBattery(100);
				device1_Radiator.setLastUpdate(timeData);
				device1_Radiator.setInvolveTime(timeData);
				device1_Radiator.setNetworkQuality(52);

				Module moduleRadiator = Module.createFromModuleTypeId("10");
				moduleRadiator.setVisibility(true);
				moduleRadiator.setName(context.getString(R.string.demo_name_module_radiator));
				moduleRadiator.setValue("30");

				device1_Radiator.addModule(moduleRadiator);
				deviceList.add(device1_Radiator);

				Device device1_WindowAct = new Device();
				device1_WindowAct.setGateId(gateId);
				device1_WindowAct.setInitialized(true);
				device1_WindowAct.setAddress("100:00:FF:000:FF2");
				device1_WindowAct.setLocationId(GATE_1_LOCATION1);
				device1_WindowAct.setRefresh(RefreshInterval.SEC_10);
				device1_WindowAct.setBattery(100);
				device1_WindowAct.setLastUpdate(timeData);
				device1_WindowAct.setInvolveTime(timeData);
				device1_WindowAct.setNetworkQuality(52);

				Module moduleWindowAct = Module.createFromModuleTypeId("3");
				moduleWindowAct.setVisibility(true);
				moduleWindowAct.setName(context.getString(R.string.demo_name_module_window_act));
				moduleWindowAct.setValue("CLOSED");

				device1_WindowAct.addModule(moduleWindowAct);
				deviceList.add(device1_WindowAct);

				Device device1_DoorAct = new Device();
				device1_DoorAct.setGateId(gateId);
				device1_DoorAct.setInitialized(true);
				device1_DoorAct.setAddress("100:00:FF:000:FF3");
				device1_DoorAct.setLocationId(GATE_1_LOCATION1);
				device1_DoorAct.setRefresh(RefreshInterval.MIN_1);
				device1_DoorAct.setBattery(100);
				device1_DoorAct.setLastUpdate(timeData);
				device1_DoorAct.setInvolveTime(timeData);
				device1_DoorAct.setNetworkQuality(52);

				Module moduleDoorAct = Module.createFromModuleTypeId("3");
				moduleDoorAct.setVisibility(true);
				moduleDoorAct.setName(context.getString(R.string.demo_name_module_door_act));
				moduleDoorAct.setValue("OPEN");

				device1_DoorAct.addModule(moduleDoorAct);
				deviceList.add(device1_DoorAct);

				Device device1_babyMonitorBed = new Device();
				device1_babyMonitorBed.setGateId(gateId);
				device1_babyMonitorBed.setInitialized(true);
				device1_babyMonitorBed.setAddress("100:00:FF:000:FF4");
				device1_babyMonitorBed.setLocationId(GATE_1_LOCATION2);
				device1_babyMonitorBed.setRefresh(RefreshInterval.MIN_5);
				device1_babyMonitorBed.setBattery(100);
				device1_babyMonitorBed.setLastUpdate(DateTime.now());
				device1_babyMonitorBed.setInvolveTime(timeData);
				device1_babyMonitorBed.setNetworkQuality(52);

				Module modulebabyMonitor = Module.createFromModuleTypeId("6");
				modulebabyMonitor.setVisibility(true);
				modulebabyMonitor.setName(context.getString(R.string.demo_name_module_baby_monitor));
				modulebabyMonitor.setValue("30");

				device1_babyMonitorBed.addModule(modulebabyMonitor);
				deviceList.add(device1_babyMonitorBed);

				Device device1_lampTable = new Device();
				device1_lampTable.setGateId(gateId);
				device1_lampTable.setInitialized(true);
				device1_lampTable.setAddress("100:00:FF:000:FF5");
				device1_lampTable.setLocationId(GATE_1_LOCATION2);
				device1_lampTable.setRefresh(RefreshInterval.MIN_5);
				device1_lampTable.setBattery(100);
				device1_lampTable.setLastUpdate(timeData);
				device1_lampTable.setInvolveTime(timeData);
				device1_lampTable.setNetworkQuality(52);

				Module modulelampTable = Module.createFromModuleTypeId("5");
				modulelampTable.setVisibility(true);
				modulelampTable.setName(context.getString(R.string.demo_name_module_lamp_table));
				modulelampTable.setValue("30");

				device1_lampTable.addModule(modulelampTable);
				deviceList.add(device1_lampTable);

				Device device1_ventilator = new Device();
				device1_ventilator.setGateId(gateId);
				device1_ventilator.setInitialized(true);
				device1_ventilator.setAddress("100:00:FF:000:FF7");
				device1_ventilator.setLocationId(GATE_1_LOCATION1);
				device1_ventilator.setRefresh(RefreshInterval.MIN_10);
				device1_ventilator.setBattery(100);
				device1_ventilator.setLastUpdate(timeData);
				device1_ventilator.setInvolveTime(timeData);
				device1_ventilator.setNetworkQuality(52);

				Module moduleVentilator = Module.createFromModuleTypeId("4");
				moduleVentilator.setVisibility(true);
				moduleVentilator.setName(context.getString(R.string.demo_name_module_ventilator));
				moduleVentilator.setValue("ON");

				device1_ventilator.addModule(moduleVentilator);
				deviceList.add(device1_ventilator);

				Device device1_pc = new Device();
				device1_pc.setGateId(gateId);
				device1_pc.setInitialized(true);
				device1_pc.setAddress("100:00:FF:000:FF8");
				device1_pc.setLocationId(GATE_1_LOCATION1);
				device1_pc.setRefresh(RefreshInterval.SEC_30);
				device1_pc.setBattery(100);
				device1_pc.setLastUpdate(timeData);
				device1_pc.setInvolveTime(timeData);
				device1_pc.setNetworkQuality(52);

				Module modulePc = Module.createFromModuleTypeId("4");
				modulePc.setVisibility(true);
				modulePc.setName(context.getString(R.string.demo_name_module_pc));
				modulePc.setValue("OFF");

				device1_pc.addModule(modulePc);
				deviceList.add(device1_pc);

				Device device1_lampTv = new Device();
				device1_lampTv.setGateId(gateId);
				device1_lampTv.setInitialized(true);
				device1_lampTv.setAddress("100:00:FF:000:FF9");
				device1_lampTv.setLocationId(GATE_1_LOCATION1);
				device1_lampTv.setRefresh(RefreshInterval.SEC_30);
				device1_lampTv.setBattery(100);
				device1_lampTv.setLastUpdate(timeData);
				device1_lampTv.setInvolveTime(timeData);
				device1_lampTv.setNetworkQuality(52);

				Module moduleLampTv = Module.createFromModuleTypeId("160");
				moduleLampTv.setVisibility(true);
				moduleLampTv.setName(context.getString(R.string.demo_name_module_lamp_tv));
				moduleLampTv.setValue("ON");

				device1_lampTv.addModule(moduleLampTv);
				deviceList.add(device1_lampTv);

				Device device1_tv = new Device();
				device1_tv.setGateId(gateId);
				device1_tv.setInitialized(true);
				device1_tv.setAddress("100:00:FF:000:FFA");
				device1_tv.setLocationId(GATE_1_LOCATION1);
				device1_tv.setRefresh(RefreshInterval.MIN_30);
				device1_tv.setBattery(100);
				device1_tv.setLastUpdate(timeData);
				device1_tv.setInvolveTime(timeData);
				device1_tv.setNetworkQuality(52);

				Module moduleTv = Module.createFromModuleTypeId("160");
				moduleTv.setVisibility(true);
				moduleTv.setName(context.getString(R.string.demo_name_module_tv));
				moduleTv.setValue("ON");

				device1_tv.addModule(moduleTv);
				deviceList.add(device1_tv);

				Device device1_cooker = new Device();
				device1_cooker.setGateId(gateId);
				device1_cooker.setInitialized(true);
				device1_cooker.setAddress("100:00:FF:000:FFB");
				device1_cooker.setLocationId(GATE_1_LOCATION3);
				device1_cooker.setRefresh(RefreshInterval.SEC_30);
				device1_cooker.setBattery(100);
				device1_cooker.setLastUpdate(timeData);
				device1_cooker.setInvolveTime(timeData);
				device1_cooker.setNetworkQuality(52);

				Module moduleCooker = Module.createFromModuleTypeId("4");
				moduleCooker.setVisibility(true);
				moduleCooker.setName(context.getString(R.string.demo_name_module_cooker));
				moduleCooker.setValue("ON");

				device1_cooker.addModule(moduleCooker);
				deviceList.add(device1_cooker);

				Device device1_lampWc = new Device();
				device1_lampWc.setGateId(gateId);
				device1_lampWc.setInitialized(true);
				device1_lampWc.setAddress("100:00:FF:000:FFD");
				device1_lampWc.setLocationId(GATE_1_LOCATION4);
				device1_lampWc.setRefresh(RefreshInterval.SEC_30);
				device1_lampWc.setBattery(100);
				device1_lampWc.setLastUpdate(timeData);
				device1_lampWc.setInvolveTime(timeData);
				device1_lampWc.setNetworkQuality(52);

				Module moduleLampWc = Module.createFromModuleTypeId("5");
				moduleLampWc.setVisibility(true);
				moduleLampWc.setName(context.getString(R.string.demo_name_module_wc_lamp));
				moduleLampWc.setValue("15");

				device1_lampWc.addModule(moduleLampWc);
				deviceList.add(device1_lampWc);
				break;

			}
			case GATE_2_ID: {
				Device device2_atmosphere = new Device();
				device2_atmosphere.setGateId(gateId);
				device2_atmosphere.setInitialized(true);
				device2_atmosphere.setAddress("101:00:FF:000:FF0");
				device2_atmosphere.setLocationId(GATE_2_LOCATION1);
				device2_atmosphere.setRefresh(RefreshInterval.SEC_1);
				device2_atmosphere.setBattery(100);
				device2_atmosphere.setLastUpdate(DateTime.now());
				device2_atmosphere.setInvolveTime(timeData);
				device2_atmosphere.setNetworkQuality(45);

				Module module_tempAtmosphere = Module.createFromModuleTypeId("10");
				module_tempAtmosphere.setVisibility(true);
				module_tempAtmosphere.setName(context.getString(R.string.demo_name_module_temp_air));
				module_tempAtmosphere.setValue("35");

				device2_atmosphere.addModule(module_tempAtmosphere);

				Module module_pressureAtmosphere = Module.createFromModuleTypeId("2");
				module_pressureAtmosphere.setVisibility(true);
				module_pressureAtmosphere.setName(context.getString(R.string.demo_name_module_pressure_air));
				module_pressureAtmosphere.setValue("25");

				device2_atmosphere.addModule(module_pressureAtmosphere);
				deviceList.add(device2_atmosphere);

				Device device2_soil = new Device();
				device2_soil.setGateId(GATE_1_ID);
				device2_soil.setInitialized(true);
				device2_soil.setAddress("101:00:FF:001:FF0");
				device2_soil.setLocationId(GATE_2_LOCATION1);
				device2_soil.setRefresh(RefreshInterval.SEC_10);
				device2_soil.setBattery(95);
				device2_soil.setLastUpdate(timeData);
				device2_soil.setInvolveTime(timeData);
				device2_soil.setNetworkQuality(60);

				Module moduleSoil = Module.createFromModuleTypeId("1");
				moduleSoil.setVisibility(true);
				moduleSoil.setName(context.getString(R.string.demo_name_module_soil));
				moduleSoil.setValue("50");

				device2_soil.addModule(moduleSoil);
				deviceList.add(device2_soil);

				Device device2_sauna = new Device();
				device2_sauna.setGateId(gateId);
				device2_sauna.setInitialized(true);
				device2_sauna.setAddress("101:00:FF:000:FF1");
				device2_sauna.setLocationId(GATE_2_LOCATION2);
				device2_sauna.setRefresh(RefreshInterval.SEC_30);
				device2_sauna.setBattery(90);
				device2_sauna.setLastUpdate(timeData);
				device2_sauna.setInvolveTime(timeData);
				device2_sauna.setNetworkQuality(55);

				Module moduleTempStove = Module.createFromModuleTypeId("10");
				moduleTempStove.setVisibility(true);
				moduleTempStove.setName(context.getString(R.string.demo_name_module_temp_stove));
				moduleTempStove.setValue("90");

				device2_sauna.addModule(moduleTempStove);

				Module moduleHumCeiling = Module.createFromModuleTypeId("1");
				moduleHumCeiling.setVisibility(true);
				moduleHumCeiling.setName(context.getString(R.string.demo_name_module_humidity_ceiling));
				moduleHumCeiling.setValue("95");

				device2_sauna.addModule(moduleHumCeiling);

				Module moduleSwitch = Module.createFromModuleTypeId("4");
				moduleSwitch.setVisibility(false);
				moduleSwitch.setName(context.getString(R.string.demo_name_module_switch));
				moduleSwitch.setValue("OFF");


				Module moduleElectricity = Module.createFromModuleTypeId("160");
				moduleElectricity.setVisibility(false);
				moduleElectricity.setName(context.getString(R.string.demo_name_module_electricity));
				moduleElectricity.setValue("OFF");

				deviceList.add(device2_sauna);

				Device device2_saunaAddtional = new Device();
				device2_saunaAddtional.setGateId(gateId);
				device2_saunaAddtional.setInitialized(true);
				device2_saunaAddtional.setAddress("101:00:FF:000:FF2");
				device2_saunaAddtional.setLocationId(GATE_2_LOCATION2);
				device2_saunaAddtional.setRefresh(RefreshInterval.MIN_1);
				device2_saunaAddtional.setBattery(78);
				device2_saunaAddtional.setLastUpdate(timeData);
				device2_saunaAddtional.setInvolveTime(timeData);
				device2_saunaAddtional.setNetworkQuality(82);

				Module moduleLighting = Module.createFromModuleTypeId("5");
				moduleLighting.setVisibility(true);
				moduleLighting.setName(context.getString(R.string.demo_name_module_lighting));
				moduleLighting.setValue("30");

				device2_saunaAddtional.addModule(moduleLighting);

				Module moduleAir = Module.createFromModuleTypeId("7");
				moduleAir.setVisibility(false);
				moduleAir.setName(context.getString(R.string.demo_name_module_air));
				moduleAir.setValue("30");

				device2_saunaAddtional.addModule(moduleAir);
				deviceList.add(device2_saunaAddtional);
				break;
			}
			case GATE_3_ID: {
				Device device3 = new Device();
				device3.setGateId(gateId);
				device3.setInitialized(true);
				device3.setAddress("100:00:FF:000:FF0");
				device3.setLocationId(GATE_3_LOCATION1);
				device3.setRefresh(RefreshInterval.SEC_5);
				device3.setBattery(100);
				device3.setLastUpdate(timeData);
				device3.setInvolveTime(timeData);
				device3.setNetworkQuality(45);

				Module device3moduleStatus = Module.createFromModuleTypeId("11");
				device3moduleStatus.setVisibility(true);
				device3moduleStatus.setName(context.getString(R.string.demo_name_module_boiler));
				device3moduleStatus.setValue("0");

				device3.addModule(device3moduleStatus);

				Module device3moduleActualTemp1 = Module.createFromModuleTypeId("10");
				device3moduleActualTemp1.setVisibility(true);
				device3moduleActualTemp1.setName(context.getString(R.string.demo_name_module_actual_temp1));
				device3moduleActualTemp1.setValue("28");

				device3.addModule(device3moduleActualTemp1);

				Module device3moduleDesiredTemp1 = Module.createFromModuleTypeId("165");
				device3moduleDesiredTemp1.setVisibility(true);
				device3moduleDesiredTemp1.setName(context.getString(R.string.demo_name_module_desired_temp1));
				device3moduleDesiredTemp1.setValue("28");

				device3.addModule(device3moduleDesiredTemp1);

				Module device3moduleTypeOp1 = Module.createFromModuleTypeId("166");
				device3moduleTypeOp1.setVisibility(true);
				device3moduleTypeOp1.setName(context.getString(R.string.demo_name_module_type_operation1));
				device3moduleTypeOp1.setValue("3");

				device3.addModule(device3moduleTypeOp1);

				Module device3moduleModeOp1 = Module.createFromModuleTypeId("167");
				device3moduleModeOp1.setVisibility(true);
				device3moduleModeOp1.setName(context.getString(R.string.demo_name_module_mode_operation1));
				device3moduleModeOp1.setValue("1");

				device3.addModule(device3moduleModeOp1);

				Module device3moduleActualTemp2 = Module.createFromModuleTypeId("266");
				device3moduleActualTemp2.setVisibility(true);
				device3moduleActualTemp2.setName(context.getString(R.string.demo_name_module_actual_temp2));
				device3moduleActualTemp2.setValue("26");

				device3.addModule(device3moduleActualTemp2);

				Module device3moduleDesiredTemp2 = Module.createFromModuleTypeId("421");
				device3moduleDesiredTemp2.setVisibility(true);
				device3moduleDesiredTemp2.setName(context.getString(R.string.demo_name_module_desired_temp2));
				device3moduleDesiredTemp2.setValue("30");

				device3.addModule(device3moduleDesiredTemp2);

				Module device3moduleTypeOp2 = Module.createFromModuleTypeId("422");
				device3moduleTypeOp2.setVisibility(true);
				device3moduleTypeOp2.setName(context.getString(R.string.demo_name_module_type_operation2));
				device3moduleTypeOp2.setValue("3");

				device3.addModule(device3moduleTypeOp2);

				Module device3moduleModeOp2 = Module.createFromModuleTypeId("423");
				device3moduleModeOp2.setVisibility(true);
				device3moduleModeOp2.setName(context.getString(R.string.demo_name_module_mode_operation2));
				device3moduleModeOp2.setValue("1");

				device3.addModule(device3moduleModeOp2);


				Module device3moduleActualTemp3 = Module.createFromModuleTypeId("522");
				device3moduleActualTemp3.setVisibility(true);
				device3moduleActualTemp3.setName(context.getString(R.string.demo_name_module_actual_temp3));
				device3moduleActualTemp3.setValue("28");

				device3.addModule(device3moduleActualTemp3);

				Module device3moduleDesiredTemp3 = Module.createFromModuleTypeId("677");
				device3moduleDesiredTemp3.setVisibility(true);
				device3moduleDesiredTemp3.setName(context.getString(R.string.demo_name_module_desired_temp3));
				device3moduleDesiredTemp3.setValue("31");

				device3.addModule(device3moduleDesiredTemp3);

				Module device3moduleTypeOp3 = Module.createFromModuleTypeId("678");
				device3moduleTypeOp3.setVisibility(true);
				device3moduleTypeOp3.setName(context.getString(R.string.demo_name_module_type_operation3));
				device3moduleTypeOp3.setValue("3");

				device3.addModule(device3moduleTypeOp3);

				Module device3moduleModeOp3 = Module.createFromModuleTypeId("679");
				device3moduleModeOp3.setVisibility(true);
				device3moduleModeOp3.setName(context.getString(R.string.demo_name_module_mode_operation3));
				device3moduleModeOp3.setValue("1");

				device3.addModule(device3moduleModeOp3);

				Module device3moduleActualTemp4 = Module.createFromModuleTypeId("778");
				device3moduleActualTemp4.setVisibility(true);
				device3moduleActualTemp4.setName(context.getString(R.string.demo_name_module_actual_temp4));
				device3moduleActualTemp4.setValue("29");

				device3.addModule(device3moduleActualTemp4);

				Module device3moduleDesiredTemp4 = Module.createFromModuleTypeId("933");
				device3moduleDesiredTemp4.setVisibility(true);
				device3moduleDesiredTemp4.setName(context.getString(R.string.demo_name_module_desired_temp4));
				device3moduleDesiredTemp4.setValue("31");

				device3.addModule(device3moduleDesiredTemp4);

				Module device3moduleTypeOp4 = Module.createFromModuleTypeId("934");
				device3moduleTypeOp4.setVisibility(true);
				device3moduleTypeOp4.setName(context.getString(R.string.demo_name_module_type_operation4));
				device3moduleTypeOp4.setValue("3");

				device3.addModule(device3moduleTypeOp4);

				Module device3moduleModeOp4 = Module.createFromModuleTypeId("935");
				device3moduleModeOp4.setVisibility(true);
				device3moduleModeOp4.setName(context.getString(R.string.demo_name_module_mode_operation4));
				device3moduleModeOp4.setValue("1");

				device3.addModule(device3moduleModeOp4);
				deviceList.add(device3);

				break;
			}
			default: {
				//do nothing

				break;
			}
		}

		return deviceList;

	}

	public List<Watchdog> getWatchdogs(Context context, String gateID) {
		List<Watchdog> watchdogList = new ArrayList<>();

		switch (gateID) {

			case GATE_1_ID: {
				Watchdog watchdog1_childrenCry = new Watchdog(1);
				watchdog1_childrenCry.setId("1");
				watchdog1_childrenCry.setGateId(gateID);
				watchdog1_childrenCry.setEnabled(false);
				watchdog1_childrenCry.setName(context.getString(R.string.demo_name_watchdog_cry));

				TreeMap<String, String> tDevices1_childrenCry = new TreeMap<>();
				TreeMap<String, String> tParams1_childrenCry = new TreeMap<>();

				String moduleControlCry = ("100:00:FF:000:FF4" + Module.ID_SEPARATOR + "6");
				tDevices1_childrenCry.put("1", moduleControlCry);
				tParams1_childrenCry.put("1", "100:00:FF:000:FF4");
				tParams1_childrenCry.put("2", "gt");
				tParams1_childrenCry.put("3", "60");
				tParams1_childrenCry.put("4", "notif");
				tParams1_childrenCry.put("5", context.getString(R.string.demo_notif_watchdog_cry));

				watchdog1_childrenCry.setModules(new ArrayList<>(tDevices1_childrenCry.values()));
				watchdog1_childrenCry.setParams(new ArrayList<>(tParams1_childrenCry.values()));

				watchdogList.add(watchdog1_childrenCry);

				Watchdog watchdog1_radiator = new Watchdog(1);
				watchdog1_radiator.setId("2");
				watchdog1_radiator.setGateId(gateID);
				watchdog1_radiator.setEnabled(true);
				watchdog1_radiator.setName(context.getString(R.string.demo_name_watchdog_radiator));

				TreeMap<String, String> tDevices1_radiator = new TreeMap<>();
				TreeMap<String, String> tParams1_radiator = new TreeMap<>();

				String moduleControlRadiator = ("100:00:FF:000:FF1" + Module.ID_SEPARATOR + "10");
				tDevices1_radiator.put("1", moduleControlRadiator);
				tParams1_radiator.put("1", "101:00:FF:001:FF0");
				tParams1_radiator.put("2", "lt");
				tParams1_radiator.put("3", "15");
				tParams1_radiator.put("4", "notif");
				tParams1_radiator.put("5", context.getString(R.string.demo_notif_watchdog_radiator));

				watchdog1_radiator.setModules(new ArrayList<>(tDevices1_radiator.values()));
				watchdog1_radiator.setParams(new ArrayList<>(tParams1_radiator.values()));

				watchdogList.add(watchdog1_radiator);

				break;
			}

			case GATE_2_ID: {
				Watchdog watchdog2_tempStone = new Watchdog(1);
				watchdog2_tempStone.setId("3");
				watchdog2_tempStone.setGateId(gateID);
				watchdog2_tempStone.setEnabled(false);
				watchdog2_tempStone.setName(context.getString(R.string.demo_name_watchdog_temp_stone));

				TreeMap<String, String> tDevices2_tempStone = new TreeMap<>();
				TreeMap<String, String> tParams2_tempStone = new TreeMap<>();

				String moduleControlTempStove = ("101:00:FF:000:FF1" + Module.ID_SEPARATOR + "10");
				tDevices2_tempStone.put("1", moduleControlTempStove);
				tParams2_tempStone.put("1", "101:00:FF:000:FF1");
				tParams2_tempStone.put("2", "gt");
				tParams2_tempStone.put("3", "120");
				tParams2_tempStone.put("4", "notif");
				tParams2_tempStone.put("5", context.getString(R.string.demo_notif_watchdog_temp_stone));

				watchdog2_tempStone.setModules(new ArrayList<>(tDevices2_tempStone.values()));
				watchdog2_tempStone.setParams(new ArrayList<>(tParams2_tempStone.values()));

				watchdogList.add(watchdog2_tempStone);

				Watchdog watchdog2_humidity = new Watchdog(1);
				watchdog2_humidity.setId("4");
				watchdog2_humidity.setGateId(gateID);
				watchdog2_humidity.setEnabled(true);
				watchdog2_humidity.setName(context.getString(R.string.demo_name_watchdog_humidity));

				TreeMap<String, String> tDevices2_humidity = new TreeMap<>();
				TreeMap<String, String> tParams2_humidity = new TreeMap<>();

				String moduleControlHumidity = ("101:00:FF:001:FF0" + Module.ID_SEPARATOR + "1");
				tDevices2_humidity.put("1", moduleControlHumidity);
				tParams2_humidity.put("1", "101:00:FF:001:FF0");
				tParams2_humidity.put("2", "lt");
				tParams2_humidity.put("3", "50");
				tParams2_humidity.put("4", "notif");
				tParams2_humidity.put("5", context.getString(R.string.demo_notif_watchdog_humidity));

				watchdog2_humidity.setModules(new ArrayList<>(tDevices2_humidity.values()));
				watchdog2_humidity.setParams(new ArrayList<>(tParams2_humidity.values()));

				watchdogList.add(watchdog2_humidity);

				break;
			}

			case GATE_3_ID: {
				Watchdog watchdog3 = new Watchdog(1);
				watchdog3.setId("5");
				watchdog3.setGateId(gateID);
				watchdog3.setEnabled(false);
				watchdog3.setName(context.getString(R.string.demo_name_watchdog_temperature));

				TreeMap<String, String> tDevices3 = new TreeMap<>();
				TreeMap<String, String> tParams3 = new TreeMap<>();

				String moduleControlTemperature = ("100:00:FF:000:FF0" + Module.ID_SEPARATOR + "10");
				tDevices3.put("1", moduleControlTemperature);
				tParams3.put("1", "100:00:FF:000:FF0");
				tParams3.put("2", "gt");
				tParams3.put("3", "29");
				tParams3.put("4", "notif");
				tParams3.put("5", context.getString(R.string.demo_notif_watchdog_temperature));

				watchdog3.setModules(new ArrayList<>(tDevices3.values()));
				watchdog3.setParams(new ArrayList<>(tParams3.values()));

				watchdogList.add(watchdog3);


				break;
			}

			default: {
				//do nothing
				break;
			}

		}

		return watchdogList;
	}

}
