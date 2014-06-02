/**
 * 
 */
package cz.vutbr.fit.intelligenthomeanywhere.thread;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

/**
 * @author ThinkDeep
 *
 */
public class ToastMessageThread implements Runnable{

	private String mMessage;
	private Activity mActivity;
	
	/**
	 * 
	 */
	public ToastMessageThread(Activity activity, String message) {
		mActivity = activity;
		mMessage = message;
	}

	@Override
	public void run() {
		Log.d("ToastMessageThred", mMessage);
		Toast.makeText(mActivity, mMessage, Toast.LENGTH_LONG).show();
	}

}
