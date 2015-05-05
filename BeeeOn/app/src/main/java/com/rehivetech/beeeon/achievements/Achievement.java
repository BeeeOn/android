package com.rehivetech.beeeon.achievements;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gamification.AchievementList;
import com.rehivetech.beeeon.gamification.AchievementListItem;

import java.util.ResourceBundle;

/**
 * @author Jan Lamacz
 */
public abstract class Achievement  {
//	private static final String TAG = Achievement.class.getSimpleName();

	protected AchievementListItem mData;
	protected Context mContext;
	protected String mAchievementId;
	protected String mAdapterId;

	public Achievement(String achievement_id, Context context) {
		setAchievementId(achievement_id);

		mContext = context;
		mAdapterId = Controller.getInstance(mContext).getActiveAdapter().getId();
//		mData = AchievementList.getInstance(mContext).getItem(Integer.parseInt(mAchievementId));
	}

	public void show() {
		LayoutInflater i = LayoutInflater.from(mContext);
		View layout = i.inflate(R.layout.achievement_toast,null);

		TextView name = (TextView) layout.findViewById(R.id.achievement_toast_name);
		TextView points = (TextView) layout.findViewById(R.id.achievement_toast_points);
		name.setText(mData.getName());
		points.setText(String.valueOf(mData.getPoints()));

		Toast toast = new Toast(mContext);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(layout);
		toast.show();
	}

	public void setAchievementId(String mID) {this.mAchievementId = mID;}

//	public AchievementListItem getAchievementData() {return mData;}
}
