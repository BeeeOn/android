package cz.vutbr.fit.iha.activity.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.util.Log;

//import cz.vutbr.fit.iha.activity.SensorDetailFragment.GetDeviceTask;

public class LocationDetailFragment extends SherlockFragment {

	private static final String TAG = LocationDetailFragment.class.getSimpleName();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Bundle bundle = this.getArguments();
		String locationID = bundle.getString("locationID");
		Log.d(TAG, "location id: " + locationID);

		// GetDeviceTask task = new GetDeviceTask();
		// task.execute(new String[] { sensorID });

		View view = inflater.inflate(R.layout.activity_location_detail_screen, container, false);
		return view;
	}

}