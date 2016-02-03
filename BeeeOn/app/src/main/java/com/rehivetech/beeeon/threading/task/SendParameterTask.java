package com.rehivetech.beeeon.threading.task;

import android.content.Context;
import android.util.Pair;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.ModuleType;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.model.DevicesModel;
import com.rehivetech.beeeon.threading.CallbackTask;

import java.util.List;

/**
 * @author Tomas Mlynaric
 */
public class SendParameterTask extends CallbackTask<Pair<String, String>> {

	protected final Device mDevice;

	public SendParameterTask(Context context, Device device) {
		super(context);
		mDevice = device;
	}

	@Override
	protected Boolean doInBackground(Pair<String, String> param) {
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		Controller controller = Controller.getInstance(mContext);
		DevicesModel devicesModel = controller.getDevicesModel();
		devicesModel.createParameter(mDevice, param.first, param.second);
		return true;
	}

	protected Boolean doInBackground(Device.DataPair pair) {
//
//		if (pair.location != null && pair.location.getId().equals(Location.NEW_LOCATION_ID)) {
//			// We need to save new location to server first
//			Location newLocation = controller.getLocationsModel().createLocation(pair.location);
//			if (newLocation == null)
//				return false;
//
//			pair.device.setLocationId(newLocation.getId());
//		}
//
//		DevicesModel devicesModel = controller.getDevicesModel();
//
//		// Save device data
//		devicesModel.saveDevice(pair.device);
//
//		// Don't save refresh during initializing
//		if (!pair.initializing) {
//			// Save refresh interval as actor switch
//			List<Module> refreshModules = pair.device.getModulesByType(ModuleType.TYPE_REFRESH.getTypeId());
//			if (!refreshModules.isEmpty()) {
//				devicesModel.switchActor(refreshModules.get(0));
//			}
//		}

		return true;
	}

}
