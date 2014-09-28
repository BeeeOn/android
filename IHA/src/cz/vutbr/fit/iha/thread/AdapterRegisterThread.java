/**
 * 
 */
package cz.vutbr.fit.iha.thread;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.activity.dialog.AddSensorActivityDialog;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.controller.Controller;

/**
 * @author ThinkDeep
 * 
 */
public class AdapterRegisterThread implements Runnable {

	private static final String TAG = AdapterRegisterThread.class.getSimpleName();

	private Controller mController;
	
	private String mSerialNumber;
	private Activity mActivity;
	private String mName;

	/**
	 * Constructor
	 */
	public AdapterRegisterThread(String name, String serialNumber, Activity activity) {
		
		mController = Controller.getInstance(activity);
		mSerialNumber = serialNumber;
		mActivity = activity;
		mName = name;

		if (mName.isEmpty()) {
			// Set default name for this adapter
			int adaptersCount = mController.getAdapters().size();
			mName = activity.getString(R.string.adapter_default_name, adaptersCount + 1);	
		}
	}

	@Override
	public void run() {
		int messageId;
		boolean result = mController.registerAdapter(mSerialNumber, mName);
		if (result) {
			messageId = R.string.toast_adapter_activated;
		} else {
			messageId = R.string.toast_adapter_activate_failed;
		}

		Log.d(TAG, mActivity.getString(messageId));
		new ToastMessageThread(mActivity, messageId).start();

		if (result) {
			Adapter adapter = mController.getAdapter(mSerialNumber); 
			if (adapter != null && mController.getFacilitiesByAdapter(adapter.getId()).isEmpty()) {
				// Show activity for adding new sensor, when this adapter doesn't have any yet
				Log.i(TAG, mSerialNumber + " is empty");
				Intent intent = new Intent(mActivity, AddSensorActivityDialog.class);
				mActivity.startActivity(intent);
			}
			// TODO: this only from loginscreen
			// else
			// if(controller.isLoggedIn()){
			// LocationScreenActivity.healActivity();
			// Intent intent = new Intent(mActivity, LocationScreenActivity.class);
			// mActivity.startActivity(intent);
			// }
		} else {
			// uncomment this if you want to hide dialog after bad serial number
			// mActivity.finish();			
		}
	}

}
