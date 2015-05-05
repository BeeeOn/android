package com.rehivetech.beeeon.persistence;

import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.gamification.AchievementListItem;
import com.rehivetech.beeeon.network.INetwork;
import com.rehivetech.beeeon.util.DataHolder;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.MultipleDataHolder;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jan Lamacz
 */
public class AchievementsModel {
	private static final String TAG = AchievementsModel.class.getSimpleName();
	private static final int RELOAD_EVERY_SECONDS = 15 * 60;
	private final INetwork mNetwork;

	private final DataHolder<AchievementListItem> mAchievementsHolder = new DataHolder<>();

	public AchievementsModel(INetwork network) {
		mNetwork = network;
	}

	public List<AchievementListItem> getAchievements() {
		return mAchievementsHolder.getObjects();
	}

	public synchronized boolean reloadAchievementsByAdapter(String adapterId, boolean forceReload) throws AppException {
		if (!forceReload && !mAchievementsHolder.isExpired(RELOAD_EVERY_SECONDS)) {
			return false;
		}

		mAchievementsHolder.setObjects(mNetwork.getAllAchievements(adapterId));
		mAchievementsHolder.setLastUpdate(DateTime.now());

		return true;
	}
}
