package com.rehivetech.beeeon.network;

import android.content.Context;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.DeviceType;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.household.user.User;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by vico on 8.7.2015.
 */
public class DemoData {

	private static final String GATE_1_ID = "64206";
	private static final String GATE_2_ID = "65260";

	private static final String GATE_1_LOCATION1 = "1";
	private static final String GATE_1_LOCATION2 = "2";
	private static final String GATE_1_LOCATION3 = "3";
	private static final String GATE_1_LOCATION4 = "4";
	private static final String GATE_2_LOCATION1 = "5";
	private static final String GATE_2_LOCATION2 = "6";

	/**
	 * Holds ID of newly created devices in this class.
	 *
	 * @see #createDevice(DeviceType, String, String)
	 */
	private int deviceIdGenerator = 1000;

	public List<Gate> getGates(Context context) {
		List<Gate> gatesList = new ArrayList<>();

		Gate demoGate1 = new Gate(GATE_1_ID, context.getString(R.string.gate_1_name));
		demoGate1.setRole(User.Role.Admin);
		demoGate1.setUtcOffset(500);
		gatesList.add(demoGate1);

		Gate demoGate2 = new Gate(GATE_2_ID, context.getString(R.string.gate_2_name));
		demoGate2.setRole(User.Role.Superuser);
		demoGate2.setUtcOffset(60);
		gatesList.add(demoGate2);

		return gatesList;
	}

	public List<Location> getLocation(Context context, String gateId) {
		List<Location> locationsList = new ArrayList<>();

		switch (gateId) {
			case GATE_1_ID: {
				Location location1LivingRoom = new Location(GATE_1_LOCATION1, context.getString(R.string.loc_name_demo_living_room), GATE_1_ID, "5");
				Location location1BedRoom = new Location(GATE_1_LOCATION2, context.getString(R.string.loc_name_demo_bedroom), GATE_1_ID, "2");
				Location location1Kitchen = new Location(GATE_1_LOCATION3, context.getString(R.string.loc_name_demo_kitchen), GATE_1_ID, "4");
				Location location1Wc = new Location(GATE_1_LOCATION4, context.getString(R.string.loc_name_demo_wc), GATE_1_ID, "6");

				locationsList.add(location1LivingRoom);
				locationsList.add(location1BedRoom);
				locationsList.add(location1Kitchen);
				locationsList.add(location1Wc);
				break;
			}

			case GATE_2_ID: {
				Location location2GreenHouse = new Location(GATE_2_LOCATION1, context.getString(R.string.loc_name_demo_green_house), GATE_2_ID, "3");
				Location location2Sauna = new Location(GATE_2_LOCATION2, context.getString(R.string.loc_name_demo_sauna), GATE_2_ID, "1");

				locationsList.add(location2GreenHouse);
				locationsList.add(location2Sauna);
				break;
			}

			default: {
				//do nothing
				break;
			}
		}

		return locationsList;
	}

	public List<Device> getDevices(String gateId) {
		List<Device> devicesList = new ArrayList<>();

		switch (gateId) {
			case GATE_1_ID: {
				devicesList.add(createDevice(DeviceType.TYPE_0, gateId, GATE_1_LOCATION1));
				devicesList.add(createDevice(DeviceType.TYPE_2, gateId, GATE_1_LOCATION2));
				devicesList.add(createDevice(DeviceType.TYPE_3, gateId, Location.NO_LOCATION_ID));
				devicesList.add(createDevice(DeviceType.TYPE_4, gateId, Location.NO_LOCATION_ID));
				devicesList.add(createDevice(DeviceType.TYPE_5, gateId, GATE_1_LOCATION3));
				break;
			}
			case GATE_2_ID: {
				devicesList.add(createDevice(DeviceType.TYPE_1, gateId, GATE_2_LOCATION1));
				break;
			}
			default: {
				//do nothing
				break;
			}
		}

		return devicesList;
	}

	private Device createDevice(DeviceType type, String gateId, String locationId) {
		Random rand = new Random();

		Device device = Device.createDeviceByType(type.getId(), gateId, String.valueOf(++deviceIdGenerator));
		device.setLocationId(locationId);
		device.setRefresh(RefreshInterval.fromInterval(rand.nextInt(3600)));
		device.setBattery(rand.nextInt(100));
		device.setLastUpdate(DateTime.now(DateTimeZone.UTC).minusSeconds(rand.nextInt(500)));
		device.setPairedTime(DateTime.now(DateTimeZone.UTC).minusMinutes(rand.nextInt(60)));
		device.setNetworkQuality(rand.nextInt(100));

		return device;
	}

}
