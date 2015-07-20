package com.rehivetech.beeeon.household.device;

import android.support.annotation.Nullable;

/**
 * Created by Robyer on 29. 5. 2015.
 */
public class DeviceFeatures {
	private final RefreshInterval mRefresh;
	private final boolean mBattery;
	private final boolean mLed;

	/**
	 * Create device features object.
	 *
	 * @param refresh default refresh interval in seconds or null if refresh is not available
	 * @param led true if led is available, false otherwise
	 * @param battery true if battery is available, false otherwise
	 */
	public DeviceFeatures(@Nullable Integer refresh, boolean led, boolean battery) {
		mRefresh = refresh != null ? RefreshInterval.fromInterval(refresh) : null;
		mLed = led;
		mBattery = battery;
	}

	@Nullable
	public RefreshInterval getDefaultRefresh() {
		return mRefresh;
	}

	public boolean hasRefresh() {
		return mRefresh != null;
	}

	public boolean hasLed() {
		return mLed;
	}

	public boolean hasBattery() {
		return mBattery;
	}
}
