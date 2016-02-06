package com.rehivetech.beeeon.threading.task;

import android.content.Context;
import android.util.Pair;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.model.DevicesModel;
import com.rehivetech.beeeon.threading.CallbackTask;

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
		Controller controller = Controller.getInstance(mContext);
		DevicesModel devicesModel = controller.getDevicesModel();
		return devicesModel.createParameter(mDevice, param.first, param.second);
	}
}
