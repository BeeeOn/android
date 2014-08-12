package cz.vutbr.fit.iha.activity.dialog;

import android.os.Bundle;
import android.view.Window;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.controller.Controller;

public class AddSensorActivityDialog extends BaseActivityDialog{
	
	private Controller mController;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_add_sensor_activity_dialog);
		
		mController = Controller.getInstance(this);
		
		
	}
	

}
