package com.rehivetech.beeeon.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ListView;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.arrayadapter.AchievementListAdapter;
import com.rehivetech.beeeon.base.BaseApplicationActivity;
import com.rehivetech.beeeon.gamification.AchievementList;
import com.rehivetech.beeeon.util.Log;

/**
 * @author Jan Lamacz
 */
public class AchievementOverviewActivity extends BaseApplicationActivity {
	private static final String TAG = AchievementOverviewActivity.class.getSimpleName();

	public static final String EXTRA_CATEGORY_NAME = "category_name";
	public static final String EXTRA_CATEGORY_ID = "category_id";

	private AchievementListAdapter mAchievementListAdapter;
	private AchievementList mAchievements;

	// extras
	private String mCategoryName;
	private String mCategoryId;

	// GUI elements
	private TextView mTextName;
	private TextView mPoints;
	private TextView mLevel;
	private ListView mAchievementList;

	private Toolbar mToolbar;
	private boolean mIsNew = false;
	private AchievementOverviewActivity mActivity;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate()");
		mActivity = this;

		setContentView(R.layout.activity_achievement_overview);

		// prepare toolbar
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		if (mToolbar != null) {
			mToolbar.setTitle(R.string.title_activity_achievement_overview);
			setSupportActionBar(mToolbar);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setHomeButtonEnabled(true);
		}
		else
		  Log.d(TAG, "noToolbar");

		Bundle bundle = getIntent().getExtras();
		if(bundle != null){
			mCategoryName = bundle.getString(EXTRA_CATEGORY_NAME);
			mCategoryId = bundle.getString(EXTRA_CATEGORY_ID);
			mAchievements = bundle.getParcelable("achievementList");
			mIsNew = false;
		}
		else{
			bundle = savedInstanceState;
			if (bundle != null) {
				mCategoryName = bundle.getString(EXTRA_CATEGORY_NAME);
				mCategoryId = bundle.getString(EXTRA_CATEGORY_ID);
				mAchievements = bundle.getParcelable("achievementList");
			}
			else{
				mIsNew = true;
			}
		}

		mLevel = (TextView) findViewById(R.id.achievement_category_detail);
		mPoints = (TextView) findViewById(R.id.achievement_category_points);
		mTextName = (TextView) findViewById(R.id.achievement_category_name);
		mAchievementList = (ListView) findViewById(R.id.achievement_list);

		mLevel.setText(getString(R.string.gam_level) + " " + String.valueOf(mAchievements.getLevel()));
		mPoints.setText(String.valueOf(mAchievements.getTotalPoints()));
		mTextName.setText(mCategoryName);

		mAchievementListAdapter = new AchievementListAdapter(mActivity, mActivity.getLayoutInflater(), mCategoryId, mAchievements);
		mAchievementList.setAdapter(mAchievementListAdapter);

		// when adding rule x instead of <-
		if(mIsNew){
			getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_cancel);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		Log.d(TAG, "onSaveInstanceState");
		savedInstanceState.putString(EXTRA_CATEGORY_NAME, mCategoryName);

		// Always call the superclass so it can save the view hierarchy state
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(TAG, "onCreateOptionsMenu");
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.watchdog_edit_menu, menu);
		return true;
	}

	@Override
	protected void onAppResume() {
		Log.d(TAG, "onAppResume()");
	}

	@Override
	protected void onAppPause() {
		Log.d(TAG, "onAppPause()");
	}

}
