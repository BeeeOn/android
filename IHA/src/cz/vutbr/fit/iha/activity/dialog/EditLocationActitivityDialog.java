package cz.vutbr.fit.iha.activity.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import com.google.analytics.tracking.android.EasyTracker;

import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.activity.LocationScreenActivity;
import cz.vutbr.fit.iha.controller.Controller;

public class EditLocationActitivityDialog extends Activity {
	
	private Controller mController;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		mController = Controller.getInstance(this);
		
		setContentView(R.layout.activity_edit_location_activity_dialog);
	}
	
	@Override
	public void onBackPressed() {
		LocationScreenActivity.healActivity();
		this.finish();
	}
	
	@Override
	public void onStart() {
		super.onStart();
		// The rest of your onStart() code.
		EasyTracker.getInstance(this).activityStart(this); // Add this method.
	}

	@Override
	public void onStop() {
		super.onStop();
		// The rest of your onStop() code.
		EasyTracker.getInstance(this).activityStop(this); // Add this method.
	}

}
