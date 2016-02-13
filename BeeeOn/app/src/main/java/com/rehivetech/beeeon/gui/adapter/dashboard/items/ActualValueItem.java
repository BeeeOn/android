package com.rehivetech.beeeon.gui.adapter.dashboard.items;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by martin on 21.1.16.
 */
public class ActualValueItem extends BaseItem implements Parcelable {

	@SerializedName("absoluteModuleId")
	private String mAbsoluteModuleId;

	public ActualValueItem(String name, String gateId, String absoluteModuleId) {
		super(name, gateId);

		mAbsoluteModuleId = absoluteModuleId;
	}

	protected ActualValueItem(Parcel in) {
		super(in);
		mAbsoluteModuleId = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeString(mAbsoluteModuleId);
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

	public String getAbsoluteModuleId() {
		return mAbsoluteModuleId;
	}
}