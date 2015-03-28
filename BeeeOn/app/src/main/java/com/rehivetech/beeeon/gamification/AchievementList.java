package com.rehivetech.beeeon.gamification;

import android.os.Parcel;
import android.os.Parcelable;

import com.rehivetech.beeeon.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jan Lamacz
 */
public class AchievementList implements Parcelable {
	private static final String TAG = AchievementList.class.getSimpleName();

	private List<AchievementListItem> mAchievementList;
	private int[] mComplete = new int[] {0,0,0}; // number of completed achievements in 3 categories
	private int[] mTotal = new int[] {0,0,0};	// number of all achievements in 3 categories
	private int mTotalPoints = 0;

	private AchievementListItem listItem;

	//
	public AchievementList() {
		Log.d(TAG, "constructor");
		mAchievementList = loadAllAchievements();
		recountValues();
	}

	// Parcel constructor called when passing this object over activities
	private AchievementList(Parcel in) {
		Log.d(TAG, "ParcelConstructor");
		readFromParcel(in);
		recountValues();
	}

	public List<AchievementListItem> getAchievements() {
		return  mAchievementList;
	}

	public int getTotalAchievements(String category) {
		return mTotal[Integer.parseInt(category)];
	}

	public int getCompletedAchievements(String category) {
		return mComplete[Integer.parseInt(category)];
	}

	public int getTotalPoints() {
		return mTotalPoints;
	}

	/** Counts number of reached stars in concrete category.
	 * Depending on progress done in each of the categories
	 * counts reached stars from 0 (almost none progress) to 3 (everything almost done)
	 *
	 * @param category - Id of category (String)
	 * @return int - number of achieved stars
	 */
	public int getStarsCount(String category) {
		float ratio = (float) mComplete[Integer.parseInt(category)] / mTotal[Integer.parseInt(category)];
		if(ratio >= 0.9)
			return 3;
		else if(ratio >= 0.6)
			return 2;
		else if(ratio >= 0.3)
			return 1;
		else return 0;
	}

	/** Counts users level.
	 * Level has (yet) significance and is (yet) is "just for fun"
	 * Level counts as 1 + 1 level for each gained star.
	 * @link getStarsCount()
	 *
	 * @return int level
	 */
	public int getLevel() {
		int level = 1;
		for(int i = 0; i < mTotal.length; i++)
			level += getStarsCount(String.valueOf(i));
		return level;
	}

	/** Calculates number of achievements in all categories.
	 * Counts number of total and completed achievements in all (3) categories
	 * and number of totally earned points.
	 */
	private void recountValues() {
		AchievementListItem achievement;
		for (int i = 0; i < mAchievementList.size(); i++) {
			achievement = mAchievementList.get(i);
			mTotal[Integer.parseInt(achievement.getCategory())]++;
			if (achievement.isDone()) {
				mComplete[Integer.parseInt(achievement.getCategory())]++;
				mTotalPoints += achievement.getPoints();
			}
		}
	}

	private List<AchievementListItem> loadAllAchievements() {
		Log.d(TAG, "AchievementListInit()");

		// TODO Download this list from server
		List<AchievementListItem> rulesList = new ArrayList<>();
		rulesList.add(new AchievementListItem("1", "2", "Getting started", "Activate your first adapter.", 10, "20.5.2015"));
		rulesList.add(new AchievementListItem("2", "2", "It's hot in there!", "Activate your first temperature sensor.", 20, null));
		rulesList.add(new AchievementListItem("3", "1", "Connected", "Pair your account with Facebook.", 0, "15.4.2015"));
		rulesList.add(new AchievementListItem("4", "1", "Sharing the news", "Share status via BeeeOn.", 10, null));
		rulesList.add(new AchievementListItem("5", "1", "Twice more fun", "Invite your friends to household.", 5, null));
		rulesList.add(new AchievementListItem("6", "1", "One big family", "Have 5 or more people in your household.", 15, null));
		rulesList.add(new AchievementListItem("7", "0", "Guarding the household", "Set your first watchdog.", 25, "12.5.2015"));
		rulesList.add(new AchievementListItem("8", "0", "It's my choice", "Set your own custom view.", 0, null));
		rulesList.add(new AchievementListItem("9", "0", "Leader of the pack", "Set 5 or more watchdogs.", 15, "14.5.2015"));
		rulesList.add(new AchievementListItem("10", "0", "Explorer", "Explore all options in BeeeOn application.", 10, null));
		rulesList.add(new AchievementListItem("11", "0", "Achievement", "Get an achievement.", 5, null));
		rulesList.add(new AchievementListItem("12", "0", "Master of BeeeOn", "Get all achievements.", 30, null));
		rulesList.add(new AchievementListItem("13", "0", "All the nice statistics", "Look at my balls. Look!", 15, "1.1.2014"));
		rulesList.add(new AchievementListItem("14", "0", "Always at your hand", "Set your own widget.", 10, "20.6.2015"));
		rulesList.add(new AchievementListItem("15", "0", "Level up!", "Reach level 2.", 10, "22.5.2015"));
		rulesList.add(new AchievementListItem("16", "0", "Level 10", "Reach level 10.", 20, null));
		rulesList.add(new AchievementListItem("17", "2", "Getting bigger", "Activate your second adapter.", 10, "11.5.2015"));
		rulesList.add(new AchievementListItem("18", "2", "Measure them all!", "Activate your fifth temperature sensor.", 20, "12.5.2015"));

		return rulesList;
	}

	// Parcel for passing this object through different activities and fragments.

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		Log.d(TAG, "writeToParcel()");
		out.writeParcelable(listItem, flags);
		out.writeTypedList(mAchievementList);
	}

	public void readFromParcel(Parcel in) {
		listItem = in.readParcelable(AchievementListItem.class.getClassLoader());
		if (mAchievementList == null) {
			mAchievementList = loadAllAchievements();
		}
		in.readTypedList(mAchievementList, AchievementListItem.CREATOR);
	}

	public static final Parcelable.Creator<AchievementList> CREATOR = new Parcelable.Creator<AchievementList>() {
		public AchievementList createFromParcel(Parcel in) {
			return new AchievementList(in);
		}

		public AchievementList[] newArray(int size) {
			return new AchievementList[size];
		}
	};
}
