package com.rehivetech.beeeon.household.device.units;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.StringRes;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseUnit {
	protected final List<BaseUnit.Item> mItems = new ArrayList<>();

	@StringRes
	int mPreferenceKey;

	public BaseUnit(@StringRes int preferenceKey) {
		mPreferenceKey = preferenceKey;
	}

	/**
	 * Get Item representing value chosen by user in settings.
	 *
	 * @param prefs
	 * @return user chosen Item or default Item, if user didn't chose it in settings yet.
	 */
	public abstract BaseUnit.Item fromSettings(SharedPreferences prefs);

	public abstract double convertValue(Item to, double value);

	public abstract double convertToDefaultValue(Item from, double value);

	public class Item implements Parcelable {
		static final int DEFAULT_ID = 0;

		public final Creator<Item> CREATOR = new Creator<Item>() {
			@Override
			public Item createFromParcel(Parcel source) {
				return new Item(source);
			}

			@Override
			public Item[] newArray(int size) {
				return new Item[0];
			}
		};

		private int mResUnitName;
		private int mResUnitShortName;
		private int mId;

		protected Item(@StringRes int id, int resUnitName, int resUnitShortName) {
			mId = id;
			mResUnitName = resUnitName;
			mResUnitShortName = resUnitShortName;
		}

		public Item(Parcel source) {
			mId = source.readInt();
			mResUnitName = source.readInt();
			mResUnitShortName = source.readInt();
		}

		/**
		 * Get short form for unit. For example for celsius you will get "°C".
		 *
		 * @param context It can be app context
		 * @return Short form for unit
		 */
		public String getStringUnit(Context context) {
			return context.getString(mResUnitShortName);
		}

		/**
		 * Get full name for unit. For example for celsius you will get "Celsius".
		 *
		 * @param context It can be app context
		 * @return String which
		 */
		public String getStringName(Context context) {
			return context.getString(mResUnitName);
		}

		/**
		 * Get full name with short form for unit. For example for celsius you will get "Celsius (°C)".
		 *
		 * @param context It can be app context
		 * @return String which
		 */
		public String getStringNameUnit(Context context) {
			return String.format("%s (%s)", getStringName(context), getStringUnit(context));
		}

		public int getId() {
			return mId;
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeInt(mId);
			dest.writeInt(mResUnitName);
			dest.writeInt(mResUnitShortName);
		}
	}
}
