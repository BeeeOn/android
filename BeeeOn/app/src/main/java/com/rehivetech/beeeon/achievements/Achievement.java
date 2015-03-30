package com.rehivetech.beeeon.achievements;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gamification.AchievementList;
import com.rehivetech.beeeon.gamification.AchievementListItem;

/**
 * @author Jan Lamacz
 */
public abstract class Achievement  {
//	private static final String TAG = Achievement.class.getSimpleName();

	protected AchievementListItem mData;
	protected Context mContext;
	protected String mID;

	public Achievement(String id, Context context) {
		setmID(id);
		mContext = context;
		mData = AchievementList.getInstance().getItem(Integer.parseInt(mID));
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

	public void setmID(String mID) {this.mID = mID;}

//	public AchievementListItem getAchievementData() {return mData;}
}
