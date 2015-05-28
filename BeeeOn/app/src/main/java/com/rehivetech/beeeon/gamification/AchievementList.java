package com.rehivetech.beeeon.gamification;

import android.content.Context;

import com.rehivetech.beeeon.asynctask.CallbackTask;
import com.rehivetech.beeeon.asynctask.ReloadAdapterDataTask;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * @author Jan Lamacz
 */
public class AchievementList extends Observable {
	private static final String TAG = AchievementList.class.getSimpleName();
	private static AchievementList mInstance = null;
	private static String mAdapterId = "0";
	private Context mContext;
	private Controller mController;

	private boolean allDataDownloaded;
	private List<AchievementListItem> allAchievementList;
	private ArrayList<AchievementListItem> mAchievementList;
	private int[] mComplete; // number of completed achievements in 3 categories
	private int[] mTotal;    // number of all achievements in 3 categories
	private int mTotalPoints;

	private AchievementList(Context context, Controller controller) {
		mContext = context.getApplicationContext();
		mController = controller;
		allDataDownloaded = false;
		allAchievementList = null;
		doReloadAchievementsTask(mAdapterId, true);
	}

	public static AchievementList getInstance(Context context) {
		Controller controller = Controller.getInstance(context);
		String oldId = mAdapterId;
		if (controller.getActiveGate() != null)
			mAdapterId = controller.getActiveGate().getId();

		if (mInstance == null || !oldId.equals(mAdapterId)) {
			mInstance = new AchievementList(context, controller);
		}
		return mInstance;
	}

	public static void cleanAll() {
		mAdapterId = null;
		mInstance = null;
	}

	public void doReloadAchievementsTask(final String adapterId, boolean forceReload) {
		ReloadAdapterDataTask reloadAchievementsTask = new ReloadAdapterDataTask(mContext, forceReload, ReloadAdapterDataTask.ReloadWhat.ACHIEVEMENTS);

		reloadAchievementsTask.setListener(new CallbackTask.CallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				Log.d(TAG, "successfully downloaded data");
				allAchievementList = mController.getAchievementsModel().getAchievements(adapterId);
				updateData();
				allDataDownloaded = true;
				setChanged();
				notifyObservers("achievements");
			}
		});
		reloadAchievementsTask.execute(adapterId);
	}

	public boolean isDownloaded() {
		return allDataDownloaded;
	}

	public List<AchievementListItem> getAllAchievements() {
		return mAchievementList;
	}

	public AchievementListItem getItem(String id) {
		for (int i = 0; i < allAchievementList.size(); i++)
			if (allAchievementList.get(i).getId().equals(id))
				return allAchievementList.get(i);
		return null;
	}

	public int getTotalAchievements(String category) {
		return mTotal[Integer.parseInt(category)];
	}

	public int getCompletedAchievements(String category) {
		return mComplete[Integer.parseInt(category)];
	}

	public int getTotalPoints() {
		return mTotalPoints;
	}

	/**
	 * Counts number of reached stars in concrete category.
	 * Depending on progress done in each of the categories
	 * counts reached stars from 0 (almost none progress) to 3 (everything almost done)
	 *
	 * @param category - Id of category (String)
	 * @return int - number of achieved stars
	 */
	public int getStarsCount(String category) {
		float ratio = (float) mComplete[Integer.parseInt(category)] / mTotal[Integer.parseInt(category)];
		if (ratio >= 0.9)
			return 3;
		else if (ratio >= 0.6)
			return 2;
		else if (ratio >= 0.3)
			return 1;
		else return 0;
	}

	/**
	 * Counts users level.
	 * Level has (yet) significance and is (yet) is "just for fun"
	 * Level counts as 1 + 1 level for each gained star.
	 *
	 * @return int level
	 * @link getStarsCount()
	 */
	public int getUserLevel() {
		int level = 1;
		for (int i = 0; i < mTotal.length; i++)
			level += getStarsCount(String.valueOf(i));
		return level;
	}

	public void updateData() {
		filterAchievements();
		recountValues();
	}

	private void filterAchievements() {
		mAchievementList = new ArrayList<>();
		for (int i = 0; i < allAchievementList.size(); i++) {
			AchievementListItem son = null, parent = null, item = allAchievementList.get(i);
//			if(!item.isVisible() && !item.isDone()) continue;
			for (int y = 0; y < allAchievementList.size(); y++) {
				if (allAchievementList.get(y).getId().equals(item.getParent()))
					parent = allAchievementList.get(y);
				else if (allAchievementList.get(y).getParent().equals(item.getId()))
					son = allAchievementList.get(y);
			}
			item.setParent(parent);
			if ((item.isDone() && (son == null || !son.isDone())) ||
					(!item.isDone() && (parent == null || parent.isDone()))) {
				item.setContext(mContext);
				mAchievementList.add(item);
			}
		}
	}

	/**
	 * Calculates number of achievements in all categories.
	 * Counts number of total and completed achievements in all (3) categories
	 * and number of totally earned points.
	 */
	private void recountValues() {
		mComplete = new int[]{0, 0, 0}; // number of completed achievements in 3 categories
		mTotal = new int[]{0, 0, 0};    // number of all achievements in 3 categories
		mTotalPoints = 0;
		AchievementListItem achievement;
		for (int i = 0; i < mAchievementList.size(); i++) {
			achievement = mAchievementList.get(i);
			achievement.recountParentValues();
			mTotal[Integer.parseInt(achievement.getCategory())] += achievement.getLevelCount();
			if (achievement.isDone()) {
				mTotalPoints += achievement.getPoints();
				mComplete[Integer.parseInt(achievement.getCategory())] += achievement.getLevelCount();
			}
		}
	}
}
