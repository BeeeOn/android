package com.rehivetech.beeeon.geofence;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.util.Log;

import java.util.List;

/**
 * Created by Martin on 17. 3. 2015.
 */
public class GeofenceIntentService extends IntentService {

	public final String TAG = GeofenceIntentService.this.getClass().getSimpleName();
	private Handler handler;

	public GeofenceIntentService() {
		super("geofence");
	}

	protected void onHandleIntent(Intent intent) {
		GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
		if (geofencingEvent.hasError()) {
//			String errorMessage = GeofenceErrorMessages.getErrorString(this,
//					geofencingEvent.getErrorCode());
			//Log.e(TAG, errorMessage);
			Log.e(TAG, "Geofence event error");
			return;
		}

		// v demo modu neodesilat
		if (Controller.isDemoMode()) {
			return;
		}

		// Get the transition type.
		int geofenceTransition = geofencingEvent.getGeofenceTransition();

		// Test that the reported transition was of interest.
		if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
				geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
			List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

			TransitionType type = TransitionType.fromInt(geofenceTransition);
			sendGeofencesToServer(triggeringGeofences, type);
		} else {
			// Log the error.
			Log.e(TAG, "Wrong transititon type");
		}
	}

	private void sendGeofencesToServer(List<Geofence> triggeringGeofences, final TransitionType type) {
		final Controller controller = Controller.getInstance(this);
		String userId = controller.getActualUser().getId();

		Log.i(TAG, "Sending new geofence to server");
		for (final Geofence fence : triggeringGeofences) {
			if (controller.getGeofenceModel().exist(userId, fence.getRequestId())) {
				Log.i(TAG, "Actual user is allowed to access this geofence.");
				Thread t = new Thread() {
					public void run() {
						controller.getGeofenceModel().setPassBorder(fence.getRequestId(), type);
					}
				};
				t.start();
			}
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		handler = new Handler();
		return super.onStartCommand(intent, flags, startId);
	}

}
