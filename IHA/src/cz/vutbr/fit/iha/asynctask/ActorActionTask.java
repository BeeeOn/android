package cz.vutbr.fit.iha.asynctask;

import android.content.Context;
import cz.vutbr.fit.iha.adapter.device.Device;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.pair.SaveDevicePair;

public class ActorActionTask extends CallbackTask<Device> {

	private Context mContext;

	public ActorActionTask(Context context) {
		super();
		mContext = context;
	}

	@Override
	protected Boolean doInBackground(Device device) {
		Controller controller = Controller.getInstance(mContext);

		return controller.switchActorValue(device);
	}

}
