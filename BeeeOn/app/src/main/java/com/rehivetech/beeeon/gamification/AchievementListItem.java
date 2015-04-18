package com.rehivetech.beeeon.gamification;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.rehivetech.beeeon.IIdentifier;
import com.rehivetech.beeeon.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @author Jan Lamacz
 */
public class AchievementListItem implements IIdentifier, Comparable<AchievementListItem>, Parcelable {
	private static final String TAG = AchievementListItem.class.getSimpleName();

	private String id;
	private String mPid;
	private String mAid;
	private String mCategory;
	private String mName;
	private String mDescription;
	private String mDate;
	private int mPoints;
	private int mTotalProgress;
	private int mCurrentProgress;

	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

	public AchievementListItem(String id, String categoryId, String name, String description, int points, String date) {
		setId(id);
		setCategory(categoryId);
		setName(name);
		setDescription(description);
		setPoints(points);
		setDate(date);
	}

	public AchievementListItem(String id, String pid, String categoryId, int points, int totalProgrss, int currentProgress, String date) {
		setId(id);
		setCategory(categoryId);
		setPoints(points);
		setDate(date);
		mPid = pid;
		mTotalProgress = totalProgrss;
		mCurrentProgress = currentProgress;
	}

	private AchievementListItem(Parcel in) {
		readFromParcel(in);
	}


	public String getAid() {
		return mAid;
	}

	public void setAid(String aid) {
		mAid = aid;
	}

	public String getPid() {
		return mPid;
	}

	public void setPid(String pid) {
		mPid = pid;
	}

	public int getTotalProgress() {
		return mTotalProgress;
	}

	public void setTotalProgress(int totalProgress) {
		mTotalProgress = totalProgress;
	}

	public int getCurrentProgress() {
		return mCurrentProgress;
	}

	public void setCurrentProgress(int currentProgress) {
		mCurrentProgress = currentProgress;
	}

	public String getCategory() {return mCategory;}
	public void setCategory(String category) {this.mCategory = category;}

	public String getName() {return mName;}
	public void setName(String name) {this.mName = name;}

	public String getDescription() {return mDescription;}
	public void setDescription(String desc) {this.mDescription = desc;}

	public boolean isDone() {return mDate != null;}

	public void setDate(String date) {this.mDate = date;}
	public Date getTime() {
		try{return dateFormat.parse(this.mDate);}
		catch (ParseException e) {Log.e(TAG, "Date parse Exception!");return null;}
	}

	public String getDate() {
		Calendar today = Calendar.getInstance();
		Calendar compare = Calendar.getInstance();
		compare.setTime(getTime());
		if(today.get(Calendar.YEAR) == compare.get(Calendar.YEAR)) {
			if(today.get(Calendar.DAY_OF_YEAR) == compare.get(Calendar.DAY_OF_YEAR))
				return "Today";
			compare.add(Calendar.DAY_OF_YEAR, 1);
			if(today.get(Calendar.DAY_OF_YEAR) == compare.get(Calendar.DAY_OF_YEAR))
				return "Yesterday";
		}
		return mDate;
	}

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
		else
			return another.getTime().compareTo(getTime());
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
