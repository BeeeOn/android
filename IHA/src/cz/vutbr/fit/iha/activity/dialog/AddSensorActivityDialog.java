package cz.vutbr.fit.iha.activity.dialog;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.controller.Controller;

public class AddSensorActivityDialog extends BaseActivityDialog{
	
	private Controller mController;
	
	// GUI elements
	private Button mButton;
	
	private CountDownTimer mCountDownTimer;
	private int mTimerButtonSec = 15;
	private int mIntervalToCheckUninitSensor = 5;
	
	private PairRequestTask mPairRequestTask;
	
	//private BaseDevice mNewDevice;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_add_sensor_activity_dialog);
		
		mController = Controller.getInstance(this);
		
		
		initViews();
		// Send request
		Adapter actAdapter = mController.getActiveAdapter();
		mPairRequestTask = new PairRequestTask();
		mPairRequestTask.execute(actAdapter.getId());
	}
	
	private void initViews() {
		mButton = (Button) findViewById(R.id.dialog_addsensor_btn);
		mButton.setText(getResources().getString(R.string.addsensor_sending_request));
	}
	
	
	
	
	public void resetPairButton() {
		mButton.setText(getResources().getString(R.string.addsensor_send_request));
		mButton.setEnabled(true);
		mButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startTimerOnButton();
				mButton.setEnabled(false);
			}
		});
	}
	
	public void startTimerOnButton() {
		mCountDownTimer = new CountDownTimer(mTimerButtonSec*1000, 1000) {

		     public void onTick(long millisUntilFinished) {
		         mButton.setText(getResources().getString(R.string.addsensor_active_pair) +" 0:"+ millisUntilFinished / 1000);
		         if( (millisUntilFinished / 1000)% mIntervalToCheckUninitSensor == 0 ){
		        	 // check if are new uninit sensor
		        	 if(mController.getUninitializedDevices().size() > 0 ) {
		        		 mCountDownTimer.cancel();
		        		 // go to setup uninit sensor
		        		 
		        	 }
		        		 
		         }
		     }

		     public void onFinish() {
		    	 //mButton.setText("done!");
		    	 resetPairButton();
		     }
		  }.start();

	}
	
	public void checkUninitSensors() {
		// GOTO next dialog to setup sensors
		startTimerOnButton();
	}
	
	/**
	 * Send pair request
	 */
	private class PairRequestTask extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... adapterID) {
			//return mController.sendPairRequest(adapterID[0]);
			return true;
		}

		@Override
		protected void onPostExecute(Boolean res) {
			if(res) { // Request was succesful send
				checkUninitSensors();
			}
			else { // Request wasnt send
				resetPairButton();
			}
		}

		
	}



}
