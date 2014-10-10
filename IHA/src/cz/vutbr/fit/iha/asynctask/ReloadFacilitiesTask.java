package cz.vutbr.fit.iha.asynctask;

import android.content.Context;
import cz.vutbr.fit.iha.controller.Controller;

/**
 * Reloads facilities by adapter
 */
public class ReloadFacilitiesTask extends CallbackTask<String> {
	
	private Context mContext;
	
	public ReloadFacilitiesTask(Context context) {
		super();
		mContext = context;
	}
	
	@Override
	protected Boolean doInBackground(String adapterId) {
		Controller controller = Controller.getInstance(mContext);
		
		return controller.reloadFacilitiesByAdapter(adapterId, true);
	}

}
