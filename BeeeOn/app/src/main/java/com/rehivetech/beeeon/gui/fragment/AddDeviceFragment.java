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
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.task.PairDeviceTask;
import com.rehivetech.beeeon.threading.task.ReloadGateDataTask;


public class AddDeviceFragment extends TrackFragment {
	private static final String TAG = AddDeviceFragment.class.getSimpleName();
	private static final String TIMER_VALUE_PAUSE = "AddSensorTimerValueOnPause";
	private static final String TIMER_BOOL_PAUSE = "AddSensorTimerBooleanOnPause";
	private static final String KEY_GATE_ID = "Gate_ID";
	private static final int TIMER_SEC_COUNT = 30;
	private boolean mFirsTime = true;

	private OnAddSensorListener mCallback;

	private String mGateId;
	private TextView mSendPairTextView;

	private DonutProgress mDonutProgress;
	private CountDownTimer mCountDownTimer;

	public static AddDeviceFragment newInstance(String gateId) {
		AddDeviceFragment fragment = new AddDeviceFragment();

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
		if (mCountDownTimer != null)
			mCountDownTimer.cancel();
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(TIMER_VALUE_PAUSE, mDonutProgress.getProgress());
		outState.putBoolean(TIMER_BOOL_PAUSE, mFirsTime);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState != null) {
			if (!savedInstanceState.getBoolean(TIMER_BOOL_PAUSE)) {
				mDonutProgress.setProgress(savedInstanceState.getInt(TIMER_VALUE_PAUSE));
				continueTimer();
			}
		}
	}

	public void continueTimer() {
		mCallback.setNextButtonEnabled(false);
		mCountDownTimer = new CountDownTimer(mDonutProgress.getProgress() * 1000 + 500, 500) {
			@Override
			public void onTick(long millisUntilFinished) {
				int timerValue = (int) (millisUntilFinished / 1000);
				mDonutProgress.setProgress(timerValue);
			}

			@Override
			public void onFinish() {
				Toast.makeText(getActivity(), R.string.addsensor_device_not_found_in_time, Toast.LENGTH_LONG).show();
				resetTimer();
			}
		}.start();

		doPairRequestTask(mDonutProgress.getProgress(), true);
	}

	private void doPairRequestTask(int timeLimit, boolean wasPaused) {
		// function creates and starts Task that handles pairing between the gate and the account
		PairDeviceTask pairDeviceTask = new PairDeviceTask(getActivity(), mGateId, timeLimit, wasPaused);
		pairDeviceTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if (success) {
					Toast.makeText(getActivity(), R.string.addsensor_device_found, Toast.LENGTH_LONG).show();

					mCountDownTimer.cancel();

					// go to setup uninit sensor
					Intent intent = new Intent(getActivity(), SetupSensorActivity.class);
					startActivityForResult(intent, Constants.SETUP_SENSOR_REQUEST_CODE);
				} else {
					Toast.makeText(getActivity(), R.string.addsensor_device_not_found_in_time, Toast.LENGTH_LONG).show();
					mCountDownTimer.cancel();
					resetTimer();
				}
			}
		});
		((BaseApplicationActivity) getActivity()).callbackTaskManager.executeTask(pairDeviceTask, mGateId);
	}

	public void startTimer() {
		mFirsTime = false;

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
			}

			public void onFinish() {
				Toast.makeText(getActivity(), R.string.addsensor_device_not_found_in_time, Toast.LENGTH_LONG).show();
				resetTimer();
			}

		}.start();

		doPairRequestTask(TIMER_SEC_COUNT, false);
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
