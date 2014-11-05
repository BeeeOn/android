package cz.vutbr.fit.iha.activity.dialog;

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.activity.MainActivity;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.adapter.device.Facility;
import cz.vutbr.fit.iha.asynctask.CallbackTask.CallbackTaskListener;
import cz.vutbr.fit.iha.asynctask.PairRequestTask;
import cz.vutbr.fit.iha.asynctask.ReloadUninitializedTask;
import cz.vutbr.fit.iha.base.TrackDialogFragment;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.util.Log;

public class AddSensorFragmentDialog extends TrackDialogFragment {

	public MainActivity mActivity;
	private View mView;
	private Controller mController;

	// GUI elements
	private Button mPosButton;
	private Button mNegButton;

	private CountDownTimer mCountDownTimer;
	private boolean mTimerDone = false;
	private boolean mTimerPause = false;
	private int mTimerButtonSec = 30;
	private int mIntervalToCheckUninitSensor = 2;
	private int mTimerValue = 0;

	private Adapter mAdapter;

	private PairRequestTask mPairRequestTask;
	private ReloadUninitializedTask mReloadUninitializedTask;

	private static final String TAG = MainActivity.class.getSimpleName();

	private static final String TIMER_VALUE_PAUSE = "AddSensorTimerValueOnPause";
	// private static final String TIMER_BOOL_PAUSE = "AddSensorTimerBooleanOnPause";

	private static final boolean DEBUG_MODE = false;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Get activity and controller
		mActivity = (MainActivity) getActivity();
		mController = Controller.getInstance(mActivity.getApplicationContext());

		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);

		LayoutInflater inflater = mActivity.getLayoutInflater();

		// Get View
		mView = inflater.inflate(R.layout.activity_add_sensor_activity_dialog, null);

		DialogInterface.OnClickListener dummyListener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// Do nothing here because we override this button later to change the close behaviour.
				// However, we still need this because on older versions of Android unless we
				// pass a handler the button doesn't get instantiated
			}
		};

		builder.setView(mView).setPositiveButton(R.string.notification_add, dummyListener).setNegativeButton(R.string.notification_cancel, dummyListener);

		// Send request
		mAdapter = mController.getActiveAdapter();
		if (mAdapter == null) {
			Toast.makeText(mActivity, getResources().getString(R.string.toast_no_adapter), Toast.LENGTH_LONG).show();
			// TODO: Ukoncit dialog ?
		}

		if (savedInstanceState != null) {
			mTimerButtonSec = savedInstanceState.getInt(TIMER_VALUE_PAUSE);
			Log.d(TAG, "Timer value: " + mTimerButtonSec);
		}

		// Create the AlertDialog object and return it
		return builder.create();

	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "OnResume AddSensorDialog !!");
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.d(TAG, "OnPause AddSensorDialog !!");
		mTimerDone = true;
		if (mCountDownTimer != null)
			mCountDownTimer.cancel();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(TIMER_VALUE_PAUSE, mTimerValue);
		// outState.putBoolean(TIMER_BOOL_PAUSE, true);
	}

	@Override
	public void onStart() {
		super.onStart();

		doPairRequestTask(mAdapter.getId());

		final AlertDialog dialog = (AlertDialog) getDialog();
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		if (dialog != null) {
			mPosButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
			mNegButton = dialog.getButton(Dialog.BUTTON_NEGATIVE);

			mPosButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// Set to RESEND pair request

				}
			});
			mPosButton.setEnabled(false);

			mNegButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mCountDownTimer != null) {
						mTimerDone = true;
						mCountDownTimer.cancel();
					}
					dialog.dismiss();
				}
			});
		}

	}

	private void doPairRequestTask(String adapterId) {
		// Send First automatic pair request
		mPairRequestTask = new PairRequestTask(getActivity().getApplicationContext());
		mPairRequestTask.setListener(new CallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				if (success) {
					// Request was successfully sent
					checkUninitSensors();
				} else {
					// Request wasn't send
					resetPairButton();
				}
			}

		});
		mPairRequestTask.execute(adapterId);
	}

	public void resetPairButton() {
		// Control if is dialog on screen
		if (getDialog() == null)
			return;
		mPosButton.setText(getResources().getString(R.string.addsensor_send_request));
		mPosButton.setEnabled(true);
		mPosButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (DEBUG_MODE) {
					startTimerOnButton();
				} else {
					doPairRequestTask(mAdapter.getId());
				}
				mPosButton.setEnabled(false);
			}
		});
	}

	public void startTimerOnButton() {
		mCountDownTimer = new CountDownTimer(mTimerButtonSec * 1000, 1000) {

			public void onTick(long millisUntilFinished) {
				if (mTimerDone)
					return;

				mTimerValue = (int) (millisUntilFinished / 1000);

				mPosButton.setText(getResources().getString(R.string.addsensor_active_pair) + " 0:" + ((mTimerValue < 10) ? "0" : "") + mTimerValue);
				if ((millisUntilFinished / 1000) % mIntervalToCheckUninitSensor == 0) {
					// check if are new uninit sensor
					Log.d(TAG, "PAIR - check if some uninit sensor");
					doReloadUninitializedFacilitiesTask(mAdapter.getId());
				}
			}

			public void onFinish() {
				// mButton.setText("done!");
				resetPairButton();
			}
		}.start();

	}

	public void checkUninitSensors() {
		// Control if is dialog on screen
		if (getDialog() == null)
			return;
		// GOTO next dialog to setup sensors
		startTimerOnButton();
	}

	public void doReloadUninitializedFacilitiesTask(String adapterId) {
		mReloadUninitializedTask = new ReloadUninitializedTask(getActivity().getApplicationContext());

		mReloadUninitializedTask.setListener(new CallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				if (!success) {
					return;
				}

				List<Facility> facilities = mController.getUninitializedFacilities(mAdapter.getId(), false);

				if (facilities.size() > 0) {
					if (mCountDownTimer != null) {
						// Setup variable as true for disable timer
						mTimerDone = true;
						mCountDownTimer.cancel();
					}
					Log.d(TAG, "Nasel jsem neinicializovane zarizeni !!!!");

					// go to setup uninit sensor
					DialogFragment newFragment = new SetupSensorFragmentDialog();
					newFragment.show(mActivity.getSupportFragmentManager(), "SetupSensor");
					getDialog().dismiss();

					// Intent intent = new Intent(AddSensorFragmentDialog.this, SetupSensorActivityDialog.class);
					// intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
					// intent.putExtras(bundle);
					// startActivity(intent);
					// finish();
				}
			}

		});

		mReloadUninitializedTask.execute(adapterId);
	}

}
