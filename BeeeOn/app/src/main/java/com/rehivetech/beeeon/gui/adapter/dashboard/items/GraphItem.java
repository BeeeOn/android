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

	@SerializedName("absoluteModuleIds")
	private List<String> mAbsoluteModuleIds;

	@SerializedName("dataRange")
	private int mDataRange;

	public GraphItem(String name, String gateId, List<String> absoluteModuleIds, @ChartHelper.DataRange int range) {
		super(name, gateId);

		mAbsoluteModuleIds = absoluteModuleIds;
		mDataRange = range;
	}

	protected GraphItem(Parcel in) {
		super(in);
		mAbsoluteModuleIds = in.createStringArrayList();
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

	public List<String> getAbsoluteModuleIds() {
		return mAbsoluteModuleIds;
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

		dest.writeStringList(mAbsoluteModuleIds);
		dest.writeInt(mDataRange);
	}
}
