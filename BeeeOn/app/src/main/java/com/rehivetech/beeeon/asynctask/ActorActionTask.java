package com.rehivetech.beeeon.asynctask;

import android.content.Context;
import android.content.Intent;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.device.Module;

public class ActorActionTask extends CallbackTask<Module> {

	public ActorActionTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(Module module) {
		Controller controller = Controller.getInstance(mContext);
		boolean success = controller.getDevicesModel().switchActor(module);
		sendActorChangedBroadcast(module);
		return success;
	}

	/**
	 * Used for immediately refresh widgets
	 *
	 * @param module
	 */
	private void sendActorChangedBroadcast(Module module) {
		Intent actionIntent = new Intent(Constants.BROADCAST_ACTOR_CHANGED);
		actionIntent.putExtra(Constants.BROADCAST_EXTRA_ACTOR_CHANGED_ID, module.getId());
		actionIntent.putExtra(Constants.BROADCAST_EXTRA_ACTOR_CHANGED_ADAPTER_ID, module.getDevice().getAdapterId());
		mContext.sendBroadcast(actionIntent);
	}
}
