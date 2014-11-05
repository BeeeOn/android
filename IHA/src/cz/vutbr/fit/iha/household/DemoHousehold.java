package cz.vutbr.fit.iha.household;

import java.util.List;
import java.util.Random;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import android.content.Context;
import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.adapter.device.Facility;
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
				List<Facility> facilities = parser.getDemoFacilitiesFromAsset(mContext, assetName);

				// Set last update time to time between (-26 hours, now>
				for (Facility facility : facilities) {
					facility.setLastUpdate(DateTime.now(DateTimeZone.UTC).minusSeconds(new Random().nextInt(60 * 60 * 26)));
				}

				facilitiesModel.setFacilitiesByAdapter(adapter.getId(), facilities);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
