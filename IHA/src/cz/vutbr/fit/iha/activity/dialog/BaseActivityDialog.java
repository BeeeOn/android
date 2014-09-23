package cz.vutbr.fit.iha.activity.dialog;

import android.app.Activity;
import android.support.v4.app.DialogFragment;

import com.google.analytics.tracking.android.EasyTracker;

public class BaseActivityDialog extends DialogFragment {

	@Override
	public void onStart() {
		super.onStart();

		EasyTracker.getInstance(getActivity()).activityStart(getActivity());
	}

	@Override
	public void onStop() {
		super.onStop();

		EasyTracker.getInstance(getActivity()).activityStart(getActivity());
	}

}
