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
	private Context mContext;
	private Controller mController;

	private ArrayList<AchievementListItem> mAchievementList = new ArrayList<AchievementListItem>();
	private int[] mComplete = new int[] {0,0,0}; // number of completed achievements in 3 categories
	private int[] mTotal = new int[] {0,0,0};	// number of all achievements in 3 categories
	private int mTotalPoints = 0;

	public AchievementList(Context context) {
		mContext = context;
		Log.d(TAG, "constructor");
		mController = Controller.getInstance(mContext);
		String adapter = "0";
		if(mController.getActiveAdapter() != null)
			adapter = mController.getActiveAdapter().getId();
		doReloadAchievementsTask(adapter, false);
	}

	public void doReloadAchievementsTask(String adapterId, boolean forceReload){
		ReloadAdapterDataTask reloadAchievementsTask = new ReloadAdapterDataTask(mContext, forceReload, ReloadAdapterDataTask.ReloadWhat.ACHIEVEMENTS);

		reloadAchievementsTask.setListener(new CallbackTask.CallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				List<AchievementListItem> list = mController.getAchievementsModel().getAchievements();
				for(int i = 0; i < list.size(); i++) {
					AchievementListItem son = null, parent = null, item = list.get(i);
					for (int y = 0; y < list.size(); y++) {
						if (list.get(y).getId().equals(item.getParent()))
							parent = list.get(y);
						else if (list.get(y).getParent().equals(item.getId()))
							son = list.get(y);
					}
					Log.d(TAG, item.getId() + ", " + item.getParent() + ", son: " + son + ", parent: " + parent);
					if ((item.isDone() && (son == null || !son.isDone())) ||
						(!item.isDone() && (parent == null || parent.isDone()))) {
						item.setContext(mContext);
						mAchievementList.add(item);
					}
				}
				recountValues();
				setChanged();
				notifyObservers("achievements");
			}
		});
		reloadAchievementsTask.execute(adapterId);
	}

	public List<AchievementListItem> getAchievements() {
		return  mAchievementList;
	}

	public AchievementListItem getItem(int id) {
		return mAchievementList.get(id);
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

	/** Counts number of reached stars in concrete category.
	 * Depending on progress done in each of the categories
	 * counts reached stars from 0 (almost none progress) to 3 (everything almost done)
	 *
	 * @param category - Id of category (String)
	 * @return int - number of achieved stars
	 */
	public int getStarsCount(String category) {
		float ratio = (float) mComplete[Integer.parseInt(category)] / mTotal[Integer.parseInt(category)];
		if(ratio >= 0.9)
			return 3;
		else if(ratio >= 0.6)
			return 2;
		else if(ratio >= 0.3)
			return 1;
		else return 0;
	}

	/** Counts users level.
	 * Level has (yet) significance and is (yet) is "just for fun"
	 * Level counts as 1 + 1 level for each gained star.
	 * @link getStarsCount()
	 *
	 * @return int level
	 */
	public int getLevel() {
		int level = 1;
		for(int i = 0; i < mTotal.length; i++)
			level += getStarsCount(String.valueOf(i));
		return level;
	}

	/** Calculates number of achievements in all categories.
	 * Counts number of total and completed achievements in all (3) categories
	 * and number of totally earned points.
	 */
	private void recountValues() {
		AchievementListItem achievement;
		for (int i = 0; i < mAchievementList.size(); i++) {
			achievement = mAchievementList.get(i);
			mTotal[Integer.parseInt(achievement.getCategory())]++;
			if (achievement.isDone()) {
				mComplete[Integer.parseInt(achievement.getCategory())]++;
				mTotalPoints += achievement.getPoints();
			}
		}
	}
}
