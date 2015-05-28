package com.rehivetech.beeeon.gui.fragment;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

public abstract class TrackDialogFragment extends DialogFragment {

	private Tracker tracker;

	@Override
	public void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		this.tracker = EasyTracker.getInstance(this.getActivity());
	}

	@Override
	public void onResume() {

		super.onResume();

		this.tracker.set(Fields.SCREEN_NAME, getClass().getSimpleName());
		this.tracker.send(MapBuilder.createAppView().build());
	}

}
