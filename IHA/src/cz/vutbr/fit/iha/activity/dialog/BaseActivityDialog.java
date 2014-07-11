package cz.vutbr.fit.iha.activity.dialog;

import android.app.Activity;

import com.google.analytics.tracking.android.EasyTracker;

public class BaseActivityDialog extends Activity
{

	@Override
	public void onStart() {
		super.onStart();

		EasyTracker.getInstance(this).activityStart(this);
	}

	@Override
	public void onStop() {
		super.onStop();

		EasyTracker.getInstance(this).activityStop(this);
	}

}
