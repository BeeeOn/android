package cz.vutbr.fit.iha.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

import cz.vutbr.fit.iha.R;
//import cz.vutbr.fit.iha.activity.SensorDetailFragment.GetDeviceTask;
import cz.vutbr.fit.iha.controller.Controller;

public class LocationDetailFragment extends SherlockFragment {

	private Controller mController;
	private static final String TAG = "SensorDetail";
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Get controller
		mController = Controller.getInstance(getActivity());

		Bundle bundle = this.getArguments();
		String sensorID = bundle.getString("sensorID");

		
		//GetDeviceTask task = new GetDeviceTask();
		//task.execute(new String[] { sensorID });

		View view = inflater.inflate(R.layout.activity_sensor_detail_screen,
				container, false);
		return view;
	}
	
	
}
