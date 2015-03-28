package com.rehivetech.beeeon.gamification;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.util.Log;

/**
 * @author Jan Lamacz
 */
public class Achievement  {
	private static final String TAG = Achievement.class.getSimpleName();

	private AchievementListItem achievementData;
	private String mID;

	public Achievement(AchievementListItem achievementItem) {
		setmID(achievementItem.getId());
		setAchievementData(achievementItem);
	}

	public void show(Context context) {
		LayoutInflater i = LayoutInflater.from(context);
		View layout = i.inflate(R.layout.achievement_toast,null);

		TextView name = (TextView) layout.findViewById(R.id.achievement_toast_name);
		TextView points = (TextView) layout.findViewById(R.id.achievement_toast_points);
		name.setText(achievementData.getName());
		points.setText(String.valueOf(achievementData.getPoints()));

		Toast toast = new Toast(context);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(layout);
		toast.show();
	}

	public String getmID() {return mID;}
	public void setmID(String mID) {this.mID = mID;}

	public AchievementListItem getAchievementData() {return achievementData;}
	public void setAchievementData(AchievementListItem achievementItem) {this.achievementData = achievementItem;}
}
