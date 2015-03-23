/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rehivetech.beeeon.geofence;

import com.google.android.gms.location.Geofence;

/**
 * A single Geofence object, defined by its center (latitude and longitude position) and radius.
 */
public class SimpleGeofence {
	// Instance variables
	private final String mId;
	private final double mLatitude;
	private final double mLongitude;
	private final float mRadius;
	private long mExpirationDuration;
	private int mTransitionType;

	/**
	 * @param latitude   Latitude of the Geofence's center. The value is not checked for validity.
	 * @param longitude  Longitude of the Geofence's center. The value is not checked for validity.
	 * @param radius     Radius of the geofence circle. The value is not checked for validity
	 * @param expiration Geofence expiration duration in milliseconds The value is not checked for
	 *                   validity.
	 * @param transition Type of Geofence transition. The value is not checked for validity.
	 */
	public SimpleGeofence(
			double latitude,
			double longitude,
			float radius,
			long expiration,
			int transition) {
		// Set the instance fields from the constructor

		// An identifier for the geofence
		this.mId = createGeofenceId();

		// Center of the geofence
		this.mLatitude = latitude;
		this.mLongitude = longitude;

		// Radius of the geofence, in meters
		this.mRadius = radius;

		// Expiration time in milliseconds
		this.mExpirationDuration = expiration;

		// Transition type
		this.mTransitionType = transition;
	}

	/**
	 * Transition is set to enter and exit transition. Expiration is set to never expire.
	 *
	 * @param latitude  Latitude of the Geofence's center. The value is not checked for validity.
	 * @param longitude Longitude of the Geofence's center. The value is not checked for validity.
	 * @param radius    Radius of the geofence circle. The value is not checked for validity
	 */
	public SimpleGeofence(
			double latitude,
			double longitude,
			float radius) {
		// Call default constructor and set default values
		this(latitude, longitude, radius, Geofence.NEVER_EXPIRE,
				Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT);
	}

	// Instance field getters

	/**
	 * Create geofence ID which is unique. It contains device ID and timestamp.
	 *
	 * @return Unique geofence ID per user. Ex. deviceId_timestamp
	 */
	private static String createGeofenceId() {
		final String timestamp = String.valueOf(System.currentTimeMillis());
		//FIXME
		final String deviceId = "";
		final String separator = "_";


		return deviceId + separator + timestamp;
	}

	/**
	 * Get the geofence ID
	 *
	 * @return A SimpleGeofence ID
	 */
	public String getId() {
		return mId;
	}

	/**
	 * Get the geofence latitude
	 *
	 * @return A latitude value
	 */
	public double getLatitude() {
		return mLatitude;
	}

	/**
	 * Get the geofence longitude
	 *
	 * @return A longitude value
	 */
	public double getLongitude() {
		return mLongitude;
	}

	/**
	 * Get the geofence radius
	 *
	 * @return A radius value
	 */
	public float getRadius() {
		return mRadius;
	}

	/**
	 * Get the geofence expiration duration
	 *
	 * @return Expiration duration in milliseconds
	 */
	public long getExpirationDuration() {
		return mExpirationDuration;
	}

	/**
	 * Get the geofence transition type
	 *
	 * @return Transition type (see Geofence)
	 */
	public int getTransitionType() {
		return mTransitionType;
	}

	/**
	 * Creates a Location Services Geofence object from a
	 * SimpleGeofence.
	 *
	 * @return A Geofence object
	 */
	public Geofence toGeofence() {
		// Build a new Geofence object
		return new Geofence.Builder()
				.setRequestId(getId())
				.setTransitionTypes(mTransitionType)
				.setCircularRegion(
						getLatitude(),
						getLongitude(),
						getRadius())
				.setExpirationDuration(mExpirationDuration)
				.build();
	}
}
