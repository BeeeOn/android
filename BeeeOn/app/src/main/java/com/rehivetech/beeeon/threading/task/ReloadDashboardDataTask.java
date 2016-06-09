package com.rehivetech.beeeon.threading.task;

import android.content.Context;
import android.support.annotation.Nullable;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.exception.AppException;

import java.util.EnumSet;

/**
 * @author martin
 * @since 29.3.16
 */
public class ReloadDashboardDataTask extends ReloadGateDataTask {


	public ReloadDashboardDataTask(Context context, boolean forceReload, ReloadWhat what) {
		super(context, forceReload, what);
	}

	public ReloadDashboardDataTask(Context context, boolean forceReload, EnumSet<ReloadWhat> what) {
		super(context, forceReload, what);
	}


	@Override
	public Boolean doInBackground(String... params) {
		String gateId = params[0];

		try {
			if (!super.doInBackground(gateId)) {
				return false;
			}
		} catch (AppException e) {
			return false;
		}

		if (params.length == 1) {
			return true;
		}

		String latitude = params[1];
		String longitude = params[2];
		return Controller.getInstance(mContext).getWeatherModel().reloadWeather(mContext, gateId, latitude, longitude);
	}
}
