package cz.vutbr.fit.iha.activity;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

import cz.vutbr.fit.iha.R;

public class MapGeofenceActivity extends SherlockFragmentActivity {

	private GoogleMap mMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map_geofence);
		mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

		if (mMap != null) {

			// Sets the map type to be "hybrid"
			mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			mMap.setMyLocationEnabled(true);
		}
	}
}
