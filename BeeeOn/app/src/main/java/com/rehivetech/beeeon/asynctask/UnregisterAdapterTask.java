package com.rehivetech.beeeon.asynctask;

import android.content.Context;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.adapter.Adapter;

public class UnregisterAdapterTask extends CallbackTask<String> {

	public UnregisterAdapterTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(String adapterId) {
		Controller controller = Controller.getInstance(mContext);

		Adapter activeAdapter = controller.getActiveAdapter();

		// Unegister adapter and reset activeAdapter
		if (controller.getAdaptersModel().unregisterAdapter(adapterId, controller.getActualUser())) {
			if (activeAdapter != null && activeAdapter.getId().equals(adapterId)) {
				controller.setActiveAdapter("", false);
			}
			return true;
		}

		return false;
	}

}
