package com.rehivetech.beeeon.gui.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.util.ActualizationTime;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by david on 3.9.15.
 */
public abstract class BaseApplicationFragmentWithReloadDataTask extends BaseApplicationFragment {
	private Timer mReloadTimer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mReloadTimer = new Timer();
	}

	@Override
	public void onResume() {
		super.onResume();
		setTimerForReloadTasks();
	}

	@Override
	public void onPause() {
		super.onPause();
		mReloadTimer.cancel();
	}

	private void setTimerForReloadTasks() {
		SharedPreferences prefs = Controller.getInstance(getActivity()).getUserSettings();
		String time = prefs.getString(ActualizationTime.PERSISTENCE_ACTUALIZATON_KEY, null);
		int period = Integer.parseInt(time) * 1000;

		if(period > 0) {
			// 0 means do not reload data

			mReloadTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					doDataReloadTask(false);
				}
			}, 0, period);
		}
	}

	abstract void doDataReloadTask(boolean forceRefresh);
}