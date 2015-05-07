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
import com.rehivetech.beeeon.gamification.AchievementListItem;
import com.rehivetech.beeeon.pair.AchievementPair;
import com.rehivetech.beeeon.util.Log;

import java.util.List;

/**
 * @author Jan Lamacz
 */
public class Achievement  {
	private static final String TAG = Achievement.class.getSimpleName();

	protected AchievementListItem mData = null;
	protected Context mContext;
	protected Controller mController;
	protected String mAchievementId;
	protected String mAdapterId;
	protected UpdateAchievementTask mUpdateAchievementTask;

	public Achievement(String achievement_id, Context context) {
		mContext = context;
		mAchievementId = achievement_id;

		mAdapterId = "999";
		mController = Controller.getInstance(mContext);
		if(mController.getActiveAdapter() != null)
			mAdapterId = mController.getActiveAdapter().getId();
		doReloadAchievementsTask(mAdapterId, false);
		doAddUpdateAchievementTask(new AchievementPair(mAdapterId, mAchievementId));
	}

	protected void doAddUpdateAchievementTask(AchievementPair pair) {
		mUpdateAchievementTask = new UpdateAchievementTask(mContext);

		mUpdateAchievementTask.setListener(new CallbackTask.CallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				Log.d(TAG, "Achievement update completed with " + success);
				if(success) show();
			}
		});
		mUpdateAchievementTask.execute(pair);
	}

	public void doReloadAchievementsTask(final String adapterId, boolean forceReload){
		ReloadAdapterDataTask reloadAchievementsTask = new ReloadAdapterDataTask(mContext, forceReload, ReloadAdapterDataTask.ReloadWhat.ACHIEVEMENTS);

		reloadAchievementsTask.setListener(new CallbackTask.CallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				List<AchievementListItem> list = mController.getAchievementsModel().getAchievements();
				for(int i = 0; i <list.size(); i++) {
					if (list.get(i).getId().equals(mAchievementId)) {
						mData = list.get(i);
						break;
					}
				}
			}
		});
		reloadAchievementsTask.execute(adapterId);
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
}
