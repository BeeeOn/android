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
		Controller ctrl = Controller.getInstance(mActivity);
		boolean result = ctrl.registerAdapter(mSerialNumber);
		Log.d("THREAD in adapter", result+"");
		if(result){
			ctrl.reloadAdapters();
			mActivity.runOnUiThread(new ToastMessageThread(mActivity, "Adapter has been activated."));
		}else{
			mActivity.runOnUiThread(new ToastMessageThread(mActivity, "Failed to activated adapter."));
		}
		
		if(!ctrl.isLoggedIn()){
			LocationScreenActivity.healActivity();
			Intent intent = new Intent(mActivity, LocationScreenActivity.class);
			mActivity.startActivity(intent);
		}
		
		mActivity.finish();
	}

}
