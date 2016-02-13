package com.rehivetech.beeeon.gui.adapter.dashboard.items;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.rehivetech.beeeon.household.device.ModuleLog;

/**
 * Created by martin on 9.2.16.
 */
public class OverviewGraphItem extends BaseItem implements Parcelable{

	@SerializedName("moduleId")
	public String mAbsoluteModuleId;

	@SerializedName("dataType")
	public ModuleLog.DataType mDataType;

	public OverviewGraphItem(String name, String gateId, String absoluteModuleId, ModuleLog.DataType dataType) {
		super(name, gateId);
		mAbsoluteModuleId = absoluteModuleId;
		mDataType = dataType;
	}

	protected OverviewGraphItem(Parcel in) {
		super(in);
		mAbsoluteModuleId = in.readString();
		String dataType = in.readString();

		for (ModuleLog.DataType logDataType : ModuleLog.DataType.values()) {
			if (dataType.equals(logDataType.getId())) {
				mDataType = logDataType;
				break;
			}
		}
	}


	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeString(mAbsoluteModuleId);
		dest.writeString(mDataType.getId());
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Creator<OverviewGraphItem> CREATOR = new Creator<OverviewGraphItem>() {
		@Override
		public OverviewGraphItem createFromParcel(Parcel in) {
			return new OverviewGraphItem(in);
		}

		@Override
		public OverviewGraphItem[] newArray(int size) {
			return new OverviewGraphItem[size];
		}
	};

	public String getAbsoluteModuleId() {
		return mAbsoluteModuleId;
	}

	public ModuleLog.DataType getDataType() {
		return mDataType;
	}
}
