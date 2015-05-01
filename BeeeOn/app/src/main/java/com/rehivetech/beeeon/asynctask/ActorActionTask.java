package com.rehivetech.beeeon.asynctask;

import android.content.Context;
import android.content.Intent;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.controller.Controller;

public class ActorActionTask extends CallbackTask<Device> {

	public ActorActionTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(Device device) {
		Controller controller = Controller.getInstance(mContext);
		boolean success = controller.getFacilitiesModel().switchActor(device);
		sendActorChangedBroadcast(device);
		return success;
	}

	/**
	 * Used for immediately refresh widgets
	 * @param device
	 */
	private void sendActorChangedBroadcast(Device device){
		Intent actionIntent = new Intent(Constants.BROADCAST_ACTOR_CHANGED);
		actionIntent.putExtra(Constants.BROADCAST_EXTRA_ACTOR_CHANGED_ID, device.getId());
		actionIntent.putExtra(Constants.BROADCAST_EXTRA_ACTOR_CHANGED_ADAPTER_ID, device.getFacility().getAdapterId());
		mContext.sendBroadcast(actionIntent);
	}
}
