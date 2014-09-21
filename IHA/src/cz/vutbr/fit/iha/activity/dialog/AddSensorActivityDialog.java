package cz.vutbr.fit.iha.activity.dialog;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;
import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.activity.LocationScreenActivity;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.adapter.device.Facility;
import cz.vutbr.fit.iha.controller.Controller;

public class AddSensorActivityDialog extends Activity {

	private Controller mController;

	// GUI elements
	private Button mButton;

	private CountDownTimer mCountDownTimer;
	private boolean mTimerDone = false;
	private int mTimerButtonSec = 30;
	private int mIntervalToCheckUninitSensor = 5;

	private PairRequestTask mPairRequestTask;

	private static final String TAG = LocationScreenActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_add_sensor_activity_dialog);

		mController = Controller.getInstance(this);

		initViews();

		// Send request
		Adapter actAdapter = mController.getActiveAdapter();
		if (actAdapter == null) {
			Toast.makeText(this, getResources().getString(R.string.toast_no_adapter), Toast.LENGTH_LONG).show();
			finish();
			return;
		}

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
		mCountDownTimer = new CountDownTimer(mTimerButtonSec * 1000, 1000) {

			public void onTick(long millisUntilFinished) {
				if (mTimerDone)
					return;

				mButton.setText(getResources().getString(R.string.addsensor_active_pair) + " 0:" + millisUntilFinished / 1000);
				if ((millisUntilFinished / 1000) % mIntervalToCheckUninitSensor == 0) {
					// check if are new uninit sensor
					Log.d(TAG, "PAIR - check if some uninit sensor");
					GetRefreshAdapterTask task = new GetRefreshAdapterTask();
					task.execute();
				}
			}

			public void onFinish() {
				// mButton.setText("done!");
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
			return mController.sendPairRequest(adapterID[0]);
			// return true;
		}

		@Override
		protected void onPostExecute(Boolean res) {
			if (res) { // Request was succesful send
				checkUninitSensors();
			} else { // Request wasnt send
				resetPairButton();
			}
		}
	}

	/**
	 * Changes selected location and redraws list of adapters there
	 */
	private class GetRefreshAdapterTask extends AsyncTask<Void, Void, List<Facility>> {

		@Override
		protected List<Facility> doInBackground(Void... params) {
			mController.reloadUninitializedFacilities(true);
			return mController.getUninitializedFacilities();
		}

		@Override
		protected void onPostExecute(List<Facility> facilities) {

			if (facilities.size() > 0) {
				// Setup variable as true for disable timer
				mTimerDone = true;
				mCountDownTimer.cancel();
				// Send count of sensors
				Bundle bundle = new Bundle();
				bundle.putInt(Constants.ADDSENSOR_COUNT_SENSOR, facilities.get(0).getDevices().size());
				// go to setup uninit sensor
				Intent intent = new Intent(AddSensorActivityDialog.this, SetupSensorActivityDialog.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				intent.putExtras(bundle);
				startActivity(intent);
				finish();
			}
		}
	}

}
