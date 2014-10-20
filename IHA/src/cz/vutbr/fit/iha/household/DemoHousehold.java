package cz.vutbr.fit.iha.household;

import android.content.Context;
import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.network.Network;
import cz.vutbr.fit.iha.network.xml.XmlParsers;

/**
 * Represents demo household with adapters and facilities loaded from local assets files.
 * 
 * @author Robyer
 */
public final class DemoHousehold extends Household {

	public static final String DEMO_EMAIL = "demo";
	
	public DemoHousehold(Context context, Network network) {
		super(context, network);

		prepareAdapters();
	}

	/**
	 * Prepare demo adapters.
	 */
	private void prepareAdapters() {
		try {
			XmlParsers parser = new XmlParsers();

			String assetName = Constants.ASSET_ADAPTERS_FILENAME;
			adaptersModel.setAdapters(parser.getDemoAdaptersFromAsset(mContext, assetName));

			for (Adapter adapter : adaptersModel.getAdapters()) {
				assetName = String.format(Constants.ASSET_LOCATIONS_FILENAME, adapter.getId());
				locationsModel.setLocationsByAdapter(adapter.getId(), parser.getDemoLocationsFromAsset(mContext, assetName));

				assetName = String.format(Constants.ASSET_ADAPTER_DATA_FILENAME, adapter.getId());
				facilitiesModel.setFacilitiesByAdapter(adapter.getId(), parser.getDemoFacilitiesFromAsset(mContext, assetName));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
