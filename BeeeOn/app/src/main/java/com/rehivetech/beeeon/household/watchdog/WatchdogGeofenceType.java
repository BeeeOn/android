package com.rehivetech.beeeon.household.watchdog;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.spinnerItem.SpinnerItem;

/**
 * @author mlyko
 */
public class WatchdogGeofenceType extends WatchdogBaseType {
	public static final int[] operatorIcons = {
			R.drawable.ic_in,
			R.drawable.ic_out
	};

	public static final String[] operatorCodes = {
			"in",
			"out"
	};

	public WatchdogGeofenceType() {
		super(WatchdogOperatorType.GEOFENCE, 0);
	}

	public WatchdogGeofenceType(int index) {
		super(WatchdogOperatorType.GEOFENCE, index);
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
		ruleTreshold.setVisibility(View.GONE);
		ruleTresholdUnit.setVisibility(View.GONE);
	}

}
