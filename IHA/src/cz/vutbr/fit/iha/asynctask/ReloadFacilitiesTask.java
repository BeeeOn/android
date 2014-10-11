package cz.vutbr.fit.iha.asynctask;

import android.content.Context;
import cz.vutbr.fit.iha.controller.Controller;

/**
 * Reloads facilities by adapter
 */
public class ReloadFacilitiesTask extends CallbackTask<String> {
	
	private final Context mContext;
	
	private final boolean mForceReload;
	
	public ReloadFacilitiesTask(Context context, boolean forceReload) {
		super();
		
		mContext = context;
		mForceReload = forceReload;
	}
	
	@Override
	protected Boolean doInBackground(String adapterId) {
		Controller controller = Controller.getInstance(mContext);
		
		return controller.reloadFacilitiesByAdapter(adapterId, mForceReload);
	}

}
