package cz.vutbr.fit.iha.household;

import android.content.Context;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.network.Network;
import cz.vutbr.fit.iha.persistence.AdaptersModel;
import cz.vutbr.fit.iha.persistence.FacilitiesModel;
import cz.vutbr.fit.iha.persistence.LocationsModel;
import cz.vutbr.fit.iha.persistence.UninitializedFacilitiesModel;

/**
 * Represents "household" for logged user with all adapters and custom lists.
 * 
 * @author Robyer
 */
public class Household {

	protected final Context mContext;

	/** Logged in user. */
	public final ActualUser user = new ActualUser();

	/** List of adapters that this user has access to (either as owner, user or guest). */
	public final AdaptersModel adaptersModel;

	public final LocationsModel locationsModel;

	public final FacilitiesModel facilitiesModel;

	public final UninitializedFacilitiesModel uninitializedFacilitiesModel;

	/** Active adapter. */
	public Adapter activeAdapter;

	public Household(Context context, Network network) {
		mContext = context;

		adaptersModel = new AdaptersModel(network);
		locationsModel = new LocationsModel(network);
		facilitiesModel = new FacilitiesModel(network);
		uninitializedFacilitiesModel = new UninitializedFacilitiesModel(network);
	}

}
