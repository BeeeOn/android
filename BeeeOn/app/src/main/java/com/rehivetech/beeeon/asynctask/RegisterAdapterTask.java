package com.rehivetech.beeeon.asynctask;

import android.content.Context;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.pair.RegisterAdapterPair;

import java.util.Locale;
import java.util.Vector;

/**
 * Registers new gate. It automatically reloads list of adapters and then we set this gate as active which also load all its sensors.
 */
public class RegisterAdapterTask extends CallbackTask<RegisterAdapterPair> {

	private Controller mController;

	public RegisterAdapterTask(Context context) {
		super(context);
	}

	private String getUniqueAdapterName() {
		Vector<String> adapterNames = new Vector<String>();

		for (Gate gate : mController.getAdaptersModel().getAdapters()) {
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
			return getUniqueAdapterName();
		}
	}

	@Override
	protected Boolean doInBackground(RegisterAdapterPair pair) {
		mController = Controller.getInstance(mContext);

		String serialNumber = pair.adapterId;
		String name = pair.adapterName.trim();

		// Set default name for this gate, if user didn't filled any
		if (name.isEmpty()) {
			// name = getUniqueAdapterName();
			name = getHexaAdapterName(serialNumber);
		}

		// Register new gate and set it as active
		if (mController.getAdaptersModel().registerAdapter(serialNumber, name)) {
			mController.setActiveAdapter(serialNumber, true);
			return true;
		}

		return false;
	}

}
