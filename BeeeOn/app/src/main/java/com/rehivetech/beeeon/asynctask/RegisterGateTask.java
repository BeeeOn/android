package com.rehivetech.beeeon.asynctask;

import android.content.Context;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.pair.RegisterGatePair;

import java.util.Locale;
import java.util.Vector;

/**
 * Registers new gate. It automatically reloads list of adapters and then we set this gate as active which also load all its sensors.
 */
public class RegisterGateTask extends CallbackTask<RegisterGatePair> {

	private Controller mController;

	public RegisterGateTask(Context context) {
		super(context);
	}

	private String getUniqueGateName() {
		Vector<String> adapterNames = new Vector<String>();

		for (Gate gate : mController.getGatesModel().getGates()) {
			adapterNames.add(gate.getName());
		}

		String name = "";

		int number = 1;
		do {
			name = mContext.getString(R.string.adapter_default_name, number++);
		} while (adapterNames.contains(name));

		return name;
	}

	private String getHexaAdapterName(String id) {
		try {
			int number = Integer.parseInt(id);
			return Integer.toHexString(number).toUpperCase(Locale.getDefault());
		} catch (NumberFormatException e) {
			return getUniqueGateName();
		}
	}

	@Override
	protected Boolean doInBackground(RegisterGatePair pair) {
		mController = Controller.getInstance(mContext);

		String serialNumber = pair.adapterId;
		String name = pair.adapterName.trim();

		// Set default name for this gate, if user didn't filled any
		if (name.isEmpty()) {
			// name = getUniqueGateName();
			name = getHexaAdapterName(serialNumber);
		}

		// Register new gate and set it as active
		if (mController.getGatesModel().registerGate(serialNumber, name)) {
			mController.setActiveAdapter(serialNumber, true);
			return true;
		}

		return false;
	}

}
