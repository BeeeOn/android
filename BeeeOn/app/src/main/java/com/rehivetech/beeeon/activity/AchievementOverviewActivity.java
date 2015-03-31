package com.rehivetech.beeeon.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.widget.ShareDialog;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.achievements.FbShareAchievement;
import com.rehivetech.beeeon.arrayadapter.AchievementListAdapter;
import com.rehivetech.beeeon.base.BaseApplicationActivity;
import com.rehivetech.beeeon.gamification.AchievementList;
import com.rehivetech.beeeon.gamification.AchievementListItem;
import com.rehivetech.beeeon.socialNetworks.Facebook;
import com.rehivetech.beeeon.util.Log;

/**
 * @author Jan Lamacz
 */
public class AchievementOverviewActivity extends BaseApplicationActivity {
	private static final String TAG = AchievementOverviewActivity.class.getSimpleName();
//	private static final String PARCEL_ACHIEVEMENT = "achievementList";

	// extras
	public static final String EXTRA_CATEGORY_NAME = "category_name";
	public static final String EXTRA_CATEGORY_ID = "category_id";
	private String mCategoryName;
	private String mCategoryId;

	//facebook
	private Facebook mFb;
	private CallbackManager mCallbackManager;
	private ShareDialog mShareDialog;

	private AchievementListAdapter mAchievementListAdapter;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_achievement_overview);
		mFb = Facebook.getInstance();

		Bundle bundle = getIntent().getExtras();
		if(bundle != null){
			mCategoryName = bundle.getString(EXTRA_CATEGORY_NAME);
			mCategoryId = bundle.getString(EXTRA_CATEGORY_ID);
//			mAchievements = bundle.getParcelable(PARCEL_ACHIEVEMENT);
		}
		else{
			bundle = savedInstanceState;
			if (bundle != null) {
				mCategoryName = bundle.getString(EXTRA_CATEGORY_NAME);
				mCategoryId = bundle.getString(EXTRA_CATEGORY_ID);
//				mAchievements = bundle.getParcelable(PARCEL_ACHIEVEMENT);
			}
		}

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		if (toolbar != null) {
			toolbar.setTitle(mCategoryName);
			setSupportActionBar(toolbar);
			getSupportActionBar().setHomeButtonEnabled(true);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}

		setFbShareCallback();
		setOnClickListeners();
	}

	/**
	 * Callback listening to result of Facebook share activity.
	 * On success logs sharing-achievement, else logs an error.
	 */
	private void setFbShareCallback() {
		mCallbackManager = CallbackManager.Factory.create();
		mShareDialog = new ShareDialog(this);
		mShareDialog.registerCallback(mCallbackManager, new FacebookCallback<Sharer.Result>() {
			@Override
			public void onSuccess(Sharer.Result shareResult) {new FbShareAchievement(getApplicationContext());}
			@Override
			public void onCancel() {Log.d(TAG, "FB: canceled");}
			@Override
			public void onError(FacebookException exception) {Log.d(TAG, "FB error: " + exception.getMessage());}
		});
	}

	/**
	 * Sets OnClick and OnLongClick listeners on all achievements in view.
	 * OnLongClick, if achievement is already completed, display facebook
	 * share activity.
	 */
	private void setOnClickListeners() {
		AchievementList mAchievements = AchievementList.getInstance();
		ListView achievementList = (ListView) findViewById(R.id.achievement_list);

		mAchievementListAdapter = new AchievementListAdapter(this, this.getLayoutInflater(), mCategoryId, mAchievements);
		achievementList.setAdapter(mAchievementListAdapter);
		// TODO Think about possible action after short click
//		achievementList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//				AchievementListItem achievementItem = mAchievementListAdapter.getItem(position);
//				new Achievement(achievementItem).show(getApplicationContext());
//			}
//		});
		achievementList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			// TODO choose between sharing on Facebook and Twitter
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				AchievementListItem achievementItem = mAchievementListAdapter.getItem(position);
				if(achievementItem.isDone()) {
					mShareDialog
							.show(mFb.shareAchievement(getApplicationContext(), achievementItem.getName(), achievementItem.getDate()));
					return true;
				}
				else
					return false;
			}
		});
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		mCallbackManager.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		Log.d(TAG, "onSaveInstanceState");
		savedInstanceState.putString(EXTRA_CATEGORY_NAME, mCategoryName);
		savedInstanceState.putString(EXTRA_CATEGORY_ID, mCategoryId);
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	protected void onAppResume() {Log.d(TAG, "onAppResume()");}

	@Override
	protected void onAppPause() {Log.d(TAG, "onAppPause()");}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			default: return super.onOptionsItemSelected(item);
		}
	}
}