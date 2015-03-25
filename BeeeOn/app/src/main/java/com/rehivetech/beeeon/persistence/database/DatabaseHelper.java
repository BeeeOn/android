package com.rehivetech.beeeon.persistence.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.rehivetech.beeeon.persistence.database.entry.GeofenceEntry;
import com.rehivetech.beeeon.util.Log;

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

	private static DatabaseHelper sInstance;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public static DatabaseHelper getInstance(Context context) {
		if (sInstance == null) {
			synchronized (DatabaseHelper.class) {
				if (sInstance == null) {
					sInstance = new DatabaseHelper(context);
				}
			}
		}
		return sInstance;
	}

	public void onCreate(SQLiteDatabase db) {
		Log.d(TAG, "Creating database");
		db.execSQL(GeofenceEntry.CREATE_TABLE);
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(TAG, "Upgrading database from version " + String.valueOf(oldVersion) + " to version " + String.valueOf(newVersion));
	}

}