package com.rehivetech.beeeon.gui.adapter.dashboard.items;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.rehivetech.beeeon.util.ChartHelper;

import java.util.List;

/**
 * Created by martin on 21.1.16.
 */

public class GraphItem extends BaseItem implements Parcelable {

	@SerializedName("deviceIds")
	private List<String> mDeviceIds;

	@SerializedName("moduleIds")
	private List<String> mModuleIds;

	@SerializedName("dataRange")
	private int mDataRange;

	public GraphItem(String name, String gateId, List<String> deviceIds, List<String> moduleIds, @ChartHelper.DataRange int range) {
		super(name, gateId);

		mDeviceIds = deviceIds;
		mModuleIds = moduleIds;
		mDataRange = range;
	}

	protected GraphItem(Parcel in) {
		super(in);
		mDeviceIds = in.createStringArrayList();
		mModuleIds = in.createStringArrayList();
		mDataRange = in.readInt();
	}

	public static final Creator<GraphItem> CREATOR = new Creator<GraphItem>() {
		@Override
		public GraphItem createFromParcel(Parcel in) {
			return new GraphItem(in);
		}

		@Override
		public GraphItem[] newArray(int size) {
			return new GraphItem[size];
		}
	};

	public List<String> getDeviceIds() {
		return mDeviceIds;
	}

	public List<String> getModuleIds() {
		return mModuleIds;
	}

	@ChartHelper.DataRange
	public int getDataRange() {
		return mDataRange;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);

		dest.writeStringList(mDeviceIds);
		dest.writeStringList(mModuleIds);
		dest.writeInt(mDataRange);
	}
}
