package com.rehivetech.beeeon.achievements;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.asynctask.CallbackTask;
import com.rehivetech.beeeon.asynctask.ReloadAdapterDataTask;
import com.rehivetech.beeeon.asynctask.UpdateAchievementTask;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gamification.AchievementList;
import com.rehivetech.beeeon.gamification.AchievementListItem;
import com.rehivetech.beeeon.pair.AchievementPair;
import com.rehivetech.beeeon.util.Log;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * @author Jan Lamacz
 */
public abstract class Achievement  implements Observer {
	private static final String TAG = Achievement.class.getSimpleName();

	protected AchievementList mAchievementList;
	protected AchievementListItem mData = null;
	protected Context mContext = null;
	protected Controller mController;
	protected String mAchievementId;
	protected String mAdapterId;
	protected UpdateAchievementTask mUpdateAchievementTask;

	protected Achievement(String achievement_id, Context context) {
		mContext = context;
		mAchievementId = achievement_id;

		mAdapterId = "0";
		mController = Controller.getInstance(mContext);
		if(mController.getActiveAdapter() != null)
			mAdapterId = mController.getActiveAdapter().getId();
		mAchievementList = AchievementList.getInstance(mContext);
		if(mAchievementList.isDownloaded()) {
			mData = mAchievementList.getItem(achievement_id);
			doAddUpdateAchievementTask(new AchievementPair(mAdapterId, mAchievementId));
		}
		else
			mAchievementList.addObserver(this);
	}

	protected void doAddUpdateAchievementTask(AchievementPair pair) {
		mUpdateAchievementTask = new UpdateAchievementTask(mContext);

		mUpdateAchievementTask.setListener(new CallbackTask.CallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if(!success)
					showError();
				else if(mAchievementList.getItem(mUpdateAchievementTask.getAchievementId()).updateProgress())
					showSuccess();
				Log.d(TAG, "Updated achievement " + mUpdateAchievementTask.getAchievementId() + "? "+success);
			}
		});
		mUpdateAchievementTask.execute(pair);
	}


	public void showSuccess() {
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

	public void showError() {
		Toast.makeText(mContext, "Neporadilo se", Toast.LENGTH_LONG).show();
	}

	@Override
	public void update(Observable observable, Object o) {
		if(o.toString().equals("achievements"))
			doAddUpdateAchievementTask(new AchievementPair(mAdapterId, mAchievementId));
	}
}
