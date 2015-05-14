package com.rehivetech.beeeon.asynctask;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.adapter.Adapter;

/**
 * Reloads specified data from server.
 */
public class ReloadAdapterDataTask extends CallbackTask<String> {

	private final boolean mForceReload;

	private ReloadWhat mWhat;

	public enum ReloadWhat {
		ADAPTERS_AND_ACTIVE_ADAPTER,
		LOCATIONS,
		FACILITIES,
		UNINITIALIZED_FACILITIES,
		USERS,
		WATCHDOGS,
		ACHIEVEMENTS,
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
			if (active == null)
				return true;

			// We need to update also facilities
			adapterId = active.getId();
			mWhat = ReloadWhat.FACILITIES;
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
		if (mWhat == ReloadWhat.ACHIEVEMENTS) {
			controller.getAchievementsModel().reloadAchievementsByAdapter(adapterId, mForceReload);
		}

		return true;
	}

}
