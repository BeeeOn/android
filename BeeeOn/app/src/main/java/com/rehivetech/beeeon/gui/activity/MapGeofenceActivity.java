package com.rehivetech.beeeon.gui.activity;

import android.app.PendingIntent;
import android.content.Context;
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
import android.support.v7.view.ActionMode;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.dialog.GeofenceDialogFragment;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.geofence.GeofenceHelper;
import com.rehivetech.beeeon.geofence.SimpleGeofence;
import com.rehivetech.beeeon.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapGeofenceActivity extends BaseApplicationActivity implements ResultCallback<Status>, OnMapLongClickListener,
		OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GeofenceDialogFragment.GeofenceCrateCallback {

	private static final String TAG = MapGeofenceActivity.class.getSimpleName();

	private static final String TAG_DIALOG_ADD_GEOFENCE = "geofenceDialog";

	private static final float MAP_ZOOM = 17.5F;
	private static final int MAXIMUM_RET_ADDRESS = 1;
	private static final int GEOFENCE_BOUND_PADDING = 50;
	private GoogleApiClient mGoogleApiClient;
	private GoogleMap mMap;
	private Toolbar mToolbar;
	private ActionMode mActionMode;
	private HashMap<Marker, GeofenceHolder> mMarkers = new HashMap<>();
	private SearchView mSearchView;
	private MenuItem mSearchItem;
	private boolean mIsAnimated = false;

	/**
	 * Only one geofence can be added in time. If it is null, no geofence is adding.
	 */
	private SimpleGeofence mAddGeofence = null;


	private PendingIntent mGeofencePendingIntent = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_map_geofence);

		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		if (mToolbar != null) {
			mToolbar.setTitle(R.string.title_activity_map_geofence);
			setSupportActionBar(mToolbar);
		}

//		setSupportProgressBarIndeterminate(true);
//		setSupportProgressBarIndeterminateVisibility(true);

		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);

		buildGoogleApiClient();
	}


	@Override
	public void onMapReady(GoogleMap map) {
		this.mMap = map;
		mMap.setMyLocationEnabled(true);

		mMap.getUiSettings().setMapToolbarEnabled(true);

		mMap.setOnMapLongClickListener(this);

		// Sets the map type to be "hybrid"
		mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

		mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker marker) {
				marker.showInfoWindow();
				GeofenceHolder fenceHolder = mMarkers.get(marker);
				if (fenceHolder != null) {
					mActionMode = startSupportActionMode(new ActionModeGeofence(fenceHolder));
				}
				return true;
			}
		});

		drawAllGeofences();

		mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
			@Override
			public void onMapLoaded() {
				if (!mIsAnimated) {
					initialZoom();
					mIsAnimated = true;
				}
			}
		});

//		setSupportProgressBarIndeterminateVisibility(false);
	}

	private void initialZoom() {
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		String provider = locationManager.getBestProvider(criteria, false);
		Location location = null;
		if (provider != null) {
			location = locationManager.getLastKnownLocation(provider);
		}

		// if there is any geofence, zoom to it
		if (mMarkers.size() == 1) {
			Log.i(TAG, "One marker found, zooming to it.");
			for (GeofenceHolder actHolder : mMarkers.values()) {
				mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(actHolder.getMarker().getPosition(), MAP_ZOOM));
			}

		} else if (mMarkers.size() > 1) {
			Log.i(TAG, "Markers found, zooming to it.");
			zoomActualGeofences();
		}
		// if there is no geofence, zoom to actual position if available
		else if (location != null) {
			LatLng actlatLng = new LatLng(location.getLatitude(), location.getLongitude());
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(actlatLng, MAP_ZOOM));
		}
		// if location is not
		else {
			Log.d(TAG, "Location is not accesible.");
		}
	}

	private void zoomActualGeofences() {
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		for (GeofenceHolder actHolder : mMarkers.values()) {
			builder.include(actHolder.getMarker().getPosition());
		}
		LatLngBounds bounds = builder.build();
		CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, GEOFENCE_BOUND_PADDING);
		mMap.animateCamera(cu);
	}

	// Create the options menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.map_menu, menu);


		// Get the SearchView and set the searchable configuration
		mSearchItem = menu.findItem(R.id.action_search);
		mSearchView = (SearchView) MenuItemCompat.getActionView(mSearchItem);

		mSearchView.setSubmitButtonEnabled(true);

		mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String s) {
				new GeocoderTask().execute(s);

				// hide keyboard
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);

				MenuItemCompat.collapseActionView(mSearchItem);

//				setSupportProgressBarIndeterminateVisibility(true);
				return true;
			}

			@Override
			public boolean onQueryTextChange(String s) {
				return false;
			}
		});

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
//			case R.id.action_search:
////				mSearchView.setIconified(false);
//				return true;
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
		Controller controller = Controller.getInstance(this);
		String userId = controller.getActualUser().getId();

		// if demo mode just save it to database
		if (controller.isDemoMode()) {
			controller.getGeofenceModel().addGeofence(userId, geofence);
			drawGeofence(geofence);
			return;
		}
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
					GeofenceHelper.getGeofencingRequest(geofence),
					// A pending intent that that is reused when calling removeGeofences(). This
					// pending intent is used to generate an intent when a matched geofence
					// transition is observed.
					GeofenceHelper.getGeofencePendingIntent(mGeofencePendingIntent, this)
			).setResultCallback(this); // Result processed in onResult().
		} catch (SecurityException securityException) {
			// Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
			Log.e(TAG, "Invalid location permission. " +
					"You need to use ACCESS_FINE_LOCATION with geofences", securityException);
		}
	}

	private void deleteGeofence(GeofenceHolder holder) {
		List<String> geofenceIds = new ArrayList<>();
		geofenceIds.add(holder.getGeofence().getId());

		Controller controller = Controller.getInstance(this);
		String userId = controller.getActualUser().getId();

		if (!controller.isDemoMode()) {
			LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, geofenceIds);
		}

		controller.getGeofenceModel().deleteGeofence(userId, holder.getGeofence().getId());

		holder.getMarker().setVisible(false);
		holder.getCircle().setVisible(false);
	}

	private void drawAllGeofences() {
		Controller controller = Controller.getInstance(this);

		String userId = controller.getActualUser().getId();
		List<SimpleGeofence> geofences = controller.getGeofenceModel().getAllGeofences(userId);

		for (SimpleGeofence actFence : geofences) {
			drawGeofence(actFence);
		}
	}

	private void drawGeofence(SimpleGeofence fence) {
		if (fence == null) {
			Log.e(TAG, "Geofence is null.");
			return;
		}
		Marker marker = mMap.addMarker(
				new MarkerOptions()
						.position(new LatLng(fence.getLatitude(), fence.getLongitude()))
						.title(fence.getName())
						.snippet(getString(R.string.radius) + ": " + fence.getRadius() + " " + getString(R.string.unit_meter_short))
						.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));

		// Instantiates a new CircleOptions object + center/radius
		CircleOptions circleOptions = new CircleOptions().center(new LatLng(fence.getLatitude(), fence.getLongitude()))
				.radius(fence.getRadius()).fillColor(getResources().getColor(R.color.beeeon_secundary_pink_transparent))
				.strokeColor(Color.TRANSPARENT).strokeWidth(2);

		// Get back the mutable Circle
		Circle circle = mMap.addCircle(circleOptions);

		GeofenceHolder fenceHolder = new GeofenceHolder(circle, marker, fence);
		mMarkers.put(marker, fenceHolder);
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
		Log.e(TAG, "Google Api Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
	}

	@Override
	public void onConnectionSuspended(int cause) {
		// The connection to Google Play services was lost for some reason. We call connect() to
		// attempt to re-establish the connection.
		Log.w(TAG, "Google Api Connection suspended");
		mGoogleApiClient.connect();
	}

	/**
	 * CALLBACKS *
	 */

	@Override
	public void onMapLongClick(LatLng pos) {
		GeofenceDialogFragment newFragment = GeofenceDialogFragment.newInstance(
				pos.latitude, pos.longitude);
		newFragment.show(getSupportFragmentManager(), TAG_DIALOG_ADD_GEOFENCE);
	}

	@Override
	public void onResult(Status status) {
		if (status.isSuccess()) {
			Controller controller = Controller.getInstance(this);
			String userId = controller.getActualUser().getId();

			Log.i(TAG, "Geoefence added");
			drawGeofence(mAddGeofence);
			controller.getGeofenceModel().addGeofence(userId, mAddGeofence);
			mAddGeofence = null;


		} else {
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

	@Override
	public void onCreateGeofence(String name, int radius, double lat, double lon) {
		SimpleGeofence geofence = new SimpleGeofence(name, lat, lon, radius, this);
		addGeofence(geofence);
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
				// Getting a maximum of 1 Address that matches the input text (best match)
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

//				setSupportProgressBarIndeterminateVisibility(false);

				// Locate the first location
				if (i == 0) {
					mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, MAP_ZOOM));
				}
			}
		}
	}

	class ActionModeGeofence implements ActionMode.Callback {

		GeofenceHolder mHolder;

		ActionModeGeofence(GeofenceHolder holder) {
			mHolder = holder;
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.geofence_actionmode, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			if (item.getItemId() == R.id.geofence_menu_del) {
				deleteGeofence(mHolder);
			} else if (item.getItemId() == R.id.geofence_menu_create_watchdog) {
				Intent intent = new Intent(MapGeofenceActivity.this, WatchdogEditRuleActivity.class);
				intent.putExtra(WatchdogEditRuleActivity.EXTRA_GEOFENCE_ID_PICKED, mHolder.getGeofence().getId());
				intent.putExtra(WatchdogEditRuleActivity.EXTRA_IS_NEW, true);
				startActivity(intent);
			}

			mode.finish();
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mActionMode = null;
		}
	}

	public class GeofenceHolder {
		private Circle mCircle;
		private Marker mMarker;
		private SimpleGeofence mGeofence;

		public GeofenceHolder(Circle circle, Marker marker, SimpleGeofence geofence) {
			this.mCircle = circle;
			this.mMarker = marker;
			this.mGeofence = geofence;
		}

		public Marker getMarker() {
			return mMarker;
		}

		public SimpleGeofence getGeofence() {
			return mGeofence;
		}

		public Circle getCircle() {
			return mCircle;
		}
	}
}
