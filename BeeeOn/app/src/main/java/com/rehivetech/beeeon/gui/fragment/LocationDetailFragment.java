package com.rehivetech.beeeon.gui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.util.Log;

public class LocationDetailFragment extends Fragment {

	private static final String TAG = LocationDetailFragment.class.getSimpleName();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Bundle bundle = this.getArguments();
		String locationID = bundle.getString("locationID");
		Log.d(TAG, "location id: " + locationID);

		View view = inflater.inflate(R.layout.activity_location_detail_screen, container, false);
		return view;
	}

}
