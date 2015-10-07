package com.rehivetech.beeeon.threading.task;

import android.content.Context;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.model.GatesModel;
import com.rehivetech.beeeon.threading.CallbackTask;

import java.util.List;
import java.util.Vector;

/**
 * Registers new gate. It automatically reloads list of gates and then we set this gate as active which also load all its modules.
 */
public class RegisterGateTask extends CallbackTask<Gate> {

	public RegisterGateTask(Context context) {
		super(context);
	}

	private String getUniqueGateName(String userName, List<Gate> gates) {
		Vector<String> gateNames = new Vector<>();

		for (Gate gate : gates) {
			gateNames.add(gate.getName());
		}

		String name;

		int number = 1;
		do {
			name = mContext.getString(R.string.task_register_gate_gate_default_name, userName, number++);
		} while (gateNames.contains(name));

		return name;
	}

	@Override
	protected Boolean doInBackground(Gate gate) {
		Controller controller = Controller.getInstance(mContext);
		GatesModel gatesModel = controller.getGatesModel();

		String serialNumber = gate.getId();
		String name = gate.getName().trim();

		// Set default name for this gate, if user didn't filled any
		if (!gate.hasName()) {
			name = getUniqueGateName(controller.getActualUser().getName(), gatesModel.getGates());
		}

		// Register new gate and set it as active
		if (gatesModel.registerGate(serialNumber, name, gate.getUtcOffset())) {
			controller.setActiveGate(serialNumber, true);
			return true;
		}

		return false;
	}

}
