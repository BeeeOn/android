package cz.vutbr.fit.iha.asynctask;

import java.util.EnumSet;

import android.content.Context;
import cz.vutbr.fit.iha.adapter.device.BaseDevice.SaveDevice;
import cz.vutbr.fit.iha.adapter.location.Location;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.pair.InitializeFacilityPair;

public class InitializeFacilityTask extends CallbackTask<InitializeFacilityPair> {
	
	private Context mContext;
	
	public InitializeFacilityTask(Context context) {
		super();
		mContext = context;
	}
	
	@Override
	protected Boolean doInBackground(InitializeFacilityPair pair) {
		Controller controller = Controller.getInstance(mContext);

		if (pair.location.getId().equals(Location.NEW_LOCATION_ID)) {
			// We need to save new location to server first
			Location newLocation = controller.addLocation(pair.location);
			if (newLocation == null)
				return false;

			pair.facility.setLocationId(newLocation.getId());
		}
	
		EnumSet<SaveDevice> what = EnumSet.of(SaveDevice.SAVE_LOCATION, SaveDevice.SAVE_NAME);

		return controller.saveFacility(pair.facility, what);
	}

}
