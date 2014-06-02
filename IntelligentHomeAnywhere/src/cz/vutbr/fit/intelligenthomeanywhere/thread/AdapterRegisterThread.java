/**
 * 
 */
package cz.vutbr.fit.intelligenthomeanywhere.thread;

import android.app.Activity;
import android.util.Log;
import cz.vutbr.fit.intelligenthomeanywhere.controller.Controller;

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
		}
		mActivity.finish();
	}

}
