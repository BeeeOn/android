package com.rehivetech.beeeon.persistence;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.geofence.GeofenceHelper;
import com.rehivetech.beeeon.geofence.SimpleGeofence;
import com.rehivetech.beeeon.geofence.TransitionType;
import com.rehivetech.beeeon.network.DemoNetwork;
import com.rehivetech.beeeon.network.INetwork;
import com.rehivetech.beeeon.persistence.database.DatabaseHelper;
import com.rehivetech.beeeon.persistence.database.entry.GeofenceEntry;
import com.rehivetech.beeeon.util.Log;

import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Martin on 24. 3. 2015.
 */
public class GeofenceModel extends BaseModel
		implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

	private static final String TAG = GeofenceModel.class.getSimpleName();

	private GoogleApiClient mGoogleApiClient;

	private PendingIntent mGeofencePendingIntent = null;

	private final LinkedList<ManageGeofenceHolder> mQueue = new LinkedList<>();

	private final Context mContext;

	public GeofenceModel(INetwork network, Context context) {
		super(network);
		mContext = context;
	}

	private synchronized void buildGoogleApiClient() {
		Log.i(TAG, "Request to build Google API client");
		mGoogleApiClient = new GoogleApiClient.Builder(mContext)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API)
				.build();
		mGoogleApiClient.connect();
	}


	/**
	 * Runs when a GoogleApiClient object successfully connects.
	 */
	@Override
	public void onConnected(Bundle connectionHint) {
		Log.i(TAG, "Connected to GoogleApiClient");

		requestNextCall();
	}

	private void requestNextCall() {
		ManageGeofenceHolder holder = mQueue.pollLast();
		if (holder == null) {
			mGoogleApiClient.disconnect();
		} else if (holder.isRegisterMode) {
			registerGeofences(holder.userId);
		} else {
			unregisterGeofences(holder.userId);
		}
	}

	public void registerAllUserGeofence(String userId) {
		if (getAllGeofences(userId).size() < 1) {
			return;
		}

		mQueue.add(new ManageGeofenceHolder(true, userId));

//		if (mGoogleApiClient != null || !mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting()) {
			buildGoogleApiClient();
//		}
	}


	public void unregisterAllUserGeofence(String userId) {
		if (getAllGeofences(userId).size() < 1) {
			return;
		}

		mQueue.add(new ManageGeofenceHolder(false, userId));

//		if (mGoogleApiClient == null || !mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting()) {
			buildGoogleApiClient();
//		}
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

	@Override
	public void onResult(Status status) {
		if (status.isSuccess()) {
			Log.i(TAG, "All geofences successfully egistered unregistered: "+status.getStatusMessage());
			requestNextCall();
		} else {
			Log.e(TAG, "Geofences WEREN'T  unregistered: " + status.getStatusMessage());
		}

	}

	protected static SimpleGeofence cursorToGeofence(Cursor cursor) throws IllegalFormatException {
		return new SimpleGeofence(cursor.getString(cursor.getColumnIndexOrThrow(GeofenceEntry.COLUMN_GEO_ID)),
				cursor.getString(cursor.getColumnIndexOrThrow(GeofenceEntry.COLUMN_NAME)),
				cursor.getDouble(cursor.getColumnIndexOrThrow(GeofenceEntry.COLUMN_LAT)),
				cursor.getDouble(cursor.getColumnIndexOrThrow(GeofenceEntry.COLUMN_LONG)),
				cursor.getFloat(cursor.getColumnIndexOrThrow(GeofenceEntry.COLUMN_RADIUS)));
	}

	@Nullable
	public SimpleGeofence getGeofence(String userId, String geofenceId) {
		SQLiteDatabase db = DatabaseHelper.getInstance(mContext).getReadableDatabase();

		Cursor cursor = db.query(GeofenceEntry.TABLE_NAME, // table
				null, // column names
				GeofenceEntry.COLUMN_GEO_ID + " = ?" + " and " +
						GeofenceEntry.COLUMN_USER_ID + " = ?", //  selections
				new String[]{geofenceId, userId}, // selections args
				null, //  group by
				null, //  having
				null, //  order by
				null); //  limit

		if (cursor == null) {
			return null;
		}

		if (cursor.getCount() < 1) {
			return null;
		}

		cursor.moveToFirst();

		return cursorToGeofence(cursor);
	}

	public void deleteGeofence(String userId, String geofenceId) {
		Log.d(TAG, "Deleting geofence: " + geofenceId);
		SQLiteDatabase db = DatabaseHelper.getInstance(mContext).getWritableDatabase();

		db.delete(GeofenceEntry.TABLE_NAME, GeofenceEntry.COLUMN_GEO_ID + " = ?" + " and " +
						GeofenceEntry.COLUMN_USER_ID + " = ?", //  selections
				new String[]{geofenceId, userId});

		db.close();
	}

	/**
	 * Get all user's geofences.
	 *
	 * @param userId ID of logged user
	 * @return List of geofences. If no geofence is registred, empty list is returned.
	 */
	public List<SimpleGeofence> getAllGeofences(String userId) {
		List<SimpleGeofence> geofenceList = new ArrayList<>();

		SQLiteDatabase db = DatabaseHelper.getInstance(mContext).getReadableDatabase();

		Cursor cursor = db.query(GeofenceEntry.TABLE_NAME, // table
				null, // column names
				GeofenceEntry.COLUMN_USER_ID + " = ?", //  selections
				new String[]{userId}, // selections args
				null, //  group by
				null, //  having
				null, //  order by
				null); //  limit

		while (cursor.moveToNext()) {
			geofenceList.add(cursorToGeofence(cursor));
		}
		cursor.close();

		return geofenceList;
	}

	/**
	 * Control if actual user has the geofence registered.
	 *
	 * @param userId
	 * @param geofenceId Geofence ID which is unique per user for all devices
	 * @return <code>True</code> if actual user has geofence registered. <code>False</code> otherwise.
	 */
	public boolean exist(String userId, String geofenceId) {
		SQLiteDatabase db = DatabaseHelper.getInstance(mContext).getReadableDatabase();

		Cursor cursor = db.query(GeofenceEntry.TABLE_NAME, // table
				null, // column names
				GeofenceEntry.COLUMN_USER_ID + " = ? AND " + GeofenceEntry.COLUMN_GEO_ID + " = ?", //  selections
				new String[]{userId, geofenceId}, // selections args
				null, //  group by
				null, //  having
				null, //  order by
				null); //  limit

		// first item exist?
		boolean exist = cursor.moveToNext();

		cursor.close();

		return exist;
	}

	public void deleteDemoData() {
		Log.d(TAG, "Deleting all geofence demo data.");
		SQLiteDatabase db = DatabaseHelper.getInstance(mContext).getWritableDatabase();

		db.delete(GeofenceEntry.TABLE_NAME, GeofenceEntry.COLUMN_USER_ID + " = ?",
				new String[]{DemoNetwork.DEMO_USER_ID});

		db.close();
	}

	public void addGeofence(String userId, SimpleGeofence geofence) {
		Log.d(TAG, "Adding geofence to DB: " + geofence.getId());
		ContentValues values = new ContentValues();

		values.put(GeofenceEntry.COLUMN_USER_ID, userId);
		values.put(GeofenceEntry.COLUMN_GEO_ID, geofence.getId());
		values.put(GeofenceEntry.COLUMN_LAT, geofence.getLatitude());
		values.put(GeofenceEntry.COLUMN_LONG, geofence.getLongitude());
		values.put(GeofenceEntry.COLUMN_RADIUS, geofence.getRadius());
		values.put(GeofenceEntry.COLUMN_NAME, geofence.getName());

		SQLiteDatabase db = DatabaseHelper.getInstance(mContext).getWritableDatabase();

		db.insert(GeofenceEntry.TABLE_NAME, null, values);

		db.close();
	}


	/**
	 * This CAN'T be called on UI thread!
	 *
	 * @param geofenceId Geofence ID which is unique per user for all devices
	 * @param type
	 */
	public void setPassBorder(String geofenceId, TransitionType type) {
		Log.i(TAG, "Passing geofence and seding to server");
		try {
			mNetwork.passBorder(geofenceId, type.getName());
		} catch (AppException e) {
			Log.e(TAG, "PassBorder network error: " + e.getLocalizedMessage());
		}
	}

	private void unregisterGeofences(String userId) {

		LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, getAllGeofenceId(userId)).
				setResultCallback(this);

	}

	private List<String> getAllGeofenceId (String userId) {
		List<String> geofenceIds = new ArrayList<>();
		for (SimpleGeofence geofence : getAllGeofences(userId)) {
			Log.i(TAG, "Unregistering geofence: " + geofence.getName() + "-" + geofence.getId());
			geofenceIds.add(geofence.getId());
		}
		return geofenceIds;
	}


	private void registerGeofences(String userId) {
		Log.i(TAG, "Registering geofences");
		LocationServices.GeofencingApi.addGeofences(
				mGoogleApiClient,
				// The GeofenceRequest object.
				GeofenceHelper.getGeofencingRequest(getAllGeofences(userId)),
				// A pending intent that that is reused when calling removeGeofences(). This
				// pending intent is used to generate an intent when a matched geofence
				// transition is observed.
				GeofenceHelper.getGeofencePendingIntent(mGeofencePendingIntent, mContext)
		).setResultCallback(this); // Result processed in onResult().
	}


	private class ManageGeofenceHolder {
		public boolean isRegisterMode;
		public String userId;

		public ManageGeofenceHolder(boolean isRegisterMode, String userId) {
			this.isRegisterMode = isRegisterMode;
			this.userId = userId;
		}
	}
}
