package com.rehivetech.beeeon.network.provider;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.WorkerThread;

import com.google.gson.Gson;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.model.place.Place;
import com.rehivetech.beeeon.model.place.PlaceResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import timber.log.Timber;

/**
 * Created by martin on 26.3.16.
 */
public class PlacesProvider {

	private static final String GOOGLE_PLACES_BASE_URL = "https://maps.googleapis.com/maps/api/place/textsearch/json?types=locality";
	private static final String QUERY_QUERY_PLACE = "query";
	private static final String QUERY_KEY = "key";

	private Context mContext;

	public PlacesProvider(Context context) {
		mContext = context;
	}

	@WorkerThread
	public List<Place> getPlaces(String constraint) throws IOException{
		Uri.Builder builder = Uri.parse(GOOGLE_PLACES_BASE_URL).buildUpon();

		builder.appendQueryParameter(QUERY_QUERY_PLACE, constraint);
		builder.appendQueryParameter(QUERY_KEY, mContext.getString(R.string.api_keys_google_places_server));

		URL url = new URL(builder.build().toString());
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

		return readResponse(reader);
	}

	private List<Place> readResponse(BufferedReader reader) throws IOException {
		StringBuilder json = new StringBuilder();

		String tmp;
		while ((tmp = reader.readLine()) != null) {
			json.append(tmp);
		}

		reader.close();

		Timber.i("google places json: %s", json.toString());

		return new Gson().fromJson(json.toString(), PlaceResult.class).getResults();
	}
}
