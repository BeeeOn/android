package com.rehivetech.beeeon.gamification;

import com.rehivetech.beeeon.util.Log;

/**
 * @author Jan Lamacz
 */
public class Achievement{
	private static final String TAG = Achievement.class.getSimpleName();

	private AchievementListItem achievementData;
	private String mID;

	public Achievement(AchievementListItem achievementItem) {
		setmID(achievementItem.getId());
		setAchievementData(achievementItem);
		Log.d(TAG, "newAchievement: " + achievementData.getName());
	}

	public String getmID() {return mID;}
	public void setmID(String mID) {this.mID = mID;}

	public AchievementListItem getAchievementData() {return achievementData;}
	public void setAchievementData(AchievementListItem achievementItem) {this.achievementData = achievementItem;}
}
