package cz.vutbr.fit.iha.activity;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

public abstract class TrackFragment extends SherlockFragment {

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
