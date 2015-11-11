package com.rehivetech.beeeon.util;

/**
 * Created by Robert on 11.11.2015.
 */
public class GpsData {
	private int altitude;
	private double longitude;
	private double latitude;

	public int getAltitude() {
		return altitude;
	}

	public void setAltitude(int altitude) throws NumberFormatException {
		this.altitude = altitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) throws NumberFormatException {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) throws NumberFormatException {
		this.latitude = latitude;
	}
}
