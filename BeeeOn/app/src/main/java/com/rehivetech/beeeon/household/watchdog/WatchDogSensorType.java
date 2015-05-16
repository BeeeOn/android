package com.rehivetech.beeeon.household.watchdog;

import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.activity.spinnerItem.SpinnerItem;
import com.rehivetech.beeeon.household.device.Device;

/**
 * @author mlyko
 */
public class WatchDogSensorType extends WatchDogBaseType {
	public static final int[] operatorIcons = {
			R.drawable.ic_action_next_item,
			R.drawable.ic_action_previous_item
	};

	public static final String[] operatorCodes = {
			"gt",
			"lt"
	};

	public WatchDogSensorType(){
		super(WatchDogOperatorType.SENSOR, 0);
	}
	public WatchDogSensorType(int index){
		super(WatchDogOperatorType.SENSOR, index);
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

		if(mUnitsHelper != null){
			Device selectedDevice = (Device) selected.getObject();
			ruleTresholdUnit.setText(mUnitsHelper.getStringUnit(selectedDevice.getValue()));
		}
	}

}
