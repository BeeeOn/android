package com.rehivetech.beeeon.gcm;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.activity.DeviceDetailActivity;
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
	static public void getModuleDetailIntent(Context context, String gateId, String deviceId, String moduleId) {
		Controller controller = Controller.getInstance(context);
		Device device = controller.getDevicesModel().getDevice(gateId, deviceId);
		if (device == null) {
			Toast.makeText(context, R.string.module_get_detail_intent_toast_device_not_available, Toast.LENGTH_SHORT).show();
			return;
		}

		Module module = device.getModuleById(moduleId);
		if (module == null) {
			Toast.makeText(context, R.string.module_get_detail_intent_toast_device_not_available, Toast.LENGTH_SHORT).show();
			return;
		}

		// Module exists, we can open activity
		Intent intent = new Intent(context, DeviceDetailActivity.class);
		intent.putExtra(DeviceDetailActivity.EXTRA_GATE_ID, String.valueOf(gateId));
		intent.putExtra(DeviceDetailActivity.EXTRA_DEVICE_ID, device.getId());
		intent.putExtra(DeviceDetailActivity.EXTRA_MODULE_ID, module.getId());

		context.startActivity(intent);
	}
}
