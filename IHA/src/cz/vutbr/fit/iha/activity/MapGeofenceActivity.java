package cz.vutbr.fit.iha.activity;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

import cz.vutbr.fit.iha.R;

public class MapGeofenceActivity extends SherlockFragmentActivity {

	private GoogleMap mMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map_geofence);
		
		setSupportProgressBarIndeterminate(true);
		setProgressBarIndeterminateVisibility(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setIcon(R.drawable.ic_launcher_white);
		
		mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

		if (mMap != null) {

			// Sets the map type to be "hybrid"
			mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			mMap.setMyLocationEnabled(true);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		return false;
	}
}
