package cz.vutbr.fit.iha.asynctask;

import android.content.Context;
import cz.vutbr.fit.iha.controller.Controller;

public class SwitchAdapterTask extends CallbackTask<String> {
	
	private Context mContext;
	
	private boolean mForceReload;
	
	public SwitchAdapterTask(Context context, boolean forceReload) {
		super();
		
		mContext = context;
		mForceReload = forceReload;
	}
	
	@Override
	protected Boolean doInBackground(String adapterId) {
		Controller controller = Controller.getInstance(mContext);

		return controller.setActiveAdapter(adapterId, mForceReload);
	}

}
