package com.rehivetech.beeeon.gui.fragment;

import android.content.SharedPreferences;
import android.os.Handler;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.util.ActualizationTime;

/**
 * Created by david on 3.9.15.
 */
public abstract class BaseApplicationFragmentWithReloadDataTask extends BaseApplicationFragment {
	private Handler mHandler;

	@Override
	public void onResume() {
		super.onResume();
		setHandler();
	}

	@Override
	public void onPause() {
		super.onPause();
		mHandler = null;
	}

	private void setHandler() {
		SharedPreferences prefs = Controller.getInstance(getActivity()).getUserSettings();
		String time = prefs.getString(ActualizationTime.PERSISTENCE_ACTUALIZATON_KEY, null);
		int period = Integer.parseInt(time) * 1000;

		if (period > 0) {
			mHandler = new Handler();
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					doDataReloadTask(false);
				}
			}, period);
		}
	}

	abstract void doDataReloadTask(boolean forceRefresh);
}