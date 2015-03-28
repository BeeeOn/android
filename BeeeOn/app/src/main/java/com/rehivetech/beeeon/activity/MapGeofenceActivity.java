package com.rehivetech.beeeon.activity;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.base.BaseApplicationActivity;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.geofence.GeofenceIntentService;
import com.rehivetech.beeeon.geofence.SimpleGeofence;
import com.rehivetech.beeeon.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapGeofenceActivity extends BaseApplicationActivity implements ResultCallback<Status>, OnMapLongClickListener, OnMarkerDragListener,
		OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

	private static final String TAG = MapGeofenceActivity.class.getSimpleName();

	private static final float MAP_ZOOM = 17.5F;
	private static final int MAXIMUM_RET_ADDRESS = 3;
	GoogleApiClient mGoogleApiClient;
	private EditText mEditSearch;
	private SearchView mSearchView;
	private GoogleMap mMap;
	private Toolbar mToolbar;

	/**
	 * Only one geofence can be added in time. If it is null, no geofence is adding.
	 */
	private SimpleGeofence mAddGeofence = null;


	private PendingIntent mGeofencePendingIntent = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_map_geofence);

		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		if (mToolbar != null) {
			mToolbar.setTitle(R.string.title_activity_map_geofence);
			setSupportActionBar(mToolbar);
		}

		setSupportProgressBarIndeterminate(true);
		setProgressBarIndeterminateVisibility(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);

		buildGoogleApiClient();
	}

	// Create the options menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.map_menu, menu);

		MenuItem searchItem = menu.findItem(R.id.action_search);
		mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);

/*
		mEditSearch.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				boolean handled = false;
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					handled = true;

					String searchString = mEditSearch.getText().toString();
					Log.d(TAG, "Request for: " + searchString);
					if (searchString != null && !searchString.isEmpty()) {
						Log.d(TAG, "Not null");
						new GeocoderTask().execute(searchString);
					}

					// hide keyboard
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(mEditSearch.getWindowToken(), 0);

					// hide action mode
					menuSearch.collapseActionView();

					Toast.makeText(MapGeofenceActivity.this, "Searching", Toast.LENGTH_SHORT).show();
				}
				return handled;
			}
		});

		menuSearch.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {

			// Menu Action Collapse
			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				// Empty EditText to remove text
				mEditSearch.setText("");
				mEditSearch.clearFocus();
				return true;
			}

			// Menu Action Expand
			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				// Focus on EditText
				mEditSearch.requestFocus();

				// Force the keyboard to show on EditText focus
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
				return true;
			}
		});
*/
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			case R.id.action_search:
				mSearchView.setIconified(false);
				return true;
		}
		return false;
	}

	/**
	 * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the LocationServices API.
	 */
	private synchronized void buildGoogleApiClient() {
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API)
				.build();
	}

	private void addGeofence(SimpleGeofence geofence) {
		if (!mGoogleApiClient.isConnected()) {
			Log.e(TAG, "Google Api Client is not connected");
			return;
		}

		if (mAddGeofence != null) {
			Log.e(TAG, "Other geofence is adding");
			return;
		}

		try {
			mAddGeofence = geofence;
			LocationServices.GeofencingApi.addGeofences(
					mGoogleApiClient,
					// The GeofenceRequest object.
					getGeofencingRequest(geofence),
					// A pending intent that that is reused when calling removeGeofences(). This
					// pending intent is used to generate an intent when a matched geofence
					// transition is observed.
					getGeofencePendingIntent()
			).setResultCallback(this); // Result processed in onResult().
		} catch (SecurityException securityException) {
			// Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
			Log.e(TAG, "Invalid location permission. " +
					"You need to use ACCESS_FINE_LOCATION with geofences", securityException);
		}
	}

	/**
	 * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
	 * Also specifies how the geofence notifications are initially triggered.
	 */
	private GeofencingRequest getGeofencingRequest(SimpleGeofence geofence) {
		List<Geofence> geofenceList = new ArrayList<>();
		geofenceList.add(geofence.toGeofence());

		GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

		// The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
		// GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
		// is already inside that geofence.
		// builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

		// Add the geofences to be monitored by geofencing service.
		builder.addGeofences(geofenceList);

		// Return a GeofencingRequest.
		return builder.build();
	}

	private PendingIntent getGeofencePendingIntent() {
		// Reuse the PendingIntent if we already have it.
		if (mGeofencePendingIntent != null) {
			return mGeofencePendingIntent;
		}
		Intent intent = new Intent(this, GeofenceIntentService.class);
		// We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
		// calling addGeofences() and removeGeofences().
		return PendingIntent.getService(this, 0, intent, PendingIntent.
				FLAG_UPDATE_CURRENT);
	}

	@Override
	public void onMapReady(GoogleMap map) {
		// it needs permission in manifest
		this.mMap = map;
		mMap.setMyLocationEnabled(true);

		mMap.getUiSettings().setMapToolbarEnabled(true);

		mMap.setOnMapLongClickListener(this);

		// Sets the map type to be "hybrid"
		mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		mMap.setOnMarkerDragListener(this);

		// trying to zoom to actual position
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		Criteria criteria = new Criteria();

		String provider = locationManager.getBestProvider(criteria, false);
		if (provider != null) {
			Location location = locationManager.getLastKnownLocation(provider);
			if (location != null) {

				LatLng actlatLng = new LatLng(location.getLatitude(), location.getLongitude());
				mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(actlatLng, MAP_ZOOM));

				// CameraPosition INIT = new CameraPosition.Builder()
				// .target(actlatLng)
				// .zoom(MAP_ZOOM)
				// .bearing(300F) // orientation
				// .tilt(50F) // viewing angle
				// .build();
				//
				// mMap.animateCamera(CameraUpdateFactory.newCameraPosition(INIT));
			} else {
				Log.d(TAG, "Location is not accesible.");
			}
		}

//		drawAllGeofences();
	}

	public void drawAllGeofences() {
		// TODO smazat po zavedeni z kontroleru
		SimpleGeofence fence = new SimpleGeofence("Brno", 49.195060, 16.606837, 500);
		drawGeofence(fence);

		List<SimpleGeofence> geofences = Controller.getInstance(this).getAllGeofences();
		for (SimpleGeofence actFence : geofences) {
			drawGeofence(actFence);
		}
	}

	public void drawGeofence(SimpleGeofence fence) {
		if (fence == null) {
			Log.e(TAG, "Geofence is null.");
			return;
		}
		mMap.addMarker(
				new MarkerOptions()
						.position(new LatLng(fence.getLatitude(), fence.getLongitude()))
						.title(fence.getName())
//						.snippet("Radius: " + fence.getRadius())
		)
				.showInfoWindow();

		// Instantiates a new CircleOptions object + center/radius
		CircleOptions circleOptions = new CircleOptions().center(new LatLng(fence.getLatitude(), fence.getLongitude()))
				.radius(fence.getRadius()).fillColor(R.color.beeeon_primary_cyan_dark).strokeColor(Color.TRANSPARENT).strokeWidth(2);

		// Get back the mutable Circle
		Circle circle = mMap.addCircle(circleOptions);
	}

	@Override
	protected void onAppResume() {

	}

	@Override
	protected void onAppPause() {

	}

	/**
	 * Runs when a GoogleApiClient object successfully connects.
	 */
	@Override
	public void onConnected(Bundle connectionHint) {
		Log.i(TAG, "Connected to GoogleApiClient");
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// Refer to the javadoc for ConnectionResult to see what error codes might be returned in
		// onConnectionFailed.
		Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
	}

	@Override
	public void onConnectionSuspended(int cause) {
		// The connection to Google Play services was lost for some reason. We call connect() to
		// attempt to re-establish the connection.
		Log.i(TAG, "Connection suspended");
		mGoogleApiClient.connect();
	}

	/**
	 * CALLBACKS *
	 */

	@Override
	public void onMapLongClick(LatLng pos) {
		Toast.makeText(this, "Long Click", Toast.LENGTH_LONG).show();
		SimpleGeofence geofence = new SimpleGeofence("ahoj", pos.latitude, pos.longitude, 100);
		addGeofence(geofence);
	}

	@Override
	public void onMarkerDrag(Marker arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMarkerDragEnd(Marker arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMarkerDragStart(Marker arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onResult(Status status) {
		if (status.isSuccess()) {
			Log.i(TAG, "Geoefence added");
			drawGeofence(mAddGeofence);
			// FIXME opravit ukldaani do databaze
//			Controller.getInstance(this).addGeofence(mAddGeofence);
			mAddGeofence = null;

			// Update state and save in shared preferences.
//			mGeofencesAdded = !mGeofencesAdded;
//			SharedPreferences.Editor editor = mSharedPreferences.edit();
//			editor.putBoolean(Constants.GEOFENCES_ADDED_KEY, mGeofencesAdded);
//			editor.commit();
//
//			// Update the UI. Adding geofences enables the Remove Geofences button, and removing
//			// geofences enables the Add Geofences button.
//			setButtonsEnabledState();
//
//			Toast.makeText(
//					this,
//					getString(mGeofencesAdded ? R.string.geofences_added :
//							R.string.geofences_removed),
//					Toast.LENGTH_SHORT
//			).show();
		} else {
//			// Get the status code for the error and log it using a user-friendly message.
//			String errorMessage = GeofenceErrorMessages.getErrorString(this,
//					status.getStatusCode());
			mAddGeofence = null;
			Log.e(TAG, "Geofence wasn't registered. No listening for geofence!");
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		mGoogleApiClient.connect();
	}

	@Override
	public void onStop() {
		super.onStop();
		mGoogleApiClient.disconnect();
	}

	// An AsyncTask class for accessing the GeoCoding Web Service
	private class GeocoderTask extends AsyncTask<String, Void, List<Address>> {

		@Override
		protected List<Address> doInBackground(String... locationName) {
			// Creating an instance of Geocoder class
			Geocoder geocoder = new Geocoder(getBaseContext());
			List<Address> addresses = null;

			Log.d(TAG, "Start searching:" + locationName);

			try {
				// Getting a maximum of 3 Address that matches the input text
				addresses = geocoder.getFromLocationName(locationName[0], MAXIMUM_RET_ADDRESS);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return addresses;
		}

		@Override
		protected void onPostExecute(List<Address> addresses) {

			if (addresses == null || addresses.size() == 0) {
				Toast.makeText(getBaseContext(), "No Location found", Toast.LENGTH_SHORT).show();
			}

			// Clears all the existing markers on the map
			// mMap.clear();

			// Adding Markers on Google Map for each matching address
			for (int i = 0; i < addresses.size(); i++) {

				Address address = (Address) addresses.get(i);

				// Creating an instance of GeoPoint, to display in Google Map
				LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

				String addressText = String
						.format("%s, %s", address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
								address.getCountryName());

				MarkerOptions markerOptions = new MarkerOptions();
				markerOptions.position(latLng);
				markerOptions.title(addressText);

				mMap.addMarker(markerOptions);

				// Locate the first location
				if (i == 0)
					mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
			}
		}
	}
}
