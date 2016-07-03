package com.rehivetech.beeeon.network.provider;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.WorkerThread;

import com.google.gson.Gson;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.model.weather.Weather;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import timber.log.Timber;

/**
 * Created by martin on 29.3.16.
 */
public class WeatherProvider {

	private static final String BASE_URL = "http://api.openweathermap.org/data/2.5/weather?units=metric";
	private static final String QUERY_APP_ID = "appid";
	private static final String QUERY_LAT = "lat";
	private static final String QUERY_LONG = "lon";


	Context mContext;

	public WeatherProvider(Context context) {
		mContext = context;
	}

	@WorkerThread
	public Weather getActualWeather(String lat, String lon) throws IOException {
		Uri.Builder builder = Uri.parse(BASE_URL).buildUpon();

		builder.appendQueryParameter(QUERY_APP_ID, mContext.getString(R.string.open_weather_maps_app_id));
		builder.appendQueryParameter(QUERY_LAT, lat);
		builder.appendQueryParameter(QUERY_LONG, lon);

		URL url = new URL(builder.build().toString());
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

		return readResponse(reader);
	}

	private Weather readResponse(BufferedReader reader) throws IOException {
		StringBuilder json = new StringBuilder();

		String tmp;
		while ((tmp = reader.readLine()) != null) {
			json.append(tmp);
		}

		reader.close();

		Timber.i("open weather maps json: %s", json.toString());

		return new Gson().fromJson(json.toString(), Weather.class);
	}
}
