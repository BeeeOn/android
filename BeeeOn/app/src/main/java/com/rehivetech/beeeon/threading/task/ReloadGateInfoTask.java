package com.rehivetech.beeeon.threading.task;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.threading.CallbackTask;

import java.util.EnumSet;

/**
 * Reloads specified data from server.
 */
public class ReloadGateInfoTask extends CallbackTask<String> {

	private final boolean mForceReload;

	private EnumSet<ReloadWhat> mWhat;

	public enum ReloadWhat {
		ACTIVE_GATE, // This special item gets ID of active gate automatically and for it load specified data; Also loads all GATES automatically
		GATES,
		LOCATIONS,
		DEVICES, // With devices are also automatically loaded LOCATIONS
		UNINITIALIZED_DEVICES,
		USERS,
		WATCHDOGS,
	}

	public static final EnumSet<ReloadWhat> RELOAD_GATES_AND_ACTIVE_GATE_DEVICES = EnumSet.of(
			ReloadWhat.ACTIVE_GATE,
			ReloadWhat.GATES,
			ReloadWhat.DEVICES
	);

	public ReloadGateInfoTask(Context context, boolean forceReload, ReloadWhat what) {
		this(context, forceReload, EnumSet.of(what));
	}

	public ReloadGateInfoTask(Context context, boolean forceReload, EnumSet<ReloadWhat> what) {
		super(context);

		mForceReload = forceReload;
		mWhat = what;
	}

	@Override
	protected Boolean doInBackground(String gateId) {
		Controller controller = Controller.getInstance(mContext);

		if (mWhat.contains(ReloadWhat.GATES) || mWhat.contains(ReloadWhat.ACTIVE_GATE)) {
			controller.getGatesModel().reloadGates(mForceReload);
		}

		if (mWhat.contains(ReloadWhat.ACTIVE_GATE)) {
			Gate active = controller.getActiveGate();
			if (active == null)
				return true;

			// Get ID of active gate so we can load specified data then
			gateId = active.getId();
			
		} else if (gateId == null) {
			throw new IllegalArgumentException("Either ReloadWhat.ACTIVE_GATE must be used or given gateId parameter.");
		}

		if (mWhat.contains(ReloadWhat.LOCATIONS) || mWhat.contains(ReloadWhat.DEVICES)) {
			controller.getLocationsModel().reloadLocationsByGate(gateId, mForceReload);
		}

		if (mWhat.contains(ReloadWhat.DEVICES)) {
			controller.getDevicesModel().reloadDevicesByGate(gateId, mForceReload);
		}

		if (mWhat.contains(ReloadWhat.UNINITIALIZED_DEVICES)) {
			controller.getUninitializedDevicesModel().reloadUninitializedDevicesByGate(gateId, mForceReload);
		}

		if (mWhat.contains(ReloadWhat.USERS)) {
			controller.getUsersModel().reloadUsersByGate(gateId, mForceReload);
		}

		if (mWhat.contains(ReloadWhat.WATCHDOGS)) {
			controller.getWatchdogsModel().reloadWatchdogsByGate(gateId, mForceReload);
		}

		return true;
	}

}
