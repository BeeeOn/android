package com.rehivetech.beeeon.gcm;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.activity.DeviceDetailActivity;
import com.rehivetech.beeeon.gui.activity.ModuleDetailActivity;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;

import java.util.List;

/**
 * Created by Martin on 6. 5. 2015.
 */
public final class Action {

	private Action() {
	}

	@Nullable
	static public void getModuleDetailIntent(Context context, int gateId, String moduleId, int type) {
		Controller controller = Controller.getInstance(context);
		Device device = controller.getDevicesModel().getDevice(String.valueOf(gateId), moduleId);
		if (device == null) {
			Toast.makeText(context, R.string.module_get_detail_intent_toast_device_not_available, Toast.LENGTH_SHORT).show();
			return;
		}


		List<Module> modules = device.getAllModules();
		if (modules.size() == 0) {
			Toast.makeText(context, R.string.module_get_detail_intent_toast_device_not_available, Toast.LENGTH_SHORT).show();
			return;
		}

		int pos = 0;
		for (int i = 0; i < modules.size(); i++) {
			// FIXME: now it is comparing id with type, which won't work at all
			if (modules.get(i).getAbsoluteId().equals(String.valueOf(type))) {

				pos = i;
				break;
			}
		}

		Module module = modules.get(pos);

		// Module exists, we can open activity
		Intent intent = new Intent(context, DeviceDetailActivity.class);
		intent.putExtra(ModuleDetailActivity.EXTRA_MODULE_ID, module.getDevice().getId());
		intent.putExtra(ModuleDetailActivity.EXTRA_GATE_ID, String.valueOf(gateId));

		context.startActivity(intent);
	}
}
