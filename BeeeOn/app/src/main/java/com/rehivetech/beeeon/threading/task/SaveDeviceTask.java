package com.rehivetech.beeeon.threading.task;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.ModuleType;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.model.DevicesModel;
import com.rehivetech.beeeon.threading.CallbackTask;

import java.util.List;

public class SaveDeviceTask extends CallbackTask<Device.DataPair> {

	public SaveDeviceTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(Device.DataPair pair) {
		Controller controller = Controller.getInstance(mContext);

		if (pair.location != null && pair.location.getId().equals(Location.NEW_LOCATION_ID)) {
			// We need to save new location to server first
			Location newLocation = controller.getLocationsModel().createLocation(pair.location);
			if (newLocation == null)
				return false;

			pair.device.setLocationId(newLocation.getId());
		}

		DevicesModel devicesModel = controller.getDevicesModel();

		// Save device data
		devicesModel.saveDevice(pair.device);

		// Don't save refresh during initializing
		if (!pair.initializing) {
			// Save refresh interval as actor switch
			List<Module> refreshModules = pair.device.getModulesByType(ModuleType.TYPE_REFRESH.getTypeId());
			if (!refreshModules.isEmpty()) {
				devicesModel.switchActor(refreshModules.get(0));
			}
		}

		return true;
	}

}
