package com.rehivetech.beeeon.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import com.rehivetech.beeeon.geofence.SimpleGeofence;
import com.rehivetech.beeeon.persistence.database.DatabaseHelper;
import com.rehivetech.beeeon.persistence.database.entry.GeofenceEntry;
import com.rehivetech.beeeon.util.Log;

import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.List;

/**
 * Created by Martin on 24. 3. 2015.
 */
public class GeofenceModel {

	private static final String TAG = GeofenceModel.class.getSimpleName();

	Context mContext;

	public GeofenceModel(Context context) {
		mContext = context;
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

		cursor.moveToFirst();

		return cursorToGeofence(cursor);
	}

	public void deleteGeofence(String userId, String geofenceId) {
		Log.d(TAG, "Deleting geofence: " + geofenceId);
		SQLiteDatabase db = DatabaseHelper.getInstance(mContext).getWritableDatabase();

		db.delete(geofenceId, GeofenceEntry.COLUMN_GEO_ID + " = ?" + " and " +
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

	public void addGeofence(String userId, SimpleGeofence geofence) {
		Log.d(TAG, "Adding geofence to DB: " + geofence.getId());
		ContentValues values = new ContentValues();

		values.put(GeofenceEntry.COLUMN_USER_ID, userId);
		values.put(GeofenceEntry.COLUMN_GEO_ID,geofence.getId());
		values.put(GeofenceEntry.COLUMN_LAT,geofence.getLatitude());
		values.put(GeofenceEntry.COLUMN_LONG,geofence.getLongitude());
		values.put(GeofenceEntry.COLUMN_RADIUS,geofence.getRadius());
		values.put(GeofenceEntry.COLUMN_NAME,geofence.getName());

		SQLiteDatabase db = DatabaseHelper.getInstance(mContext).getWritableDatabase();

		db.insert(GeofenceEntry.TABLE_NAME, null, values);

		db.close();
	}

}
