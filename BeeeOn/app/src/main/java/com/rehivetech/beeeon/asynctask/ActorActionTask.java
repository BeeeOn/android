package com.rehivetech.beeeon.asynctask;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.controller.Controller;

public class ActorActionTask extends CallbackTask<Device> {
	public static final String ACTION_ACTOR_CHANGED = "com.rehivetech.beeeon.ACTION_ACTOR_CHANGED";
	public static final String EXTRA_ACTOR_CHANGED_ID = "com.rehivetech.beeeon.EXTRA_ACTION_CHANGED_ID";
	public static final String EXTRA_ACTOR_CHANGED_ADAPTER_ID = "com.rehivetech.beeeon.EXTRA_ACTOR_CHANGED_ADAPTER_ID";

	public ActorActionTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(Device device) {
		Controller controller = Controller.getInstance(mContext);

		SystemClock.sleep(4000);
		boolean success = controller.getFacilitiesModel().switchActor(device);
		sendActorChangedBroadcast(device);
		return success;
	}

	/**
	 * Used for immediately refresh widgets
	 * @param device
	 */
	private void sendActorChangedBroadcast(Device device){
		Intent actionIntent = new Intent(ACTION_ACTOR_CHANGED);
		actionIntent.putExtra(EXTRA_ACTOR_CHANGED_ID, device.getId());
		actionIntent.putExtra(EXTRA_ACTOR_CHANGED_ADAPTER_ID, device.getFacility().getAdapterId());
		mContext.sendBroadcast(actionIntent);
	}
}
