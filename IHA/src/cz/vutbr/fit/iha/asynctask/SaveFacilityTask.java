package cz.vutbr.fit.iha.asynctask;

import android.content.Context;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.pair.SaveFacilityPair;

public class SaveFacilityTask extends CallbackTask<SaveFacilityPair> {

	private Context mContext;

	public SaveFacilityTask(Context context) {
		super();
		mContext = context;
	}

	@Override
	protected Boolean doInBackground(SaveFacilityPair pair) {
		Controller controller = Controller.getInstance(mContext);

		return controller.saveFacility(pair.facility, pair.what);
	}

}
