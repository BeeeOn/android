package com.rehivetech.beeeon.model.place;

import com.google.gson.annotations.SerializedName;

/**
 * Created by martin on 26.3.16.
 */
public class Place {

	@SerializedName("formatted_address")
	private String mAddress;

	@SerializedName("geometry")
	private Geometry mGeometry;

	@SerializedName("name")
	private String mName;

	public String getAddress() {
		return mAddress;
	}

	public String getName() {
		return mName;
	}

	@Override
	public String toString() {
		return mAddress;
	}

	public Double[] getCoordinates() {
		Double[] coordinates = new Double[2];
		coordinates[0] = mGeometry.getLocation().getLat();
		coordinates[1] = mGeometry.getLocation().getLong();

		return coordinates;
	}

	private class Geometry {

		@SerializedName("location")
		private Location mLocation;

		public Location getLocation() {
			return mLocation;
		}
	}

	private class Location {

		@SerializedName("lat")
		private double mLat;

		@SerializedName("lng")
		private double mLong;

		public double getLat() {
			return mLat;
		}

		public double getLong() {
			return mLong;
		}
	}
}
