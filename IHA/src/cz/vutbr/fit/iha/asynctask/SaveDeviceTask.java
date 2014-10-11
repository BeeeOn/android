package cz.vutbr.fit.iha.asynctask;

import android.content.Context;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.pair.SaveDevicePair;

public class SaveDeviceTask extends CallbackTask<SaveDevicePair> {
	
	private Context mContext;
	
	public SaveDeviceTask(Context context) {
		super();
		mContext = context;
	}
	
	@Override
	protected Boolean doInBackground(SaveDevicePair pair) {
		Controller controller = Controller.getInstance(mContext);

		return controller.saveDevice(pair.device, pair.what);
	}

}
