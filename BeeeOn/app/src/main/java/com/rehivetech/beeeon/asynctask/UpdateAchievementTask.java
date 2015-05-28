package com.rehivetech.beeeon.asynctask;

import android.content.Context;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.pair.AchievementPair;

import java.util.List;

/**
 * @author Jan Lamacz
 */
public class UpdateAchievementTask extends CallbackTask<AchievementPair> {
	private List<String> mAchievementId;

	public UpdateAchievementTask(Context context) {
		super(context);
	}

	@Override
	protected Boolean doInBackground(AchievementPair pair) {
		Controller controller = Controller.getInstance(mContext);

		mAchievementId = controller.getAchievementsModel().updateAchievement(pair.gateId, pair.achievement);
		return mAchievementId.size() > 0;
	}

	public List<String> getAchievementId() {
		return mAchievementId;
	}
}
