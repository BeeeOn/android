package com.rehivetech.beeeon.gamification;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.rehivetech.beeeon.IIdentifier;
import com.rehivetech.beeeon.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @author Jan Lamacz
 */
public class AchievementListItem implements IIdentifier, Comparable<AchievementListItem>, Parcelable {
	private static final String TAG = AchievementListItem.class.getSimpleName();

	private Context mContext;

	private String mAdapter;
	private String mPid;
	private String mAid;
	private String mName;
	private String mDescription;
	private String mCategory;
	private String mDateOther;
	private boolean mVisible;
	private Long mDate;
	private int mLevel;
	private int mPoints;
	private int mTotalProgress;
	private int mCurrentProgress;
	private AchievementListItem mParent = null;

	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

	public AchievementListItem(String id, String pid, String categoryId, int points, int totalProgress, int currentProgress, String visible, String date, String dateOther) {
		mAid = id;
		mPid = pid;
		mPoints = points;
		mCategory = categoryId;
		mTotalProgress = totalProgress;
		mCurrentProgress = currentProgress;
		mDateOther = dateOther;
		mDate = date.length() > 0 ? Long.parseLong(date)*1000 : 0;
		mVisible = visible.equals("t");
	}

	private AchievementListItem(Parcel in) {
		readFromParcel(in);
	}

	public boolean updateProgress() {
		mCurrentProgress++;
		if(mCurrentProgress == mTotalProgress) {
			mDate = System.currentTimeMillis();
			return true;
		}
		return false;
	}

	public void recountParentValues() {
		mLevel = 1;
		AchievementListItem parent = mParent;
		while(parent != null) {
			mLevel++;
			parent = parent.getParentItem();
		}
	}

	@Override
	public String getId() {return mAid;}
	public int getPoints() {return mPoints;}
	public int getLevel() { return mLevel; }
	public String getParent() {return mPid;}
	public String getCategory() {return mCategory;}
	public int getLevelCount() { return isDone() ? mLevel : 1; }
	public String getName() { return mContext == null ? "" : mName; }
	public String getDescription() { return mContext == null ? "" : mDescription; }
	public AchievementListItem getParentItem() { return mParent; }
	public String getProgressString() {
		if(mTotalProgress == 1) return "";
		return mCurrentProgress + "/" + mTotalProgress;
	}
	public int getTotalProgress() { return mTotalProgress; }
	public int getCurrentProgress() { return mCurrentProgress; }

	public boolean isVisible() { return mVisible; }
	public boolean isDone() {return mCurrentProgress >= mTotalProgress;}

	public void setParent(AchievementListItem parent) { mParent = parent; }
	public void setAid(String aid) { mAdapter = aid; }
	public void setContext(Context c) {
		mContext = c;
		mName = mContext.getString(mContext.getResources().getIdentifier("name_" + mAid, "string", mContext.getPackageName()));
		mDescription = mContext.getString(mContext.getResources().getIdentifier("desc_" + mAid, "string", mContext.getPackageName()));
	}

	public String getTime() {
		return dateFormat.format(new Date(mDate));
	}

	public String getDate() {
		Calendar today = Calendar.getInstance();
		Calendar compare = Calendar.getInstance();
		compare.setTimeInMillis(mDate);
		if(today.get(Calendar.YEAR) == compare.get(Calendar.YEAR)) {
			if(today.get(Calendar.DAY_OF_YEAR) == compare.get(Calendar.DAY_OF_YEAR))
				return mContext.getString(R.string.date_today);
			compare.add(Calendar.DAY_OF_YEAR, 1);
			if(today.get(Calendar.DAY_OF_YEAR) == compare.get(Calendar.DAY_OF_YEAR))
				return mContext.getString(R.string.date_yesterday);
		}
		return getTime();
	}

	/** Sorts achievements by date.
	 * Sorts list of achievements by date from oldest to newest
	 * and takes the not-completed ones to end of that list.
	 */
	@Override
	public int compareTo(@NonNull AchievementListItem another) {
		if(isDone()) {
			if(!another.isDone())	// Another isn't complete -> to top
				return -1;
			else
				return another.getTime().compareTo(getTime());
		}
		else {
			if(another.isDone()) return 1;
			else return isVisible() ? -1 : 1;
		}

	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mAid);
		dest.writeString(mPid);
		dest.writeString(mCategory);
		dest.writeLong(mDate);
		dest.writeString(mDateOther);
		dest.writeInt(mPoints);
		dest.writeInt(mTotalProgress);
		dest.writeInt(mCurrentProgress);
	}

	public void readFromParcel(Parcel in){
		mAid = in.readString();
		mPid = in.readString();
		mCategory = in.readString();
		mDate = in.readLong();
		mDateOther = in.readString();
		mPoints = in.readInt();
		mTotalProgress = in.readInt();
		mCurrentProgress = in.readInt();
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
