package cz.vutbr.fit.iha.asynctask;

import android.content.Context;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.household.User;
import cz.vutbr.fit.iha.pair.AddUserPair;
import cz.vutbr.fit.iha.util.Log;

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
