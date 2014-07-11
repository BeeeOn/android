package cz.vutbr.fit.iha.activity.dialog;

import android.os.Bundle;
import android.view.Window;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.activity.LocationScreenActivity;

public class EditLocationActitivityDialog extends BaseActivityDialog
{
	//private Controller mController;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		//mController = Controller.getInstance(this);
		
		setContentView(R.layout.activity_edit_location_activity_dialog);
	}
	
	@Override
	public void onBackPressed() {
		LocationScreenActivity.healActivity();
		this.finish();
	}

}
