package com.rehivetech.beeeon.threading.task;

import android.content.Context;
import android.content.Intent;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.threading.CallbackTask;

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
		actionIntent.putExtra(Constants.BROADCAST_EXTRA_ACTOR_CHANGED_ID, module.getAbsoluteId());
		actionIntent.putExtra(Constants.BROADCAST_EXTRA_ACTOR_CHANGED_GATE_ID, module.getDevice().getGateId());
		mContext.sendBroadcast(actionIntent);
	}
}
