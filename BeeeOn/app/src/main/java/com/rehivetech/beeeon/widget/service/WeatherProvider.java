package com.rehivetech.beeeon.widget.service;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.widget.persistence.WidgetWeatherPersistence;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Tomáš on 25. 4. 2015.
 */
public class WeatherProvider {

	private static final int SUCCESSFULL_CODE = 200;
	// always using metric units -> UnitsHelper will transform it
	private static final String UNITS = "metric";
	private static final String URL_LOCATION = "http://api.openweathermap.org/data/2.5/find?q=%s&mode=json&units=%s&lang=%s";
	private static final String URL_WEATHER_BY_ID = "http://api.openweathermap.org/data/2.5/weather?id=%s&units=%s&lang=%s";

	private static int BUFFER_SIZE = 1024;
	private Context mContext;

	public WeatherProvider(Context context) {
		mContext = context.getApplicationContext();
	}

	// ------------------------------------------------------ //
	// -------------------- UPDATING METHODS ---------------- //
	// ------------------------------------------------------ //

	public JSONObject getLocations(String cityInput) {
		try {
			HttpURLConnection connection = requestLocation(cityInput, getLangLocaleCode());

			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuilder json = new StringBuilder(BUFFER_SIZE);
			String tmp;
			while ((tmp = reader.readLine()) != null) {
				Log.d("getLocations", tmp);
				json.append(tmp).append("\n");
			}
			reader.close();
			JSONObject data = new JSONObject(json.toString());

			// This value will be 404 if the request was not successful
			if (data.getInt("cod") != SUCCESSFULL_CODE) {
				return null;
			}

			return data;
		} catch (JSONException | IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public JSONObject getWeatherByCityId(String cityId) {
		try {
			HttpURLConnection connection = requestWeatherById(cityId, getLangLocaleCode());

			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			StringBuilder json = new StringBuilder(BUFFER_SIZE);
			String tmp;
			while ((tmp = reader.readLine()) != null) {
				Log.d("getWeatherByCityId", tmp);
				json.append(tmp).append("\n");
			}
			reader.close();

			JSONObject data = new JSONObject(json.toString());

			// This value will be 404 if the request was not successful
			if (data.getInt("cod") != SUCCESSFULL_CODE) {
				return null;
			}

			return data;
		} catch (IOException | JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	// ------------------------------------------------------ //
	// ----------------------- REQUESTS --------------------- //
	// ------------------------------------------------------ //

	private HttpURLConnection requestLocation(String cityName, String lang) throws IOException {
		URL url = new URL(String.format(URL_LOCATION, Uri.encode(cityName), UNITS, lang));

		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.addRequestProperty("x-api-key", mContext.getString(R.string.open_weather_maps_app_id));

		return connection;
	}

	private HttpURLConnection requestWeatherById(String cityId, String lang) throws IOException {
		URL url = new URL(String.format(URL_WEATHER_BY_ID, Uri.encode(cityId), UNITS, lang));

		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.addRequestProperty("x-api-key", mContext.getString(R.string.open_weather_maps_app_id));

		return connection;
	}

	// ------------------------------------------------------ //
	// ----------------------- PARSERS ---------------------- //
	// ------------------------------------------------------ //

	public List<City> parseCities(JSONObject data) {
		ArrayList<City> results = new ArrayList<>();
		try {
			JSONArray listOfCities = data.getJSONArray("list");
			for (int i = 0; i < listOfCities.length(); i++) {
				JSONObject cityObj = listOfCities.getJSONObject(i);
				City city = new City();
				city.id = cityObj.getString("id");
				city.name = cityObj.getString("name");
				city.countryId = cityObj.getJSONObject("sys").getString("country");
				city.json = cityObj;
				results.add(city);
			}
			return results;
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Parse widget icon based on id which were get from the server
	 *
	 * @param actualId
	 * @param sunrise
	 * @param sunset
	 * @return string icon resource
	 */
	public static int parseWeatherIconResource(int actualId, long sunrise, long sunset) {
		int iconRes;

		// if its exactly 800, its clear sky
		if (actualId == 800) {
			long currentTime = new Date().getTime();
			if (sunrise != -1 && sunset != -1) {
				if (currentTime >= sunrise && currentTime < sunset) {
					iconRes = R.string.weather_sunny;
				} else {
					iconRes = R.string.weather_clear_night;
				}
			} else {
				iconRes = R.string.weather_sunny;
			}

			return iconRes;
		}
		// else we simplify that to some groups
		int id = actualId / 100;
		switch (id) {
			case 2:
				iconRes = R.string.weather_thunder;
				break;
			case 3:
				iconRes = R.string.weather_drizzle;
				break;
			case 5:
				iconRes = R.string.weather_rainy;
				break;
			case 6:
				iconRes = R.string.weather_snowy;
				break;
			case 7:
				iconRes = R.string.weather_foggy;
				break;
			case 8:
				iconRes = R.string.weather_cloudy;
				break;

			// default is nice day
			default:
				iconRes = WidgetWeatherPersistence.DEFAULT_WEATHER_ICON;
				break;
		}

		return iconRes;
	}


	private String getLangLocaleCode() {
		// TODO should change, cause e.g czech language returns CS and OWM needs CZ
		//Locale locale = mContext.getResources().getConfiguration().locale;
		//Log.v("WeatherProvider", "selected locale code: " + locale.getLanguage());
		//return locale.getLanguage();
		return "en";
	}

	public class City {
		public String id;
		public String name;
		public String countryId;
		public JSONObject json;
	}
}


