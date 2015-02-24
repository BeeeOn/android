package com.rehivetech.beeeon.asynctask;

import android.content.Context;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.pair.SaveFacilityPair;

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
