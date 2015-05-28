package com.rehivetech.beeeon.threading.task;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.threading.CallbackTask;

/**
 * Reloads specified data from server.
 */
public class ReloadGateDataTask extends CallbackTask<String> {

	private final boolean mForceReload;

	private ReloadWhat mWhat;

	public enum ReloadWhat {
		GATES_AND_ACTIVE_GATE,
		LOCATIONS,
		DEVICES,
		UNINITIALIZED_DEVICES,
		USERS,
		WATCHDOGS,
		ACHIEVEMENTS,
	}

	public ReloadGateDataTask(Context context, boolean forceReload, ReloadWhat what) {
		super(context);

		mForceReload = forceReload;
		mWhat = what;
	}

	@Override
	protected Boolean doInBackground(String gateId) {
		Controller controller = Controller.getInstance(mContext);

		if (mWhat == ReloadWhat.GATES_AND_ACTIVE_GATE) {
			controller.getGatesModel().reloadGates(mForceReload);

			Gate active = controller.getActiveGate();
			if (active == null)
				return true;

			// We need to update also devices
			gateId = active.getId();
			mWhat = ReloadWhat.DEVICES;
		}

		if (mWhat == ReloadWhat.LOCATIONS || mWhat == ReloadWhat.DEVICES) {
			controller.getLocationsModel().reloadLocationsByGate(gateId, mForceReload);
		}

		if (mWhat == ReloadWhat.DEVICES) {
			controller.getDevicesModel().reloadDevicesByGate(gateId, mForceReload);
		}

		if (mWhat == ReloadWhat.UNINITIALIZED_DEVICES) {
			controller.getUninitializedDevicesModel().reloadUninitializedDevicesByGate(gateId, mForceReload);
		}

		if (mWhat == ReloadWhat.USERS) {
			controller.getUsersModel().reloadUsersByGate(gateId, mForceReload);
		}

		if (mWhat == ReloadWhat.WATCHDOGS) {
			controller.getWatchdogsModel().reloadWatchdogsByGate(gateId, mForceReload);
		}
		if (mWhat == ReloadWhat.ACHIEVEMENTS) {
			controller.getAchievementsModel().reloadAchievementsByGate(gateId, mForceReload);
		}

		return true;
	}

}
