package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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

	private View mView;

	private OnAddSensorListener mCallback;

	private String mGateId;


	// GUI elements
	private DonutProgress mTime;
	private CountDownTimer mCountDownTimer;
	private boolean mTimerDone = false;
	private boolean mTimerPause = false;
	private int mTimerButtonSec = 30;
	private int mIntervalToCheckUninitSensor = 2;
	private int mTimerValue = 0;
	private int mTimeOldValProgress = 100;
	private int mTimeNewValProgress = 0;


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

		if (savedInstanceState != null) {
			mTimerButtonSec = savedInstanceState.getInt(TIMER_VALUE_PAUSE);
			Log.d(TAG, "Timer value: " + mTimerButtonSec);
		}

		mGateId = getArguments().getString(KEY_GATE_ID);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			// Get activity and controller
			mCallback = (OnAddSensorListener) getActivity();
		} catch (ClassCastException e) {
			throw new ClassCastException(String.format("%s must implement OnAddSensorListener",activity.toString()));
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.activity_add_sensor_activity_dialog, container, false);
		return mView;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constants.SETUP_SENSOR_REQUEST_CODE) {
			mCallback.onAddSensor(resultCode == Activity.RESULT_OK);
		}
	}
/*
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser) {
			Log.d(TAG, "ADD Sensor fragment is visible");
			//mTimeText = (TextView) mView.findViewById(R.id.add_sensor_time);
			//startTimer();
			checkUnInitSensor();

			mTime = (DonutProgress) mView.findViewById(R.id.progress);
			mTime.setMax(mTimerButtonSec);
			mTime.setInnerBottomText(getString(R.string.addsensor_sending_request));
			mTime.setInnerBottomTextColor(getResources().getColor(R.color.beeeon_secundary_pink));
			//mTime.setTitle(" ");
			/*
			NumberPicker np = (NumberPicker) mView.findViewById(R.id.numberPicker);
			np.setMaxValue(20);
			np.setMinValue(0);
			np.setFocusable(true);
			np.setFocusableInTouchMode(true);
			*/
	/*	} else {
			stopTimer();
		}
	}**/


	@Override
	public void onPause() {
		super.onPause();
		Log.d(TAG, "OnPause AddSensorDialog !!");
		mTimerDone = true;
		if (mCountDownTimer != null)
			mCountDownTimer.cancel();
		if (mTime != null) {
			mTime.setInnerBottomText(getString(R.string.addsensor_stoped));
			//mTime.setTitle(" ");
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(TIMER_VALUE_PAUSE, mTimerValue);
		// outState.putBoolean(TIMER_BOOL_PAUSE, true);
	}
/*
	public void resetPairButton() {
		//mTimeText.setText("Time is out");
		if (mTime != null) {
			//mTime.setTitle(" ");
			mTime.setInnerBottomText("Time is out");
		}
		resetBtnPair();
	}
*/
	public void startTimer() {
		mTimerDone = false;
		if (mTime != null) {
			//mTime.setTitle(String.valueOf(mTimerButtonSec));
			mTime.setInnerBottomText("seconds");
		}
		mCountDownTimer = new CountDownTimer(mTimerButtonSec * 1000, 1000) {

			public void onTick(long millisUntilFinished) {
				if (mTimerDone)
					return;

				mTimerValue = (int) (millisUntilFinished / 1000);

				//mTimeText.setText(getResources().getString(R.string.addsensor_active_pair) + " 0:" + ((mTimerValue < 10) ? "0" : "") + mTimerValue);
				//mTime.setTitle(String.valueOf(mTimerValue));
				//mTimeNewValProgress = mTimerValue*100/mTimerButtonSec;
				Log.d(TAG, "actual progress: " + mTimeNewValProgress);
				mTime.setProgress(mTimerValue);
				//mTime.animateProgressTo(mTimeOldValProgress,mTimeNewValProgress,null);
				//mTimeOldValProgress = mTimeNewValProgress;
				if ((millisUntilFinished / 1000) % mIntervalToCheckUninitSensor == 0) {
					// check if are new uninit sensor
					Log.d(TAG, "PAIR - check if some uninit sensor");
					checkUnInitSensor();
				}
			}

			public void onFinish() {
				// mButton.setText("done!");
				mTime.setProgress(0);
				//resetPairButton();
			}
		}.start();

	}

	public void checkUninitSensors() {
		startTimer();
	}

	public void stopTimer() {
		mTimerDone = true;
		if (mCountDownTimer != null)
			mCountDownTimer.cancel();
		if (mTime != null) {
			//mTime.setSubTitle(getString(R.string.addsensor_stoped));
			//mTime.setTitle(" ");
		}
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
					Log.d(TAG, "Nasel jsem neinicializovane zarizeni !!!!");
					stopTimer();
					// go to setup uninit sensor
					Intent intent = new Intent(getActivity(), SetupSensorActivity.class);
					startActivityForResult(intent, Constants.SETUP_SENSOR_REQUEST_CODE);
				} /*else {
					if (mFirstUse) {
						mFirstUse = false;
						doPairRequestTask(mPairGate.getId());
						mNext.setEnabled(false);
					}
				}*/
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
				mCallback.setNextButtonEnabled(true);
				if (success) {
					// Request was successfully sent
					startTimer();
				} else {
					// Request wasn't send
					//resetBtnPair();
				}
			}

		});

		// Execute and remember task so it can be stopped automatically
		((BaseApplicationActivity) getActivity()).callbackTaskManager.executeTask(pairRequestTask, gateId);
	}

	public void checkUnInitSensor() {
		Log.d(TAG, "Send if some uninit mDevice");
		doReloadUninitializedDevicesTask(mGateId, true);
	}

	public void doAction () {
		doPairRequestTask(mGateId);
	}

	public interface OnAddSensorListener {
		void onAddSensor(boolean success);
		void setNextButtonEnabled(boolean enabled);
	}
}
