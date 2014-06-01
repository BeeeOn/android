/**
 * 
 */
package cz.vutbr.fit.intelligenthomeanywhere.adapter;

import android.app.Activity;
import android.util.Log;
import cz.vutbr.fit.intelligenthomeanywhere.controller.Controller;

/**
 * @author ThinkDeep
 *
 */
public class AdapterRegisterThred implements Runnable{

	private String mSerialNumber;
	private Activity mActivity;
	
	/**
	 * Constructor
	 */
	public AdapterRegisterThred(String serialNumber, Activity activity) {
		mSerialNumber = serialNumber;
		mActivity = activity;
	}

	@Override
	public void run() {
		boolean result = Controller.getInstance(mActivity).registerAdapter(mSerialNumber);
		Log.d("THREAD in adapter", result+"");
		
//		
//		mActivity.runOnUiThread(new Runnable() {
//			@Override
//			public void run() {
//				int i;
//			}
//		}
	}

}
