package com.rehivetech.beeeon.persistence.database.entry;

import android.provider.BaseColumns;

/**
 * Created by Martin on 17. 3. 2015.
 */
public class GeofenceEntry implements BaseColumns {
	public static final String TABLE_NAME = "geofence";

	public static final String COLUMN_GEO_ID = "geo_id";
	public static final String COLUMN_USER_ID = "user_id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_LAT = "lat";
	public static final String COLUMN_LONG = "long";
	public static final String COLUMN_RADIUS = "radius";

	public static final String CREATE_TABLE =
			"CREATE TABLE " + TABLE_NAME + "(" +
					_ID + " integer primary key autoincrement, " +
					COLUMN_GEO_ID + " TEXT," +
					COLUMN_USER_ID + " TEXT," +
					COLUMN_NAME + " TEXT," +
					COLUMN_LONG + " REAL," +
					COLUMN_LAT + " REAL," +
					COLUMN_RADIUS + " REAL" +
					");";
}
