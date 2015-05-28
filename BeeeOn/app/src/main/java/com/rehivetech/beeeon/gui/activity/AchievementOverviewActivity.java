package com.rehivetech.beeeon.gui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.facebook.CallbackManager;
import com.facebook.share.widget.ShareDialog;
import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gamification.achievement.GeneralAchievement;
import com.rehivetech.beeeon.gui.dialog.ShareFragmentDialog;
import com.rehivetech.beeeon.arrayadapter.AchievementListAdapter;
import com.rehivetech.beeeon.gamification.AchievementList;
import com.rehivetech.beeeon.gamification.AchievementListOnClickListener;

import java.util.Observable;
import java.util.Observer;

/**
 * @author Jan Lamacz
 */
public class AchievementOverviewActivity extends BaseApplicationActivity implements AchievementListOnClickListener, Observer {
	private static final String TAG = AchievementOverviewActivity.class.getSimpleName();

	// extras
	public static final String EXTRA_CATEGORY_ID = "category_id";
	private String mCategoryId;

	//facebook
	private CallbackManager mCallbackManager;
	private ShareDialog mShareDialog;

	private AchievementList mAchievementListHolder;
	private AchievementListAdapter mAchievementListAdapter;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_achievement_overview);

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			mCategoryId = bundle.getString(EXTRA_CATEGORY_ID);
		} else {
			bundle = savedInstanceState;
			if (bundle != null)
				mCategoryId = bundle.getString(EXTRA_CATEGORY_ID);
		}

		String categoryName;
		switch (mCategoryId) {
			case "0":
				categoryName = getString(R.string.profile_category_app);
				break;
			case "1":
				categoryName = getString(R.string.profile_category_friends);
				break;
			case "2":
				categoryName = getString(R.string.profile_category_senzors);
				break;
			default:
				categoryName = getString(R.string.title_activity_achievement_overview);
		}

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		if (toolbar != null) {
			toolbar.setTitle(categoryName);
			setSupportActionBar(toolbar);
			getSupportActionBar().setHomeButtonEnabled(true);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}

		mAchievementListHolder = AchievementList.getInstance(this);
		if (mAchievementListHolder.isDownloaded())
			setListAdapter();
		else
			mAchievementListHolder.addObserver(this);

		setFbShareCallback();
	}

	/**
	 * Sets OnClick and OnLongClick listeners on all achievements in view.
	 * OnLongClick, if achievement is already completed, display facebook
	 * share activity.
	 */
	private void setListAdapter() {
		ListView achievementList = (ListView) findViewById(R.id.achievement_list);

		mAchievementListAdapter = new AchievementListAdapter(this.getLayoutInflater(), mCategoryId, this, mAchievementListHolder.getAllAchievements(), this);
		achievementList.setAdapter(mAchievementListAdapter);
		achievementList.setSelector(android.R.color.transparent);
	}

	/**
	 * Callback listening to result of Facebook share activity.
	 * On success logs sharing-achievement, else logs an error.
	 */
	private void setFbShareCallback() {
//		mCallbackManager = CallbackManager.Factory.create();
//		mShareDialog = new ShareDialog(this);
//		mShareDialog.registerCallback(mCallbackManager, new FacebookCallback<Sharer.Result>() {
//			@Override
//			public void onSuccess(Sharer.Result shareResult) {
//				if (shareResult.getPostId() != null) // null is if 'cancel' is hit
//					new GeneralAchievement(Constants.ACHIEVEMENT_FACEBOOK_SHARE, getApplicationContext());
//			}
//			@Override
//			public void onCancel() {
//				Log.d(TAG, "FB: canceled");
//			}
//			@Override
//			public void onError(FacebookException e) {
//				Log.d(TAG, "FB error: " + e.getMessage());
//				Toast.makeText(getApplicationContext(), getString(R.string.NetworkError___CL_INTERNET_CONNECTION), Toast.LENGTH_LONG).show();
//			}
//		});
	}

	/**
	 * Implements Interface, shows alert dialog for choosing network to
	 * share an achievement via.
	 */
	@Override
	public void OnAchievementClick(View aView, int position) {
//		AchievementListItem achievementItem = mAchievementListAdapter.getItem(position);
		new ShareFragmentDialog().show(getSupportFragmentManager(), TAG);
	}

	/**
	 * Callback for Facebook sharing dialog, handles the response
	 */
	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		// Sharing with Twitter
		// if user has twitter native app - can control if sharing was successful
		if (requestCode == 66586 && // TODO Fix MAGIC!!
				resultCode == RESULT_OK) {
			new GeneralAchievement(Constants.ACHIEVEMENT_TWITTER_SHARE, this);
		}
		// Sharing with Google Plus
		if (requestCode == 66587 && // TODO Fix MAGIC!!
				resultCode == RESULT_OK) {
			new GeneralAchievement(Constants.ACHIEVEMENT_GPLUS_SHARE, this);
		}
		mCallbackManager.onActivityResult(requestCode, resultCode, data);
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void update(Observable observable, Object o) {
		if (o.toString().equals("achievements")) {
			setListAdapter();
		}
	}
}
