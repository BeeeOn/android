package com.rehivetech.beeeon.network;

import android.content.Context;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.DeviceType;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.household.user.User;

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

	private static final String GATE_1_LOCATION1 = "5555";
	private static final String GATE_1_LOCATION2 = "2222";
	private static final String GATE_1_LOCATION3 = "4444";
	private static final String GATE_1_LOCATION4 = "6666";
	private static final String GATE_2_LOCATION1 = "1111";
	private static final String GATE_2_LOCATION2 = "2";

	public List<Gate> getGates(Context context) {

		List<Gate> gateList = new ArrayList<>();

		Gate demoGate1 = new Gate(GATE_1_ID, context.getString(R.string.gate_1_name));
		demoGate1.setRole(User.Role.Admin);
		demoGate1.setUtcOffset(-60);
		gateList.add(demoGate1);

		Gate demoGate2 = new Gate(GATE_2_ID, context.getString(R.string.gate_2_name));
		demoGate2.setRole(User.Role.Superuser);
		demoGate2.setUtcOffset(60);
		gateList.add(demoGate2);

		return gateList;
	}

	public List<Location> getLocation(Context context, String gateId) {

		List<Location> locationList = new ArrayList<>();
		switch (gateId) {
			case GATE_1_ID: {
				Location location1LivingRoom = new Location(GATE_1_LOCATION1, context.getString(R.string.loc_name_demo_living_room), GATE_1_ID, "5");
				Location location1BedRoom = new Location(GATE_1_LOCATION2, context.getString(R.string.loc_name_demo_bedroom), GATE_1_ID, "2");
				Location location1Kitchen = new Location(GATE_1_LOCATION3, context.getString(R.string.loc_name_demo_kitchen), GATE_1_ID, "4");
				Location location1Wc = new Location(GATE_1_LOCATION4, context.getString(R.string.loc_name_demo_wc), GATE_1_ID, "6");

				locationList.add(location1LivingRoom);
				locationList.add(location1BedRoom);
				locationList.add(location1Kitchen);
				locationList.add(location1Wc);
				break;
			}

			case GATE_2_ID: {
				Location location2GreenHouse = new Location(GATE_2_LOCATION1, context.getString(R.string.loc_name_demo_green_house), GATE_2_ID, "3");
				Location location2Sauna = new Location(GATE_2_LOCATION2, context.getString(R.string.loc_name_demo_sauna), GATE_2_ID, "1");

				locationList.add(location2GreenHouse);
				locationList.add(location2Sauna);
				break;
			}

			default: {
				//do nothing
				break;
			}
		}


		return locationList;
	}

	public List<Device> getDevices(String gateId) {
		List<Device> deviceList = new ArrayList<>();

		int timeLastUpdate = 1377684610;
		DateTime timeData = new DateTime((long) (timeLastUpdate * 1000), DateTimeZone.UTC);

		switch (gateId) {

			case GATE_1_ID: {
				Device device = Device.createDeviceByType(DeviceType.TYPE_0.getId(), gateId, "100:00:FF:000:FF0");
				device.setInitialized(true);
				device.setLocationId(GATE_1_LOCATION1);
				device.setRefresh(RefreshInterval.SEC_5);
				device.setBattery(100);
				device.setLastUpdate(DateTime.now());
				device.setPairedTime(timeData);
				device.setNetworkQuality(52);

				deviceList.add(device);
				break;
			}
			case GATE_2_ID: {
				Device device = Device.createDeviceByType(DeviceType.TYPE_1.getId(), gateId, "100:00:FF:000:FF1");
				device.setInitialized(true);
				device.setLocationId(GATE_2_LOCATION1);
				device.setLastUpdate(DateTime.now());
				device.setPairedTime(timeData);
				device.setNetworkQuality(52);

				deviceList.add(device);
				break;
			}
			default: {
				//do nothing

				break;
			}
		}

		return deviceList;

	}

}
