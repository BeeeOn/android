package com.rehivetech.beeeon.persistence.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import com.rehivetech.beeeon.geofence.SimpleGeofence;
import com.rehivetech.beeeon.persistence.database.entry.GeofenceEntry;
import com.rehivetech.beeeon.util.Log;

import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.List;

/**
 * Created by Martin on 17. 3. 2015.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

	/**
	 * Name of file woth database
	 */
	public static final String DATABASE_NAME = "BeeeOn.db";
	private static final String TAG = SQLiteOpenHelper.class.getSimpleName();
	/**
	 * Database version
	 */
	private static final int DATABASE_VERSION = 1;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	protected static SimpleGeofence cursorToGeofence(Cursor cursor) throws IllegalFormatException {
		// FIXME: SimpleGeofence does not have constructor with these parameters
		return new SimpleGeofence(// cursor.getString(cursor.getColumnIndexOrThrow(GeofenceEntry.COLUMN_GEO_ID)),
				cursor.getDouble(cursor.getColumnIndexOrThrow(GeofenceEntry.COLUMN_LAT)),
				cursor.getDouble(cursor.getColumnIndexOrThrow(GeofenceEntry.COLUMN_LONG)),
				cursor.getFloat(cursor.getColumnIndexOrThrow(GeofenceEntry.COLUMN_RADIUS)));
	}

	public void onCreate(SQLiteDatabase db) {
		Log.d(TAG, "Creating database");
		db.execSQL(GeofenceEntry.CREATE_TABLE);
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(TAG, "Upgrading database from version " + String.valueOf(oldVersion) + " to version " + String.valueOf(newVersion));
	}

	@Nullable
	public SimpleGeofence getGeofence(String userId, String geofenceId) {
		SQLiteDatabase db = this.getReadableDatabase();

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
		SQLiteDatabase db = this.getWritableDatabase();

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

		SQLiteDatabase db = this.getReadableDatabase();

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

		return geofenceList;
	}

}