package com.rehivetech.beeeon.geofence;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Martin on 16. 4. 2015.
 */

public class ManageGeofenceService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {
	public static final String TAG = ManageGeofenceService.class.getSimpleName();

	/**
	 * Extra intent for string value which represents user ID.
	 */
	public static final String EXTRA_USER_ID = "extra_user_id";

	/**
	 * Extra intent for boolean value if register/unregister geofence.
	 * True for register user geofences, false for unregister user geofences.
	 */
	public static final String EXTRA_REGISTER = "extra_register";

	private GoogleApiClient mGoogleApiClient;

	private boolean mIsRegister;

	private List<SimpleGeofence> mGeofenceList;

	private PendingIntent mGeofencePendingIntent = null;

	@Override
	public void onCreate() {
		Log.i(TAG, "Geofence: onCreate");
		// Cannot (un)register geofence without Google Play Services
		// Or don't (un)register geofence if demo mode
		if (!Utils.isGooglePlayServicesAvailable(ManageGeofenceService.this) || Controller.isDemoMode()) {
			Log.w(TAG, "Services unavailable or demo mode -> exit");
			stopSelf();
			return;
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "service starting");

		Log.i(TAG, "(Un)registering all geofences");


		final String userId = intent.getStringExtra(EXTRA_USER_ID);
		if (userId == null || userId.isEmpty()) {
			Log.e(TAG, "User ID null or empty -> exit");
			stopSelf();
			return START_STICKY;
		}

		mGeofenceList = Controller.getInstance(ManageGeofenceService.this).getGeofenceModel().getAllGeofences(userId);
		if (mGeofenceList == null || mGeofenceList.isEmpty()) {
			Log.i(TAG, "Nothing to (un)register -> exit");
			// nothing to (un)register
			stopSelf();
			return START_STICKY;
		}

		buildGoogleApiClient();

		stopSelf();


		// If we get killed, after returning from here, restart
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// We don't provide binding, so return null
		return null;
	}

	private void unregister() {
		List<String> geofenceIds = new ArrayList<>();
		for (SimpleGeofence geofence : mGeofenceList) {
			Log.i(TAG, "Unregistering geofence: " + geofence.getName()+"-"+geofence.getId());
			geofenceIds.add(geofence.getId());
		}
		LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, geofenceIds).
				setResultCallback(this);

	}

	private void register() {
		Log.i(TAG, "Registering geofences");
		LocationServices.GeofencingApi.addGeofences(
				mGoogleApiClient,
				// The GeofenceRequest object.
				GeofenceHelper.getGeofencingRequest(mGeofenceList),
				// A pending intent that that is reused when calling removeGeofences(). This
				// pending intent is used to generate an intent when a matched geofence
				// transition is observed.
				GeofenceHelper.getGeofencePendingIntent(mGeofencePendingIntent, ManageGeofenceService.this)
		).setResultCallback(this); // Result processed in onResult().
	}

	private synchronized void buildGoogleApiClient() {
		mGoogleApiClient = new GoogleApiClient.Builder(ManageGeofenceService.this)
				.addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
					@Override
					public void onConnected(Bundle bundle) {
						Log.e(TAG, "Geofence, poseru se");
					}

					@Override
					public void onConnectionSuspended(int i) {
						Log.e(TAG, "Geofence, poseru se");
					}
				})
				.addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
					@Override
					public void onConnectionFailed(ConnectionResult connectionResult) {
						Log.e(TAG, "Geofence, poseru se");
					}
				})
				.addApi(LocationServices.API)
				.build();
	}

	/**
	 * Runs when a GoogleApiClient object successfully connects.
	 */
	@Override
	public void onConnected(Bundle connectionHint) {
		Log.i(TAG, "Connected to GoogleApiClient");

		if (mIsRegister) {
			register();
		} else {
			unregister();
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// Refer to the javadoc for ConnectionResult to see what error codes might be returned in
		// onConnectionFailed.
		Log.e(TAG, "Google Api Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
		stopSelf();
	}

	@Override
	public void onConnectionSuspended(int cause) {
		// The connection to Google Play services was lost for some reason. We call connect() to
		// attempt to re-establish the connection.
		Log.w(TAG, "Google Api Connection suspended");
		mGoogleApiClient.connect();
	}

	@Override
	public void onResult(Status status) {
		if (status.isSuccess()) {
			Log.i(TAG, "All geofences successfully " + (mIsRegister ? "registered" : "unregistered"));

		} else {
			Log.e(TAG, "Geofences WEREN'T  " + (mIsRegister ? "registered" : "unregistered"));
		}
		stopSelf();
	}

	@Override
	public void onDestroy() {
		Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();

		if (mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
		}
	}
}


