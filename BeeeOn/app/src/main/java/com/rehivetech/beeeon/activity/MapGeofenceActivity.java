package com.rehivetech.beeeon.activity;

import java.io.IOException;
import java.util.List;

import android.content.Context;
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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

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
import com.rehivetech.beeeon.base.BaseActivity;
import com.rehivetech.beeeon.base.BaseApplicationActivity;
import com.rehivetech.beeeon.geofence.SimpleGeofence;
import com.rehivetech.beeeon.util.Log;

public class MapGeofenceActivity extends BaseApplicationActivity implements OnMapLongClickListener, OnMarkerDragListener,
		OnMapReadyCallback {

	private static final String TAG = "geofence";

	private static final float MAP_ZOOM = 17.5F;
	private static final int MAXIMUM_RET_ADDRESS = 3;

	private EditText mEditSearch;
    private SearchView mSearchView;

	private GoogleMap mMap;
    private Toolbar mToolbar;

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
		String provider = locationManager.getBestProvider(criteria, true);
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
		
		map.addMarker(new MarkerOptions().draggable(true).position(new LatLng(10, 10)).title("Hello world"));
	}

	public void addMarkerForFence(SimpleGeofence fence) {
		if (fence == null) {
			Log.e(TAG, "Geofence is null.");
			return;
		}
		mMap.addMarker(
				new MarkerOptions().position(new LatLng(fence.getLatitude(), fence.getLongitude()))
						.title("Fence " + fence.getId()).snippet("Radius: " + fence.getRadius())).showInfoWindow();

		// Instantiates a new CircleOptions object + center/radius
		CircleOptions circleOptions = new CircleOptions().center(new LatLng(fence.getLatitude(), fence.getLongitude()))
				.radius(fence.getRadius()).fillColor(0x40ff0000).strokeColor(Color.TRANSPARENT).strokeWidth(2);

		// Get back the mutable Circle
		Circle circle = mMap.addCircle(circleOptions);
		// more operations on the circle...
	}

    @Override
    protected void onAppResume() {

    }

    @Override
    protected void onAppPause() {

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

	/** CALLBACKS **/

	@Override
	public void onMapLongClick(LatLng arg0) {
		Toast.makeText(this, "Long Click", Toast.LENGTH_LONG).show();
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
}