/**
 * 
 */
package cz.vutbr.fit.iha.thread;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import cz.vutbr.fit.iha.activity.LocationScreenActivity;
import cz.vutbr.fit.iha.controller.Controller;

/**
 * @author ThinkDeep
 *
 */
public class AdapterRegisterThread implements Runnable{

	private static final String TAG = AdapterRegisterThread.class.getSimpleName();

	private String mSerialNumber;
	private Activity mActivity;
	
	/**
	 * Constructor
	 */
	public AdapterRegisterThread(String serialNumber, Activity activity) {
		mSerialNumber = serialNumber;
		mActivity = activity;
	}

	@Override
	public void run() {
		Controller controller = Controller.getInstance(mActivity);
		
		String message;
		if (controller.registerAdapter(mSerialNumber)) {
			message = "Adapter has been activated.";
		} else {
			message = "Failed to activate adapter.";
		}
		
		Log.d(TAG, message);
		mActivity.runOnUiThread(new ToastMessageThread(mActivity, message));
		
		if(!controller.isLoggedIn()){
			LocationScreenActivity.healActivity();
			Intent intent = new Intent(mActivity, LocationScreenActivity.class);
			mActivity.startActivity(intent);
		}
		
		mActivity.finish();
	}

}
