package com.rehivetech.beeeon.asynctask;

import android.content.Context;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.User;
import com.rehivetech.beeeon.pair.AddUserPair;
import com.rehivetech.beeeon.util.Log;

/**
 * Reloads facilities by adapter
 */
public class AddAdapterUserTask extends CallbackTask<AddUserPair> {

	private final Context mContext;

	private final boolean mForceReload;

	public AddAdapterUserTask(Context context, boolean forceReload) {
		super();

		mContext = context;
		mForceReload = forceReload;
	}

	@Override
	protected Boolean doInBackground(AddUserPair pair) {
		Controller controller = Controller.getInstance(mContext);
		boolean result = false;
		try{
			result = controller.addUser(pair.adapter.getId(), pair.user); 
		}
		catch(Exception e){
			Log.e(">/]", e.getMessage()+""); //FIXME: !! message is null? tell user where is problem some days
		}
		return result;
	}

}
