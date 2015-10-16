package com.rehivetech.beeeon.gui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.google.android.gms.analytics.Tracker;
import com.rehivetech.beeeon.BeeeOnApplication;


public abstract class TrackFragment extends Fragment {

	private Tracker tracker;

	@Override
	public void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		this.tracker = ((BeeeOnApplication)getActivity().getApplication()).getDefaultTracker();
	}

}
