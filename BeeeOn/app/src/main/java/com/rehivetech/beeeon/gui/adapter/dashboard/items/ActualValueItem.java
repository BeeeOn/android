package com.rehivetech.beeeon.gui.adapter.dashboard.items;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by martin on 21.1.16.
 */
public class ActualValueItem extends BaseItem implements Parcelable {

	@SerializedName("deviceId")
	private String mDeviceId;

	@SerializedName("moduleId")
	private String mModuleId;

	public ActualValueItem(String name, String gateId, String deviceId, String moduleId) {
		super(name, gateId);

		mDeviceId = deviceId;
		mModuleId = moduleId;
	}

	protected ActualValueItem(Parcel in) {
		super(in);
		mDeviceId = in.readString();
		mModuleId = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeString(mDeviceId);
		dest.writeString(mModuleId);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Creator<ActualValueItem> CREATOR = new Creator<ActualValueItem>() {
		@Override
		public ActualValueItem createFromParcel(Parcel in) {
			return new ActualValueItem(in);
		}

		@Override
		public ActualValueItem[] newArray(int size) {
			return new ActualValueItem[size];
		}
	};

	public String getDeviceId() {
		return mDeviceId;
	}

	public String getModuleId() {
		return mModuleId;
	}
}