package com.rehivetech.beeeon.persistence;

import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.gamification.AchievementListItem;
import com.rehivetech.beeeon.network.INetwork;
import com.rehivetech.beeeon.util.MultipleDataHolder;

import org.joda.time.DateTime;

import java.util.List;

/**
 * @author Jan Lamacz
 */
public class AchievementsModel extends BaseModel {
	private static final String TAG = AchievementsModel.class.getSimpleName();
	private static final int RELOAD_EVERY_SECONDS = 15 * 60;

	private final MultipleDataHolder<AchievementListItem> mAchievementsHolder = new MultipleDataHolder<>();

	public AchievementsModel(INetwork network) {
		super(network);
	}

	public List<AchievementListItem> getAchievements(String adapterId) {
		return mAchievementsHolder.getObjects(adapterId);
	}

	public synchronized boolean reloadAchievementsByAdapter(String adapterId, boolean forceReload) throws AppException {
		if (!forceReload && !mAchievementsHolder.isExpired(adapterId, RELOAD_EVERY_SECONDS)) {
			return false;
		}

		mAchievementsHolder.setObjects(adapterId, mNetwork.getAllAchievements(adapterId));
		mAchievementsHolder.setLastUpdate(adapterId, DateTime.now());

		return true;
	}

	public List<String> updateAchievement(String adapterId, String achievementId) {
		return mNetwork.setProgressLvl(adapterId, achievementId);
	}
}
