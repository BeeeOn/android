package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.lzyzsd.circleprogress.DonutProgress;
import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.activity.BaseApplicationActivity;
import com.rehivetech.beeeon.gui.activity.SetupSensorActivity;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.task.PairRequestTask;
import com.rehivetech.beeeon.threading.task.ReloadGateDataTask;
import com.rehivetech.beeeon.util.Log;

import java.util.List;


public class AddSensorFragment extends TrackFragment {
	private static final String TAG = AddSensorFragment.class.getSimpleName();
	private static final String TIMER_VALUE_PAUSE = "AddSensorTimerValueOnPause";
	// private static final String TIMER_BOOL_PAUSE = "AddSensorTimerBooleanOnPause";
	private static final String KEY_GATE_ID = "Gate_ID";
	private static final int TIMER_SEC_COUNT = 30;
	private static final int CHECK_EVERY_X_SECONDS = 2;

	private boolean mFirstUse = true;

	private OnAddSensorListener mCallback;

	private String mGateId;
	private TextView mSendPairTextView;

	private DonutProgress mDonutProgress;
	private CountDownTimer mCountDownTimer;

	public static AddSensorFragment newInstance(String gateId) {
		AddSensorFragment fragment = new AddSensorFragment();

		Bundle args = new Bundle();
		args.putString(KEY_GATE_ID, gateId);
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mGateId = getArguments().getString(KEY_GATE_ID);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			// Get activity and controller
			mCallback = (OnAddSensorListener) getActivity();
		} catch (ClassCastException e) {
			throw new ClassCastException(String.format("%s must implement OnAddSensorListener", activity.toString()));
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_add_sensor_activity_dialog, container, false);

		mSendPairTextView = (TextView) view.findViewById(R.id.intro_image_text);
		mDonutProgress = (DonutProgress) view.findViewById(R.id.progress);
		resetTimer();

		return view;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constants.SETUP_SENSOR_REQUEST_CODE) {
			resetTimer();
			mCallback.onAddSensor(resultCode == Activity.RESULT_OK);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.d(TAG, "OnPause AddSensorDialog !!");
		//mTimerDone = true;
		if (mCountDownTimer != null)
			mCountDownTimer.cancel();
		/*if (mDonutProgress != null) {
			mDonutProgress.setInnerBottomText(getString(R.string.addsensor_stoped));
			//mDonutProgress.setTitle(" ");
		}*/
	}

	/*
		@Override
		public void onSaveInstanceState(Bundle outState) {
			super.onSaveInstanceState(outState);
			outState.putInt(TIMER_VALUE_PAUSE, mTimerValue);
			// outState.putBoolean(TIMER_BOOL_PAUSE, true);
		}
		*/
/*
	public void resetPairButton() {
		//mTimeText.setText("Time is out");
		if (mDonutProgress != null) {
			//mDonutProgress.setTitle(" ");
			mDonutProgress.setInnerBottomText("Time is out");
		}
		resetBtnPair();
	}
*/
	public void startTimer() {
		mCallback.setNextButtonEnabled(false);
		mDonutProgress.setProgress(TIMER_SEC_COUNT);
		mDonutProgress.setInnerBottomText(getString(R.string.addsensor_time_left_unit));
		mDonutProgress.setInnerBottomTextColor(getResources().getColor(R.color.beeeon_secundary_pink));
		mDonutProgress.setTextColor(getResources().getColor(R.color.beeeon_secundary_pink));
		mDonutProgress.setFinishedStrokeColor(getResources().getColor(R.color.beeeon_secundary_pink));
		mDonutProgress.setUnfinishedStrokeColor(getResources().getColor(R.color.white));
		mSendPairTextView.setText(R.string.activity_add_device_shake_it);

		// the timer should start counting down from 30, thats why + 500
		mCountDownTimer = new CountDownTimer(TIMER_SEC_COUNT * 1000 + 500, 500) {

			public void onTick(long millisUntilFinished) {
				int timerValue = (int) (millisUntilFinished / 1000);
				mDonutProgress.setProgress(timerValue);

				if (timerValue % CHECK_EVERY_X_SECONDS == 0) {
					// check new uninitialized devices
					doReloadUninitializedDevicesTask(mGateId, true);
				}
			}

			public void onFinish() {
				Toast.makeText(getActivity(), R.string.addsensor_device_not_found_in_time, Toast.LENGTH_LONG).show();
				resetTimer();
			}

		}.start();

	}

	public void doReloadUninitializedDevicesTask(String gateId, boolean forceReload) {
		ReloadGateDataTask reloadUninitializedDevicesTask = new ReloadGateDataTask(getActivity(), forceReload, ReloadGateDataTask.ReloadWhat.UNINITIALIZED_DEVICES);

		reloadUninitializedDevicesTask.setListener(new CallbackTask.ICallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				if (!success) {
					return;
				}

				List<Device> devices = Controller.getInstance(getActivity()).getUninitializedDevicesModel().getUninitializedDevicesByGate(mGateId);

				if (devices.size() > 0) {
					Toast.makeText(getActivity(), R.string.addsensor_device_found, Toast.LENGTH_LONG).show();

					mCountDownTimer.cancel();

					// go to setup uninit sensor
					Intent intent = new Intent(getActivity(), SetupSensorActivity.class);
					startActivityForResult(intent, Constants.SETUP_SENSOR_REQUEST_CODE);
				} else {
					if (mFirstUse) {
						mFirstUse = false;
						doPairRequestTask(mGateId);
					}
				}

			}

		});

		// Execute and remember task so it can be stopped automatically
		((BaseApplicationActivity) getActivity()).callbackTaskManager.executeTask(reloadUninitializedDevicesTask, gateId);
	}

	private void doPairRequestTask(String gateId) {
		mCallback.setNextButtonEnabled(false);
		// Send First automatic pair request
		PairRequestTask pairRequestTask = new PairRequestTask(getActivity());

		pairRequestTask.setListener(new CallbackTask.ICallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				//mCallback.setNextButtonEnabled(true);
				mDonutProgress.setInnerBottomText(getString(R.string.addsensor_time_left_unit));
				if (success) {
					// Request was successfully sent
					//startTimer();
				} else {
					// Request wasn't send
					//resetBtnPair();
					// TODO: Stop timer
					Toast.makeText(getActivity(), getString(R.string.addsensor_request_task_not_successful), Toast.LENGTH_LONG).show();
					resetTimer();
				}
			}

		});

		// Execute and remember task so it can be stopped automatically
		((BaseApplicationActivity) getActivity()).callbackTaskManager.executeTask(pairRequestTask, gateId);
	}

	public void doAction() {
		startTimer();
	}

	private void resetTimer() {
		mCallback.setNextButtonEnabled(true);
		mDonutProgress.setInnerBottomText(getString(R.string.addsensor_waiting));
		mDonutProgress.setInnerBottomTextColor(getResources().getColor(R.color.beeeon_drawer_bg));
		mDonutProgress.setTextColor(getResources().getColor(R.color.beeeon_drawer_bg));
		mDonutProgress.setProgress(TIMER_SEC_COUNT);
		mDonutProgress.setMax(TIMER_SEC_COUNT);
		mDonutProgress.setFinishedStrokeColor(getResources().getColor(R.color.white));
		mSendPairTextView.setText(R.string.activity_add_device_dialog_text);
	}

	public interface OnAddSensorListener {
		void onAddSensor(boolean success);

		void setNextButtonEnabled(boolean enabled);
	}
}
