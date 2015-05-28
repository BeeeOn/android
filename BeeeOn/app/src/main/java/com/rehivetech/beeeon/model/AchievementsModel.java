package com.rehivetech.beeeon.model;

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

	public List<AchievementListItem> getAchievements(String gateId) {
		return mAchievementsHolder.getObjects(gateId);
	}

	public synchronized boolean reloadAchievementsByGate(String gateId, boolean forceReload) throws AppException {
		if (!forceReload && !mAchievementsHolder.isExpired(gateId, RELOAD_EVERY_SECONDS)) {
			return false;
		}

		mAchievementsHolder.setObjects(gateId, mNetwork.getAllAchievements(gateId));
		mAchievementsHolder.setLastUpdate(gateId, DateTime.now());

		return true;
	}

	public List<String> updateAchievement(String gateId, String achievementId) {
		return mNetwork.setProgressLvl(gateId, achievementId);
	}
}
