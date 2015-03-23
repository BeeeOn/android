package com.rehivetech.beeeon.gamification;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.rehivetech.beeeon.IIdentifier;
import com.rehivetech.beeeon.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Jan Lamacz
 */
public class AchievementListItem implements IIdentifier, Comparable<AchievementListItem>, Parcelable {
	private static final String TAG = AchievementListItem.class.getSimpleName();

	private String id;
	private String mCategory;
	private String mName;
	private String mDescription;
	private String mDate;
	private int mPoints;

	public AchievementListItem(String id, String categoryId, String name, String description, int points, String date) {
		setId(id);
		setCategory(categoryId);
		setName(name);
		setDescription(description);
		setDate(date);
		setPoints(points);
	}

	private AchievementListItem(Parcel in) {
		readFromParcel(in);
	}

	public String getCategory() {return mCategory;}
	public void setCategory(String category) {this.mCategory = category;}

	public String getName() {return mName;}
	public void setName(String name) {this.mName = name;}

	public String getDescription() {return mDescription;}
	public void setDescription(String desc) {this.mDescription = desc;}

	public boolean isDone() {return mDate != null;}

	public String getDate() {return mDate;}
	public void setDate(String date) {this.mDate = date;}

	public int getPoints() {return mPoints;}
	public void setPoints(int points) {this.mPoints = points;}

	@Override
	public String getId() {return id;}
	public void setId(String id) {this.id = id;}

	/** Sorts achievements by date.
	 * Sorts list of achievements by date from oldest to newest
	 * and takes the not-completed ones to end of that list.
	 */
	@Override
	public int compareTo(@NonNull AchievementListItem another) {
		if (!isDone())	// Not completed -> to end
			return 1;
		else if(!another.isDone())	// Another isn't complete -> to start
			return -1;
		try{
			SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
			Date date1 = sdf.parse(getDate());
			Date date2 = sdf.parse(another.getDate());
			return date2.compareTo(date1);
		}catch(ParseException ex){
			Log.e(TAG, "Sorting exception");
			return 1;
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(mName);
		dest.writeString(mDescription);
		dest.writeString(mCategory);
		dest.writeString(mDate);
		dest.writeInt(mPoints);
	}

	public void readFromParcel(Parcel in){
		id = in.readString();
		mName = in.readString();
		mDescription = in.readString();
		mCategory = in.readString();
		mDate = in.readString();
		mPoints = in.readInt();
	}

	public static final Parcelable.Creator<AchievementListItem> CREATOR = new Parcelable.Creator<AchievementListItem>() {
		public AchievementListItem createFromParcel(Parcel in) {
			return new AchievementListItem(in);
		}

		public AchievementListItem[] newArray(int size) {
			return new AchievementListItem[size];
		}
	};

}
