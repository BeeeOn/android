package com.rehivetech.beeeon.household.watchdog;

import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.activity.spinnerItem.SpinnerItem;
import com.rehivetech.beeeon.household.device.Module;

/**
 * @author mlyko
 */
public class WatchdogSensorType extends WatchdogBaseType {
	public static final int[] operatorIcons = {
			R.drawable.ic_action_next_item,
			R.drawable.ic_action_previous_item
	};

	public static final String[] operatorCodes = {
			"gt",
			"lt"
	};

	public WatchdogSensorType() {
		super(WatchdogOperatorType.SENSOR, 0);
	}

	public WatchdogSensorType(int index) {
		super(WatchdogOperatorType.SENSOR, index);
	}

	@Override
	public int[] getAllIcons() {
		return operatorIcons;
	}

	@Override
	public String[] getAllCodes() {
		return operatorCodes;
	}

	@Override
	public void setupGUI(SpinnerItem selected, FloatingActionButton operatorButton, EditText ruleTreshold, TextView ruleTresholdUnit) {
		super.setupGUI(selected, operatorButton, ruleTreshold, ruleTresholdUnit);

		// shows necessary gui elements
		operatorButton.setVisibility(View.VISIBLE);
		ruleTreshold.setVisibility(View.VISIBLE);
		ruleTresholdUnit.setVisibility(View.VISIBLE);

		// senzors can have only numbers
		ruleTreshold.setInputType(InputType.TYPE_CLASS_NUMBER);

		if (mUnitsHelper != null) {
			Module selectedModule = (Module) selected.getObject();
			ruleTresholdUnit.setText(mUnitsHelper.getStringUnit(selectedModule.getValue()));
		}
	}

}
