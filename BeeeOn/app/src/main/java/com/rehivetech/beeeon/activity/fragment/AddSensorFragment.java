package com.rehivetech.beeeon.activity.fragment;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.activity.AddSensorActivity;
import com.rehivetech.beeeon.adapter.Adapter;
import com.rehivetech.beeeon.base.TrackFragment;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.util.Log;

public class AddSensorFragment extends TrackFragment {

	public AddSensorActivity mActivity;
	private View mView;
	private Controller mController;

	// GUI elements
	private LinearLayout mLayout;
	private TextView mTimeText;
	

	private CountDownTimer mCountDownTimer;
	private boolean mTimerDone = false;
	private boolean mTimerPause = false;
	private int mTimerButtonSec = 30;
	private int mIntervalToCheckUninitSensor = 2;
	private int mTimerValue = 0;

	private Adapter mAdapter;


	private static final String TAG = AddSensorFragment.class.getSimpleName();

	private static final String TIMER_VALUE_PAUSE = "AddSensorTimerValueOnPause";
	// private static final String TIMER_BOOL_PAUSE = "AddSensorTimerBooleanOnPause";

	private static final boolean DEBUG_MODE = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Get activity and controller
		mActivity = (AddSensorActivity) getActivity();
		mController = Controller.getInstance(mActivity.getApplicationContext());

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
	    	Log.d(TAG, "ADD ADAPTER fragment is visible");
	    	mActivity.setBtnLastPage();
	    	mActivity.setFragment(this);
	    	mTimeText = (TextView) mView.findViewById(R.id.add_sensor_time);
	    	//startTimer();
	    	mActivity.checkUnInitSensor();
	    }
	    else {
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
		mTimeText.setText("Time is out");
		mActivity.resetBtnPair();
	}

	public void startTimer() {
		mTimerDone = false;
		mCountDownTimer = new CountDownTimer(mTimerButtonSec * 1000, 1000) {

			public void onTick(long millisUntilFinished) {
				if (mTimerDone)
					return;

				mTimerValue = (int) (millisUntilFinished / 1000);

				mTimeText.setText(getResources().getString(R.string.addsensor_active_pair) + " 0:" + ((mTimerValue < 10) ? "0" : "") + mTimerValue);
				if ((millisUntilFinished / 1000) % mIntervalToCheckUninitSensor == 0) {
					// check if are new uninit sensor
					Log.d(TAG, "PAIR - check if some uninit sensor");
					mActivity.checkUnInitSensor();
				}
			}

			public void onFinish() {
				// mButton.setText("done!");
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
	}

	

}
