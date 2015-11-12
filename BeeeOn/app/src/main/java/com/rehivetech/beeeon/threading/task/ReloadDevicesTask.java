package com.rehivetech.beeeon.threading.task;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.threading.CallbackTask;

import java.util.List;

public class ReloadDevicesTask extends CallbackTask<List<Device>> {

	private final boolean mForceReload;

	public ReloadDevicesTask(Context context, boolean forceReload) {
		super(context);

		mForceReload = forceReload;
	}

	@Override
	protected Boolean doInBackground(List<Device> devices) {
		Controller controller = Controller.getInstance(mContext);

		return controller.getDevicesModel().refreshDevices(devices, mForceReload);
	}

}
