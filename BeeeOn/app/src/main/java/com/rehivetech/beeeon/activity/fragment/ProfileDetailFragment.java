package com.rehivetech.beeeon.activity.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v4.app.DialogFragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.melnykov.fab.FloatingActionButton;
import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.activity.AchievementOverviewActivity;
import com.rehivetech.beeeon.arrayadapter.GamCategoryListAdapter;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gamification.AchievementList;
import com.rehivetech.beeeon.gamification.GamificationCategory;
import com.rehivetech.beeeon.household.ActualUser;
import com.rehivetech.beeeon.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Jan Lamacz
 */
public class ProfileDetailFragment extends Fragment {
  	private static final String TAG = ProfileDetailFragment.class.getSimpleName();
  	private View mView;
  	private Controller mController;
  	private ActualUser actUser;
	private SharedPreferences mPrefs;
  	private GamCategoryListAdapter mCategoryListAdapter;
	public CallbackManager mFacebookCallbackManager;

	// GUI
	private TextView userName;
  	private TextView userLevel;
  	private ImageView userImage;
  	private ListView mCategoryList;
	private TextView mPoints;
	private TextView mMoreAccounts;
	private FloatingActionButton mMoreArrow;
	private FloatingActionButton mMoreAdd;
	private RelativeLayout mMoreVisible;
	private RelativeLayout mMoreLayout;

	// SocialNetworks
	private boolean showMoreAccounts = false;
	private int notregistredNetworks = 0;
	private final int totalNetworks = 2;
	private CharSequence[] socialNetworks = new CharSequence[totalNetworks];
	private String FBlogin;
	private String TWlogin;

	public ProfileDetailFragment() {
	    mController = Controller.getInstance(getActivity());
		mPrefs = mController.getUserSettings();
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

		// Shared preferences
		FBlogin = mPrefs.getString(Constants.PERSISTANCE_PREF_LOGIN_FACEBOOK,null);
		TWlogin = mPrefs.getString(Constants.PERSISTANCE_PREF_LOGIN_TWITTER,null);
		if(FBlogin == null) socialNetworks[notregistredNetworks++] = "Facebook";
		if(TWlogin == null) socialNetworks[notregistredNetworks++] = "Twitter";

    	// Inflate the layout for this fragment
    	mView = inflater.inflate(R.layout.profile_detail, container, false);
    	userName = (TextView) mView.findViewById(R.id.profile_name);
    	userLevel = (TextView) mView.findViewById(R.id.profile_detail);
    	userImage = (ImageView) mView.findViewById(R.id.profile_image);
    	mCategoryList = (ListView) mView.findViewById(R.id.gam_category_list);
		mPoints = (TextView) mView.findViewById(R.id.profile_points);
		mMoreAccounts = (TextView) mView.findViewById(R.id.profile_more_text);
		mMoreArrow = (FloatingActionButton) mView.findViewById(R.id.profile_more_arrow);
		mMoreAdd = (FloatingActionButton) mView.findViewById(R.id.profile_more_add);
		mMoreVisible = (RelativeLayout) mView.findViewById(R.id.profile_more_accounts);
		mMoreLayout = (RelativeLayout) mView.findViewById(R.id.profile_more);
		setMoreVisibility();

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

		//GUI components for social networks accounts
		if(notregistredNetworks > 0) {// more known networks to by added
			mMoreAdd.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					new SocialNetworkDialog().show(getFragmentManager(), TAG);
				}
			});
			mMoreAdd.setVisibility(View.VISIBLE);
		}
		else
			mMoreAdd.setVisibility(View.INVISIBLE);
		if(notregistredNetworks != totalNetworks) { //at least one network is added
			mMoreArrow.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showMoreAccounts = !showMoreAccounts;
					setMoreVisibility();
				}
			});
			mMoreArrow.setVisibility(View.VISIBLE);
		}
		else
			mMoreArrow.setVisibility(View.INVISIBLE);
  	}

  	private void redrawCategories() {
		// list of all achievements (probably downloaded from server)
		final AchievementList achievementList = new AchievementList();

		userLevel.setText(getString(R.string.profile_level) + " " + achievementList.getLevel());
		mPoints.setText(String.valueOf(achievementList.getTotalPoints()));

		List<GamificationCategory> rulesList = new ArrayList<>();
		rulesList.add(new GamificationCategory("0", getString(R.string.profile_category_app)));
		rulesList.add(new GamificationCategory("1", getString(R.string.profile_category_friends)));
		rulesList.add(new GamificationCategory("2", getString(R.string.profile_category_senzors)));

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
			intent.putExtra("achievementList", achievementList); // list of all achievements
			startActivity(intent);
			}
		});
  	}

	private void setMoreVisibility() {
		if(notregistredNetworks == totalNetworks) { //none social network is paired
			mMoreArrow.setVisibility(View.INVISIBLE);
			mMoreVisible.setVisibility(View.INVISIBLE);
		}
		else {	//at least one network is connected, allow to show her profile
			if (showMoreAccounts) {
				mMoreLayout.setMinimumHeight(250);
				mMoreVisible.setVisibility(View.VISIBLE);
				rotate(0, 90);
			} else {
				mMoreLayout.setMinimumHeight(40);
				mMoreVisible.setVisibility(View.INVISIBLE);
				rotate(90, 0);
			}
		}
	}

	private void rotate(float start, float end) {
		final RotateAnimation rotateAnim = new RotateAnimation(0.0f, end,
			RotateAnimation.RELATIVE_TO_SELF, 0.5f,
			RotateAnimation.RELATIVE_TO_SELF, 0.5f);

		rotateAnim.setDuration(100);
		rotateAnim.setFillAfter(true);
		mMoreArrow.startAnimation(rotateAnim);
	}

  	public void onPause() {
		super.onPause();
	    Log.d(TAG, "onPause()");
  	}

  	public void onDestroy(){
	    super.onDestroy();
	    Log.d(TAG, "onDestroy()");
  	}

	private class SocialNetworkDialog extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.profile_new_account)
				.setItems(socialNetworks, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
							case 0:
								LoginManager.getInstance().logInWithReadPermissions(getActivity(), Arrays.asList("public_profile"));
								break;
							case 1:
								Toast.makeText(getActivity(), "Not implemented yet", Toast.LENGTH_LONG).show();
								break;
						}
					}
				})
				.setNegativeButton(R.string.action_close, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {}
				});
			return builder.create();
		}
	}
}
