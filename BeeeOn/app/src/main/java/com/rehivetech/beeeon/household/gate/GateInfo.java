package com.rehivetech.beeeon.household.gate;

import com.rehivetech.beeeon.household.user.User;

public class GateInfo extends Gate {
	public static final String TAG = GateInfo.class.getSimpleName();

	protected String mOwner = "";
	protected int mDevicesCount;
	protected int mUsersCount;
	protected String mVersion = "";
	protected String mIp = "";

	public GateInfo(String id, String name, String owner, User.Role role, int utcOffsetInMinutes, int devicesCount, int usersCount, String version, String ip) {
		super(id, name);
		mOwner = owner;
		mRole = role;
		mUtcOffsetInMinutes = utcOffsetInMinutes;
		mDevicesCount = devicesCount;
		mUsersCount = usersCount;
		mVersion = version;
		mIp = ip;
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
}
