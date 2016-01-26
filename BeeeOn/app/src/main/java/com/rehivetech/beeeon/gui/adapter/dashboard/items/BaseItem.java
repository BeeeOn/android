package com.rehivetech.beeeon.gui.adapter.dashboard.items;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by martin on 21.1.16.
 */
public abstract class BaseItem implements Parcelable {

	@SerializedName("name")
	private String mName;

	@SerializedName("gateId")
	private String mGateId;

	public BaseItem(String name, String gateId) {
		mName = name;
		mGateId = gateId;
	}

	protected BaseItem(Parcel in) {
		mName = in.readString();
		mGateId = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mName);
		dest.writeString(mGateId);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Creator<BaseItem> CREATOR = new Creator<BaseItem>() {
		@Override
		public BaseItem createFromParcel(Parcel in) {
			throw new UnsupportedOperationException("Could not instantiate abstract class");
		}

		@Override
		public BaseItem[] newArray(int size) {
			return new BaseItem[size];
		}
	};

	public String getGateId() {
		return mGateId;
	}

	public String getName() {
		return mName;
	}
}