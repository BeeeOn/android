package com.rehivetech.beeeon.activity.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.activity.AchievementOverviewActivity;
import com.rehivetech.beeeon.arrayadapter.GamCategoryListAdapter;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gamification.AchievementList;
import com.rehivetech.beeeon.gamification.GamificationCategory;
import com.rehivetech.beeeon.household.ActualUser;
import com.rehivetech.beeeon.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jan Lamacz
 */
public class ProfileDetailFragment extends Fragment {
  	private static final String TAG = ProfileDetailFragment.class.getSimpleName();
  	private View mView;
  	private Controller mController;
  	private ActualUser actUser;
  	private GamCategoryListAdapter mCategoryListAdapter;

	private TextView userName;
  	private TextView userDetail;
  	private ImageView userImage;
  	private ListView mCategoryList;
	private TextView mPoints;

	public ProfileDetailFragment() {
	    mController = Controller.getInstance(getActivity());
	    actUser = mController.getActualUser();
  	}

  	@Override
  	public void onCreate(Bundle savedInstanceState){
	    super.onCreate(savedInstanceState);
	    Log.d(TAG, "onCreate()");
  	}

  	@Override
  	public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    	Log.d(TAG, "onCreateView()");

    	// Inflate the layout for this fragment
    	mView = inflater.inflate(R.layout.profile_detail, container, false);
    	userName = (TextView) mView.findViewById(R.id.profile_name);
    	userDetail = (TextView) mView.findViewById(R.id.profile_detail);
    	userImage = (ImageView) mView.findViewById(R.id.profile_image);
    	mCategoryList = (ListView) mView.findViewById(R.id.gam_category_list);
		mPoints = (TextView) mView.findViewById(R.id.profile_points);

    	redrawCategories();
    	return mView;
  	}

 	@Override
  	public void onActivityCreated(Bundle savedInstanceState){
	    super.onActivityCreated(savedInstanceState);
	    Log.d(TAG, "onActivityCreated()");

	    Bitmap picture = actUser.getPicture();
	    if (picture == null)
	    	picture = actUser.getDefaultPicture(getActivity());

	    userName.setText(actUser.getName());
	    userImage.setImageBitmap(picture);
  	}

  	private void redrawCategories() {
		final AchievementList achievementList = new AchievementList();

		userDetail.setText(getString(R.string.gam_level) + " " + achievementList.getLevel());
		mPoints.setText(String.valueOf(achievementList.getTotalPoints()));

		List<GamificationCategory> rulesList = new ArrayList<>();
		rulesList.add(new GamificationCategory("0", getString(R.string.gam_category_app)));
		rulesList.add(new GamificationCategory("1", getString(R.string.gam_category_friends)));
		rulesList.add(new GamificationCategory("2", getString(R.string.gam_category_senzors)));

		mCategoryListAdapter = new GamCategoryListAdapter(getActivity(), rulesList, getActivity().getLayoutInflater(), achievementList);

		mCategoryList.setAdapter(mCategoryListAdapter);
		mCategoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				GamificationCategory category = mCategoryListAdapter.getItem(position);

				Bundle bundle = new Bundle();
				bundle.putString(AchievementOverviewActivity.EXTRA_CATEGORY_ID, category.getId());
				bundle.putString(AchievementOverviewActivity.EXTRA_CATEGORY_NAME, category.getName());

				Intent intent = new Intent(getActivity(), AchievementOverviewActivity.class);
				intent.putExtras(bundle);
				intent.putExtra("achievementList", achievementList);
				startActivity(intent);
			}
		});
  	}

  	public void onPause() {
		super.onPause();
	    Log.d(TAG, "onPause()");
  	}

  	public void onDestroy(){
	    super.onDestroy();
	    Log.d(TAG, "onDestroy()");
  	}
}
