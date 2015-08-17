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
import com.rehivetech.beeeon.gui.activity.BaseApplicationActivity;
import com.rehivetech.beeeon.gui.activity.SetupDeviceActivity;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.CallbackTaskManager;
import com.rehivetech.beeeon.threading.task.PairDeviceTask;
import com.rehivetech.beeeon.util.Log;


public class AddDeviceFragment extends TrackFragment {
	private static final String TAG = AddDeviceFragment.class.getSimpleName();
	private static final String TIMER_VALUE_PAUSE = "AddDeviceTimerValueOnPause";
	private static final String KEY_GATE_ID = "Gate_ID";

	private long mStartTimeMSec = 0;

	private OnAddDeviceListener mCallback;

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
			mCallback = (OnAddDeviceListener) getActivity();
		} catch (ClassCastException e) {
			throw new ClassCastException(String.format("%s must implement OnAddSensorListener", activity.toString()));
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_device_add, container, false);

		mSendPairTextView = (TextView) view.findViewById(R.id.device_add_intro_image_text);
		mDonutProgress = (DonutProgress) view.findViewById(R.id.device_add_circle_progress);
		resetTimer();

		return view;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constants.SETUP_DEVICE_REQUEST_CODE) {
			resetTimer();
			mCallback.onAddDevice(resultCode == Activity.RESULT_OK);
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
		outState.putLong(TIMER_VALUE_PAUSE, mStartTimeMSec);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState != null) {
			mStartTimeMSec = savedInstanceState.getLong(TIMER_VALUE_PAUSE);
			if (mStartTimeMSec != 0)
				continueTimer();
		}
	}

	public void continueTimer() {
		mCallback.setNextButtonEnabled(false);
		mSendPairTextView.setText(R.string.device_add_shake_it_text);
		long diffTimeMSec = (long) (Constants.PAIR_TIME_SEC * 1000) - ((System.nanoTime() / 1000000) - mStartTimeMSec);
		if (diffTimeMSec < 0) {
			Log.w(TAG, "diffTimeMSec is less than zero!");
			return;
		}
		mCountDownTimer = new CountDownTimer(diffTimeMSec + 500, 500) {
			@Override
			public void onTick(long millisUntilFinished) {
				int timerValue = (int) (millisUntilFinished / 1000);
				mDonutProgress.setProgress(timerValue);
			}

			@Override
			public void onFinish() {
				Toast.makeText(getActivity(), R.string.device_add_device_not_found_in_time, Toast.LENGTH_LONG).show();
				resetTimer();
			}
		}.start();

		doPairRequestTask(mStartTimeMSec);
	}

	private void doPairRequestTask(long timeLimitMSec) {
		// function creates and starts Task that handles pairing between the gate and the account
		PairDeviceTask pairDeviceTask = new PairDeviceTask(getActivity(), mGateId, timeLimitMSec);
		pairDeviceTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				// Reset time of pairing start so it won't start again, when it already finished
				mStartTimeMSec = 0;

				if (success) {
					Toast.makeText(getActivity(), R.string.device_add_device_found, Toast.LENGTH_LONG).show();

					mCountDownTimer.cancel();

					// go to setup uninit device
					Intent intent = new Intent(getActivity(), SetupDeviceActivity.class);
					intent.putExtra(SetupDeviceActivity.EXTRA_GATE_ID, mGateId);
					startActivityForResult(intent, Constants.SETUP_DEVICE_REQUEST_CODE);
				} else {
					Toast.makeText(getActivity(), R.string.device_add_device_not_found_in_time, Toast.LENGTH_LONG).show();
					mCountDownTimer.cancel();
					resetTimer();
				}
			}
		});
		((BaseApplicationActivity) getActivity()).callbackTaskManager.executeTask(pairDeviceTask, mGateId, CallbackTaskManager.ProgressIndicator.PROGRESS_NONE);
	}

	public void startTimer() {
		mStartTimeMSec = System.nanoTime() / 1000000;

		mCallback.setNextButtonEnabled(false);
		mDonutProgress.setProgress(Constants.PAIR_TIME_SEC);
		mDonutProgress.setInnerBottomText(getString(R.string.device_add_time_left_unit));
		mDonutProgress.setInnerBottomTextColor(getResources().getColor(R.color.beeeon_accent));
		mDonutProgress.setTextColor(getResources().getColor(R.color.beeeon_accent));
		mDonutProgress.setFinishedStrokeColor(getResources().getColor(R.color.beeeon_accent));
		mDonutProgress.setUnfinishedStrokeColor(getResources().getColor(R.color.white));
		mSendPairTextView.setText(R.string.device_add_shake_it_text);

		// the timer should start counting down from 30, thats why + 500
		mCountDownTimer = new CountDownTimer(Constants.PAIR_TIME_SEC * 1000 + 500, 500) {

			public void onTick(long millisUntilFinished) {
				int timerValueSec = (int) (millisUntilFinished / 1000);
				mDonutProgress.setProgress(timerValueSec);
			}

			public void onFinish() {
				Toast.makeText(getActivity(), R.string.device_add_device_not_found_in_time, Toast.LENGTH_LONG).show();
				resetTimer();
			}

		}.start();

		doPairRequestTask(0);
	}

	public void doAction() {
		startTimer();
	}

	private void resetTimer() {
		mCallback.setNextButtonEnabled(true);
		mDonutProgress.setInnerBottomText(getString(R.string.device_add_waiting));
		mDonutProgress.setInnerBottomTextColor(getResources().getColor(R.color.white));
		mDonutProgress.setTextColor(getResources().getColor(R.color.white));
		mDonutProgress.setProgress(Constants.PAIR_TIME_SEC);
		mDonutProgress.setMax(Constants.PAIR_TIME_SEC);
		mDonutProgress.setFinishedStrokeColor(getResources().getColor(R.color.white));
		mSendPairTextView.setText(R.string.device_add_dialog_device_add_text);
	}

	public interface OnAddDeviceListener {
		void onAddDevice(boolean success);

		void setNextButtonEnabled(boolean enabled);
	}
}
