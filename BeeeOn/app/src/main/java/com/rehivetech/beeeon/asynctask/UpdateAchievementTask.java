package com.rehivetech.beeeon.asynctask;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.pair.AchievementPair;

/**
 * @author Jan Lamacz
 */
public class UpdateAchievementTask  extends CallbackTask<AchievementPair>  {
	private String mAchievementId;

	public UpdateAchievementTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(AchievementPair pair) {
		Controller controller = Controller.getInstance(mContext);

		mAchievementId = controller.getAchievementsModel().updateAchievement(pair.adapter, pair.achievement);
		return mAchievementId.length() > 0;
	}

	public String getAchievementId() {
		return mAchievementId;
	}
}
