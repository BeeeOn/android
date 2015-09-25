package com.rehivetech.beeeon.threading.task;

import android.content.Context;
import android.support.annotation.Nullable;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.threading.CallbackTask;

public class AddLocationTask extends CallbackTask<Location> {

	@Nullable
	private Location mNewLocation = null;

	public AddLocationTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(Location location) {
		Controller controller = Controller.getInstance(mContext);

		mNewLocation = controller.getLocationsModel().createLocation(location);

		return mNewLocation != null;
	}

	@Nullable
	public Location getNewLocation() {
		return mNewLocation;
	}

}
