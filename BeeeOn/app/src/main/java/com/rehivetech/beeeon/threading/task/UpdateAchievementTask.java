package com.rehivetech.beeeon.threading.task;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gamification.achievement.Achievement;
import com.rehivetech.beeeon.threading.CallbackTask;

import java.util.List;

/**
 * @author Jan Lamacz
 */
public class UpdateAchievementTask extends CallbackTask<Achievement.DataPair> {
	private List<String> mAchievementId;

	public UpdateAchievementTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(Achievement.DataPair pair) {
		Controller controller = Controller.getInstance(mContext);

		mAchievementId = controller.getAchievementsModel().updateAchievement(pair.gateId, pair.achievement);
		return mAchievementId.size() > 0;
	}

	public List<String> getAchievementId() {
		return mAchievementId;
	}
}
