package com.rehivetech.beeeon.household.device;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.rehivetech.beeeon.util.Log;

/**
 * Created by Robyer on 29. 5. 2015.
 */
public class DeviceFeatures {
	private static final String TAG = DeviceFeatures.class.getSimpleName();

	private final Refresh mRefresh;
	private final Battery mBattery;
	private final Led mLed;

	/**
	 * Create device features object.
	 *
	 * @param refresh default refresh interval in seconds or null if refresh is not available
	 * @param led true if led is available, false otherwise
	 * @param battery true if battery is available, false otherwise
	 */
	public DeviceFeatures(@Nullable Integer refresh, boolean led, boolean battery) {
		mRefresh = refresh != null ? new Refresh(refresh) : null;
		mLed = led ? new Led() : null;
		mBattery = battery ? new Battery() : null;
	}

	public boolean hasRefresh() {
		return mRefresh != null;
	}

	@Nullable
	public RefreshInterval getDefaultRefresh() {
		return hasRefresh() ? mRefresh.defaultInterval : null;
	}

	@Nullable
	public RefreshInterval getActualRefresh() {
		if (!hasRefresh()) {
			return RefreshInterval.SEC_1;
			// FIXME: should return null, but need to adapt rest of code first
			//return null;
		}

		return mRefresh.actualInterval != null ? mRefresh.actualInterval : mRefresh.defaultInterval;
	}

	public void setActualRefresh(@NonNull RefreshInterval refresh) {
		if (!hasRefresh()) {
			// TODO: Throw exception?
			Log.w(TAG, "Trying to set refresh value, but Device doesn't have refresh.");
			return;
		}
		mRefresh.actualInterval = refresh;
	}

	public boolean hasLed() {
		return mLed != null;
	}

	public boolean hasBattery() {
		return mBattery != null;
	}

	public void setBatteryValue(int value) {
		if (!hasBattery()) {
			// TODO: Throw exception?
			Log.w(TAG, "Trying to set refresh value, but Device doesn't have battery.");
			return;
		}
		mBattery.actualValue = value;
	}

	public int getBatteryValue() {
		return mBattery.actualValue;
	}

	private static class Refresh {
		public final RefreshInterval defaultInterval;
		public RefreshInterval actualInterval;

		public Refresh(int defaultSecs) {
			this.defaultInterval = RefreshInterval.fromInterval(defaultSecs);
		}
	}

	private static class Battery {
		public int actualValue;
	}

	private static class Led {
	}
}
