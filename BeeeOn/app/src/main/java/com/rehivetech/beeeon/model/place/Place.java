package com.rehivetech.beeeon.model.place;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by martin on 26.3.16.
 */
public class Place implements Parcelable {

	@SerializedName("formatted_address")
	private String mAddress;

	@SerializedName("geometry")
	private Geometry mGeometry = new Geometry();

	@SerializedName("name")
	private String mName;

	public String getAddress() {
		return mAddress;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public void setCoordinates(double lat, double longt) {
		Location location = mGeometry.getLocation();
		location.setLat(lat);
		location.setLong(longt);
	}

	@Override
	public String toString() {
		return mAddress;
	}

	public double[] getCoordinates() {
		double[] coordinates = new double[2];
		coordinates[0] = mGeometry.getLocation().getLat();
		coordinates[1] = mGeometry.getLocation().getLong();

		return coordinates;
	}

	private class Geometry implements Parcelable {

		@SerializedName("location")
		private Location mLocation = new Location();

		public Location getLocation() {
			return mLocation;
		}


		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeParcelable(this.mLocation, flags);
		}

		public Geometry() {
		}

		protected Geometry(Parcel in) {
			this.mLocation = in.readParcelable(Location.class.getClassLoader());
		}

		public final Creator<Geometry> CREATOR = new Creator<Geometry>() {
			public Geometry createFromParcel(Parcel source) {
				return new Geometry(source);
			}

			public Geometry[] newArray(int size) {
				return new Geometry[size];
			}
		};
	}

	private class Location implements Parcelable {

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

		public void setLat(double lat) {
			mLat = lat;
		}

		public void setLong(double aLong) {
			mLong = aLong;
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeDouble(this.mLat);
			dest.writeDouble(this.mLong);
		}

		public Location() {
		}

		protected Location(Parcel in) {
			this.mLat = in.readDouble();
			this.mLong = in.readDouble();
		}

		public final Creator<Location> CREATOR = new Creator<Location>() {
			public Location createFromParcel(Parcel source) {
				return new Location(source);
			}

			public Location[] newArray(int size) {
				return new Location[size];
			}
		};
	}


	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.mAddress);
		dest.writeParcelable(this.mGeometry, flags);
		dest.writeString(this.mName);
	}

	public Place() {
	}

	protected Place(Parcel in) {
		this.mAddress = in.readString();
		this.mGeometry = in.readParcelable(Geometry.class.getClassLoader());
		this.mName = in.readString();
	}

	public static final Parcelable.Creator<Place> CREATOR = new Parcelable.Creator<Place>() {
		public Place createFromParcel(Parcel source) {
			return new Place(source);
		}

		public Place[] newArray(int size) {
			return new Place[size];
		}
	};
}
