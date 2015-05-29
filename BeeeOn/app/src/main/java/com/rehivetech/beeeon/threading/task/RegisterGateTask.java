package com.rehivetech.beeeon.threading.task;

import android.content.Context;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.threading.CallbackTask;

import java.util.Locale;
import java.util.Vector;

/**
 * Registers new gate. It automatically reloads list of adapters and then we set this gate as active which also load all its sensors.
 */
public class RegisterGateTask extends CallbackTask<Gate> {

	private Controller mController;

	public RegisterGateTask(Context context) {
		super(context);
	}

	private String getUniqueGateName() {
		Vector<String> gateNames = new Vector<String>();

		for (Gate gate : mController.getGatesModel().getGates()) {
			gateNames.add(gate.getName());
		}

		String name = "";

		int number = 1;
		do {
			name = mContext.getString(R.string.adapter_default_name, number++);
		} while (gateNames.contains(name));

		return name;
	}

	private String getHexaGateName(String id) {
		try {
			int number = Integer.parseInt(id);
			return Integer.toHexString(number).toUpperCase(Locale.getDefault());
		} catch (NumberFormatException e) {
			return getUniqueGateName();
		}
	}

	@Override
	protected Boolean doInBackground(Gate gate) {
		mController = Controller.getInstance(mContext);

		String serialNumber = gate.getId();
		String name = gate.getName().trim();

		// Set default name for this gate, if user didn't filled any
		if (name.isEmpty()) {
			// name = getUniqueGateName();
			name = getHexaGateName(serialNumber);
		}

		// Register new gate and set it as active
		if (mController.getGatesModel().registerGate(serialNumber, name)) {
			mController.setActiveGate(serialNumber, true);
			return true;
		}

		return false;
	}

}
