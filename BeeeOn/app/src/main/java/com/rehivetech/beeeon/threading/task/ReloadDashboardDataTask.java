package com.rehivetech.beeeon.threading.task;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.exception.AppException;

import java.util.EnumSet;

/**
 * Created by martin on 29.3.16.
 */
public class ReloadDashboardDataTask extends ReloadGateDataTask{


	public ReloadDashboardDataTask(Context context, boolean forceReload, ReloadWhat what) {
		super(context, forceReload, what);
	}

	public ReloadDashboardDataTask(Context context, boolean forceReload, EnumSet<ReloadWhat> what) {
		super(context, forceReload, what);
	}


	@Override
	public Boolean doInBackground(String... params) {
		try {
			if (!super.doInBackground(params[0])) {
				return false;
			}
		} catch (AppException e) {
			return false;
		}

		if (params.length == 1) {
			return true;
		}

		return Controller.getInstance(mContext).getWeatherModel().reloadWeather(mContext, params[0], params[1], params[2]);
	}
}
