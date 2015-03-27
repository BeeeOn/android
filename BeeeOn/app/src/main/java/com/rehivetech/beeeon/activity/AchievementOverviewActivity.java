package com.rehivetech.beeeon.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.arrayadapter.AchievementListAdapter;
import com.rehivetech.beeeon.base.BaseApplicationActivity;
import com.rehivetech.beeeon.gamification.Achievement;
import com.rehivetech.beeeon.gamification.AchievementList;
import com.rehivetech.beeeon.gamification.AchievementListItem;
import com.rehivetech.beeeon.util.Log;

/**
 * @author Jan Lamacz
 */
public class AchievementOverviewActivity extends BaseApplicationActivity {
	private static final String TAG = AchievementOverviewActivity.class.getSimpleName();

	// extras
	public static final String EXTRA_CATEGORY_NAME = "category_name";
	public static final String EXTRA_CATEGORY_ID = "category_id";
	private AchievementList mAchievements;
	private String mCategoryName;
	private String mCategoryId;

	private AchievementListAdapter mAchievementListAdapter;
	private Toolbar mToolbar;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_achievement_overview);

		Bundle bundle = getIntent().getExtras();
		if(bundle != null){
			mCategoryName = bundle.getString(EXTRA_CATEGORY_NAME);
			mCategoryId = bundle.getString(EXTRA_CATEGORY_ID);
			mAchievements = bundle.getParcelable("achievementList");
		}
		else{
			bundle = savedInstanceState;
			if (bundle != null) {
				mCategoryName = bundle.getString(EXTRA_CATEGORY_NAME);
				mCategoryId = bundle.getString(EXTRA_CATEGORY_ID);
				mAchievements = bundle.getParcelable("achievementList");
			}
		}

		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		if (mToolbar != null) {
			mToolbar.setTitle(mCategoryName);
			setSupportActionBar(mToolbar);
			getSupportActionBar().setHomeButtonEnabled(true);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}

		ListView achievementList = (ListView) findViewById(R.id.achievement_list);
		mAchievementListAdapter = new AchievementListAdapter(this, this.getLayoutInflater(), mCategoryId, mAchievements);
		achievementList.setAdapter(mAchievementListAdapter);
		achievementList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				AchievementListItem achievementItem = mAchievementListAdapter.getItem(position);
				new Achievement(achievementItem);
			}
		});
		achievementList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				AchievementListItem achievementItem = mAchievementListAdapter.getItem(position);
				Toast.makeText(getApplicationContext(), achievementItem.getName(), Toast.LENGTH_LONG).show();
				return true;
			}
		});
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		Log.d(TAG, "onSaveInstanceState");
		savedInstanceState.putString(EXTRA_CATEGORY_NAME, mCategoryName);
		savedInstanceState.putString(EXTRA_CATEGORY_ID, mCategoryId);

		// Always call the superclass so it can save the view hierarchy state
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
