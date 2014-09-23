/**
 * 
 */
package cz.vutbr.fit.iha.thread;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.activity.dialog.AddSensorActivityDialog;
import cz.vutbr.fit.iha.controller.Controller;

/**
 * @author ThinkDeep
 * 
 */
public class AdapterRegisterThread implements Runnable {

	private static final String TAG = AdapterRegisterThread.class.getSimpleName();

	private String mSerialNumber;
	private Activity mActivity;
	private String mName;

	/**
	 * Constructor
	 */
	public AdapterRegisterThread(String name, String serialNumber, Activity activity) {
		mSerialNumber = serialNumber;
		mActivity = activity;
		// TODO: name of adapter
		mName = (name.isEmpty())?"test": name;
	}

	@Override
	public void run() {
		Controller controller = Controller.getInstance(mActivity);

		int messageId;
		boolean result = controller.registerAdapter(mSerialNumber, mName);
		if (result) {
			messageId = R.string.toast_adapter_activated;
		} else {
			messageId = R.string.toast_adapter_activate_failed;
		}

		Log.d(TAG, mActivity.getString(messageId));
		new ToastMessageThread(mActivity, messageId).start();

		if (controller.getAdapter(mSerialNumber, true) != null ){// && controller.getAdapter(mSerialNumber, true).isEmpty()) {
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

		// uncomment this if you want to hide dialog after bad serial number
		// mActivity.finish();
	}

}
