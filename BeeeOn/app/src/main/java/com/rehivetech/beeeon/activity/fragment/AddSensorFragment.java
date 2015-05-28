package com.rehivetech.beeeon.activity.fragment;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.lzyzsd.circleprogress.DonutProgress;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.activity.AddSensorActivity;
import com.rehivetech.beeeon.base.TrackFragment;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.util.Log;


public class AddSensorFragment extends TrackFragment {

	public AddSensorActivity mActivity;
	private View mView;
	private Controller mController;

	// GUI elements
	private LinearLayout mLayout;
	private TextView mTimeText;
	private DonutProgress mTime;


	private CountDownTimer mCountDownTimer;
	private boolean mTimerDone = false;
	private boolean mTimerPause = false;
	private int mTimerButtonSec = 30;
	private int mIntervalToCheckUninitSensor = 2;
	private int mTimerValue = 0;
	private int mTimeOldValProgress = 100;
	private int mTimeNewValProgress = 0;

	private Gate mGate;


	private static final String TAG = AddSensorFragment.class.getSimpleName();

	private static final String TIMER_VALUE_PAUSE = "AddSensorTimerValueOnPause";
	// private static final String TIMER_BOOL_PAUSE = "AddSensorTimerBooleanOnPause";

	private static final boolean DEBUG_MODE = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Get activity and controller
		mActivity = (AddSensorActivity) getActivity();
		mController = Controller.getInstance(mActivity);

		// Send request
		mGate = mController.getActiveGate();
		if (mGate == null) {
			Toast.makeText(mActivity, getResources().getString(R.string.toast_no_adapter), Toast.LENGTH_LONG).show();
			// TODO: Ukoncit dialog ?
		}

		if (savedInstanceState != null) {
			mTimerButtonSec = savedInstanceState.getInt(TIMER_VALUE_PAUSE);
			Log.d(TAG, "Timer value: " + mTimerButtonSec);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.activity_add_sensor_activity_dialog, container, false);
		mLayout = (LinearLayout) mView.findViewById(R.id.container);
		return mView;
	}


	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser) {
			Log.d(TAG, "ADD Sensor fragment is visible");
			mActivity.setBtnLastPage();
			mActivity.setFragment(this);
			//mTimeText = (TextView) mView.findViewById(R.id.add_sensor_time);
			//startTimer();
			mActivity.checkUnInitSensor();

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
		} else {
			stopTimer();
		}
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

	@Override
	public void onStart() {
		super.onStart();
	}


	public void resetPairButton() {
		//mTimeText.setText("Time is out");
		if (mTime != null) {
			//mTime.setTitle(" ");
			mTime.setInnerBottomText("Time is out");
		}
		mActivity.resetBtnPair();
	}

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
					mActivity.checkUnInitSensor();
				}
			}

			public void onFinish() {
				// mButton.setText("done!");
				mTime.setProgress(0);
				resetPairButton();
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
}
