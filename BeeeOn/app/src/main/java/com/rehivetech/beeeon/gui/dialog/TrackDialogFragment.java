package com.rehivetech.beeeon.gui.dialog;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.google.android.gms.analytics.Tracker;


public abstract class TrackDialogFragment extends DialogFragment {

	private Tracker tracker;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//		this.tracker = ((BeeeOnApplication)getActivity().getApplication()).getDefaultTracker();
	}

}
