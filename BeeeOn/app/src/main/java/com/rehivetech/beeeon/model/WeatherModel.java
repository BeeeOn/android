package com.rehivetech.beeeon.model;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import com.rehivetech.beeeon.model.weather.Weather;
import com.rehivetech.beeeon.network.INetwork;
import com.rehivetech.beeeon.network.provider.WeatherProvider;
import com.rehivetech.beeeon.util.MultipleDataHolder;

import java.io.IOException;

/**
 * Created by martin on 29.3.16.
 */
public class WeatherModel extends BaseModel {

	public WeatherModel(INetwork network) {
		super(network);
	}

	private final MultipleDataHolder<Weather> mWeather = new MultipleDataHolder<>();

	@WorkerThread
	public synchronized boolean reloadWeather(Context context, String gateId, String lat, String lon){

		Weather weather;
		WeatherProvider provider = new WeatherProvider(context);
		try {
			weather = provider.getActualWeather(lat, lon);
		} catch (IOException e) {

			return false;
		}

		mWeather.addObject(gateId, weather);
		return true;
	}

	@Nullable
	public Weather getWeather(String gateId) {
		return (mWeather.getObjects(gateId).size() == 0) ? null : mWeather.getObjects(gateId).get(0);
	}
}

