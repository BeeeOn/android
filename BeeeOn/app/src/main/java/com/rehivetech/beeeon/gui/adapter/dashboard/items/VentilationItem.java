package com.rehivetech.beeeon.gui.adapter.dashboard.items;

import android.os.Parcel;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by martin on 20.3.16.
 */
public class VentilationItem extends BaseItem {

	@SerializedName("outSideAbsoluteModuleId")
	private String mOutsideAbsoluteModuleId;

	@SerializedName("insideAbsoluteModuleId")
	private String mInSideAbsoluteModuleId;

	@SerializedName("coordinates")
	private double[] mLocation;

	public VentilationItem(String name, String gateId, @Nullable double[] location, @Nullable String outsideAbsoluteModuleId, String inSideAbsoluteModuleId) {
		super(name, gateId);
		mLocation = location;
		mOutsideAbsoluteModuleId = outsideAbsoluteModuleId;
		mInSideAbsoluteModuleId = inSideAbsoluteModuleId;
	}


	public String getOutsideAbsoluteModuleId() {
		return mOutsideAbsoluteModuleId;
	}

	public String getInSideAbsoluteModuleId() {
		return mInSideAbsoluteModuleId;
	}

	public double[] getLocation() {
		return mLocation;
	}

	public String getLatitiude() {
		return Double.toString(mLocation[0]);
	}

	public String getLongitiude() {
		return Double.toString(mLocation[1]);
	}
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeString(mOutsideAbsoluteModuleId);
		dest.writeString(mInSideAbsoluteModuleId);
		dest.writeDoubleArray(mLocation);
	}

	protected VentilationItem(Parcel in) {
		super(in);
		mOutsideAbsoluteModuleId = in.readString();
		mInSideAbsoluteModuleId = in.readString();
		mLocation = in.createDoubleArray();
	}

	public static final Creator<VentilationItem> CREATOR = new Creator<VentilationItem>() {
		public VentilationItem createFromParcel(Parcel source) {
			return new VentilationItem(source);
		}

		public VentilationItem[] newArray(int size) {
			return new VentilationItem[size];
		}
	};
}
