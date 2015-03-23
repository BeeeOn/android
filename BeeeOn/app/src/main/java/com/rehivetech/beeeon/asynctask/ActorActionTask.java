package com.rehivetech.beeeon.asynctask;

import android.content.Context;
import com.rehivetech.beeeon.adapter.device.Device;
import com.rehivetech.beeeon.controller.Controller;

public class ActorActionTask extends CallbackTask<Device> {

	public ActorActionTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(Device device) {
		Controller controller = Controller.getInstance(mContext);

		return controller.switchActorValue(device);
	}

}
