package com.rehivetech.beeeon.asynctask;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.adapter.Adapter;

/**
 * Reloads specified data from server.
 */
public class ReloadAdapterDataTask extends CallbackTask<String> {

	private final boolean mForceReload;

	private final ReloadWhat mWhat;

	public enum ReloadWhat {
		ADAPTERS_AND_ACTIVE_ADAPTER,
		LOCATIONS,
		FACILITIES,
		UNINITIALIZED_FACILITIES,
		USERS,
		WATCHDOGS,
	}

	public ReloadAdapterDataTask(Context context, boolean forceReload, ReloadWhat what) {
		super(context);

		mForceReload = forceReload;
		mWhat = what;
	}

	@Override
	protected Boolean doInBackground(String adapterId) {
		Controller controller = Controller.getInstance(mContext);

		if (mWhat == ReloadWhat.ADAPTERS_AND_ACTIVE_ADAPTER) {
			controller.getAdaptersModel().reloadAdapters(mForceReload);

			Adapter active = controller.getActiveAdapter();
			if (active != null) {
				adapterId = active.getId();
			} else {
				return true;
			}
		}

		if (mWhat == ReloadWhat.LOCATIONS || mWhat == ReloadWhat.FACILITIES) {
			controller.getLocationsModel().reloadLocationsByAdapter(adapterId, mForceReload);
		}

		if (mWhat == ReloadWhat.FACILITIES) {
			controller.getFacilitiesModel().reloadFacilitiesByAdapter(adapterId, mForceReload);
		}

		if (mWhat == ReloadWhat.UNINITIALIZED_FACILITIES) {
			controller.getUninitializedFacilitiesModel().reloadUninitializedFacilitiesByAdapter(adapterId, mForceReload);
		}

		if (mWhat == ReloadWhat.USERS) {
			controller.getUsersModel().reloadUsersByAdapter(adapterId, mForceReload);
		}

		if (mWhat == ReloadWhat.WATCHDOGS) {
			controller.getWatchDogsModel().reloadWatchDogsByAdapter(adapterId, mForceReload);
		}

		return true;
	}

}
