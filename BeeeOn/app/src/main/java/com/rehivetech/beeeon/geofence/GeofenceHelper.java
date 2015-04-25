package com.rehivetech.beeeon.geofence;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Martin on 16. 4. 2015.
 */
public final class GeofenceHelper {

	/**
	 * Private constructor to avoid instantiation
	 */
	private GeofenceHelper(){};

	/**
	 * Builds and returns a GeofencingRequest. Specifies one geofence to be monitored.
	 */
	static public GeofencingRequest getGeofencingRequest(final SimpleGeofence geofence) {
		List<Geofence> geofenceList = new ArrayList<>();
		geofenceList.add(geofence.toGeofence());

		return getGeofencingRequestCommon(geofenceList);
	}

	static public GeofencingRequest getGeofencingRequest(final List<SimpleGeofence> simpleGeofenceList) {
		List<Geofence> geofenceList = new ArrayList<>();
		for (SimpleGeofence simpleGofence : simpleGeofenceList) {
			geofenceList.add(simpleGofence.toGeofence());
		}
		return getGeofencingRequestCommon(geofenceList);
	}

	/**
	 * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
	 */
	static private GeofencingRequest getGeofencingRequestCommon(final List<Geofence> geofenceList) {

		GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

		// Add the geofences to be monitored by geofencing service.
		builder.addGeofences(geofenceList);

		// Return a GeofencingRequest.
		return builder.build();
	}

	static public PendingIntent getGeofencePendingIntent(final PendingIntent geofencePendingIntent, final Context context) {
		// Reuse the PendingIntent if we already have it.
		if (geofencePendingIntent != null) {
			return geofencePendingIntent;
		}
		Intent intent = new Intent(context, GeofenceIntentService.class);
		// We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
		// calling addGeofences() and removeGeofences().
		return PendingIntent.getService(context, 0, intent, PendingIntent.
				FLAG_UPDATE_CURRENT);
	}
}
