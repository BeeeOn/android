package com.rehivetech.beeeon.household.gate;

import android.support.annotation.NonNull;

import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.util.GpsData;

public class GateInfo extends Gate {

	protected String mOwner = "";
	protected int mDevicesCount;
	protected int mUsersCount;
	protected String mVersion = "";
	protected String mIp = "";
	protected GpsData mGpsData = new GpsData();

	public GateInfo(String id, String name) {
		super(id, name);
	}

	public GateInfo(String id, String name, String owner, User.Role role, int utcOffsetInMinutes, int devicesCount, int usersCount, String version, String ip, @NonNull GpsData gpsData) {
		super(id, name);
		mOwner = owner;
		mRole = role;
		mUtcOffsetInMinutes = utcOffsetInMinutes;
		mDevicesCount = devicesCount;
		mUsersCount = usersCount;
		mVersion = version;
		mIp = ip;
		mGpsData = gpsData;
	}

	public String getOwner() {
		return mOwner;
	}

	public int getDevicesCount() {
		return mDevicesCount;
	}

	public int getUsersCount() {
		return mUsersCount;
	}

	public String getVersion() {
		return mVersion;
	}

	public String getIp() {
		return mIp;
	}

	@NonNull
	public GpsData getGpsData() {
		return mGpsData;
	}

	public void setOwner(String owner) {
		mOwner = owner;
	}

	public void setDevicesCount(int devicesCount) {
		mDevicesCount = devicesCount;
	}

	public void setUsersCount(int usersCount) {
		mUsersCount = usersCount;
	}

	public void setVersion(String version) {
		mVersion = version;
	}

	public void setIp(String ip) {
		mIp = ip;
	}

	public void setGpsData(@NonNull GpsData gpsData) {
		mGpsData = gpsData;
	}
}
