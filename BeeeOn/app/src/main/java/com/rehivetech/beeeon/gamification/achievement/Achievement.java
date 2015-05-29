package com.rehivetech.beeeon.gamification.achievement;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gamification.AchievementList;
import com.rehivetech.beeeon.gamification.AchievementListItem;
import com.rehivetech.beeeon.pair.AchievementPair;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.task.UpdateAchievementTask;
import com.rehivetech.beeeon.util.Log;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * @author Jan Lamacz
 */
public abstract class Achievement implements Observer {
	private static final String TAG = Achievement.class.getSimpleName();

	protected AchievementList mAchievementList;
	protected AchievementListItem mData = null;
	protected Context mContext = null;
	protected Controller mController;
	private String mAchievementId;
	private String mGateId;
	private boolean mSendUpdate;
	protected UpdateAchievementTask mUpdateAchievementTask;

	protected Achievement(String achievement_id, Context context) {
		this(achievement_id, context, true);
	}

	protected Achievement(String achievement_id, Context context, boolean sendUpdate) {
		mContext = context.getApplicationContext();
		mAchievementId = achievement_id;
		mSendUpdate = sendUpdate;

		mGateId = "0";
		mController = Controller.getInstance(mContext);
		if (mController.getActiveGate() != null)
			mGateId = mController.getActiveGate().getId();
		mAchievementList = AchievementList.getInstance(mContext);
		if (mAchievementList.isDownloaded()) {
			mData = mAchievementList.getItem(achievement_id);
			doAddUpdateAchievementTask(new AchievementPair(mGateId, mAchievementId));
		} else
			mAchievementList.addObserver(this);
	}

	protected void doAddUpdateAchievementTask(AchievementPair pair) {
		if (!mSendUpdate && mData.isDone()) return; // if is done and should not be send, skip it

		mUpdateAchievementTask = new UpdateAchievementTask(mContext);
		mUpdateAchievementTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				List<String> idList = mUpdateAchievementTask.getAchievementId();
				if (!success) {
					Toast.makeText(mContext, mContext.getString(R.string.social_no_connection), Toast.LENGTH_LONG).show();
					return;
				} else if (idList == null) return; // DEMO, don`t update anything
				for (int i = 0; i < idList.size(); i++) {
					AchievementListItem item = mAchievementList.getItem(idList.get(i));
					if (item == null) continue;
					else if (item.updateProgress())
						showSuccess(item);
					Log.d(TAG, "Updated achievement " + idList.get(i));
				}
				mAchievementList.updateData();
			}
		});
		mUpdateAchievementTask.execute(pair);
	}

	public void showSuccess(AchievementListItem item) {
		LayoutInflater i = LayoutInflater.from(mContext);
		View layout = i.inflate(R.layout.achievement_toast, null);

		TextView name = (TextView) layout.findViewById(R.id.achievement_toast_name);
		TextView points = (TextView) layout.findViewById(R.id.achievement_toast_points);
		name.setText(item.getName());
		points.setText(String.valueOf(item.getPoints()));

		Toast toast = new Toast(mContext);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(layout);
		toast.show();
	}

	@Override
	public void update(Observable observable, Object o) {
		if (o.toString().equals("achievements"))
			doAddUpdateAchievementTask(new AchievementPair(mGateId, mAchievementId));
	}
}
