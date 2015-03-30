package com.rehivetech.beeeon.household;

import android.content.Context;
import com.rehivetech.beeeon.adapter.Adapter;
import com.rehivetech.beeeon.network.INetwork;
import com.rehivetech.beeeon.persistence.AdaptersModel;
import com.rehivetech.beeeon.persistence.DeviceLogsModel;
import com.rehivetech.beeeon.persistence.FacilitiesModel;
import com.rehivetech.beeeon.persistence.LocationsModel;
import com.rehivetech.beeeon.persistence.UninitializedFacilitiesModel;
import com.rehivetech.beeeon.persistence.WatchDogsModel;

/**
 * Represents "household" for logged user with all adapters and custom lists.
 * 
 * @author Robyer
 */
public final class Household {

	protected final Context mContext;

	/** Logged in user. */
	public final User user = new User();

	/** List of adapters that this user has access to (either as owner, user or guest). */
	public final AdaptersModel adaptersModel;

	public final LocationsModel locationsModel;

	public final FacilitiesModel facilitiesModel;

	public final UninitializedFacilitiesModel uninitializedFacilitiesModel;
	
	public final DeviceLogsModel deviceLogsModel;

	public final WatchDogsModel watchDogsModel;

	/** Active adapter. */
	public Adapter activeAdapter;

	public Household(Context context, INetwork network) {
		mContext = context;

		adaptersModel = new AdaptersModel(network);
		locationsModel = new LocationsModel(network, context);
		facilitiesModel = new FacilitiesModel(network);
		uninitializedFacilitiesModel = new UninitializedFacilitiesModel(network);
		deviceLogsModel = new DeviceLogsModel(network);
		watchDogsModel = new WatchDogsModel(network);
	}

}
