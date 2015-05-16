package com.rehivetech.beeeon.household.watchdog;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.activity.spinnerItem.SpinnerItem;

/**
 * @author mlyko
 */
public class WatchDogGeofenceType extends WatchDogBaseType {
	public static final int[] operatorIcons = {
		R.drawable.ic_in,
		R.drawable.ic_out
	};

	public static final String[] operatorCodes = {
		"in",
		"out"
	};

	public WatchDogGeofenceType(){
		super(WatchDogOperatorType.GEOFENCE, 0);
	}
	public WatchDogGeofenceType(int index){
		super(WatchDogOperatorType.GEOFENCE, index);
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
