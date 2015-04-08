package com.rehivetech.beeeon.activity.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.activity.AchievementOverviewActivity;
import com.rehivetech.beeeon.activity.dialog.PairNetworkFragmentDialog;
import com.rehivetech.beeeon.arrayadapter.GamCategoryListAdapter;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gamification.AchievementList;
import com.rehivetech.beeeon.gamification.GamificationCategory;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.socialNetworks.BeeeOnFacebook;
import com.rehivetech.beeeon.socialNetworks.BeeeOnTwitter;
import com.rehivetech.beeeon.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Design pattern Observer
 * @author Jan Lamacz
 */
public class ProfileDetailFragment extends Fragment implements Observer {
  	private static final String TAG = ProfileDetailFragment.class.getSimpleName();
  	private User actUser;
  	private GamCategoryListAdapter mCategoryListAdapter;
	private View mView;
	private Context mContext;
	private int mDisplayPixel;

	// GUI
	private TextView userName;
  	private TextView userLevel;
  	private ImageView userImage;
  	private ListView mCategoryList;
	private TextView mPoints;
	private FloatingActionButton mMoreArrow;
	private FloatingActionButton mMoreAdd;
	private RelativeLayout mMoreVisible;
	private RelativeLayout mMoreLayout;

	// SocialNetworks
	private boolean showMoreAccounts = false;
	private final int totalNetworks = 2;
	private int unconnectedNetworks = 0;
	private BeeeOnFacebook mFb;
	private BeeeOnTwitter mTw;
	private TextView mFbName;
	private TextView mTwName;

	public ProfileDetailFragment() {
		mContext = getActivity();
		Controller controller = Controller.getInstance(mContext);
	    actUser = controller.getActualUser();
  	}

  	@Override
  	public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    	Log.d(TAG, "onCreateView()");

		DisplayMetrics metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
		mDisplayPixel = (int) metrics.density;

    	// Inflate the layout for this fragment
		mView = inflater.inflate(R.layout.profile_detail, container, false);
    	userName = (TextView) mView.findViewById(R.id.profile_name);
    	userLevel = (TextView) mView.findViewById(R.id.profile_detail);
    	userImage = (ImageView) mView.findViewById(R.id.profile_image);
    	mCategoryList = (ListView) mView.findViewById(R.id.gam_category_list);
		mPoints = (TextView) mView.findViewById(R.id.profile_points);
		mMoreArrow = (FloatingActionButton) mView.findViewById(R.id.profile_more_arrow);
		mMoreAdd = (FloatingActionButton) mView.findViewById(R.id.profile_more_add);
		mMoreVisible = (RelativeLayout) mView.findViewById(R.id.profile_more_accounts);
		mMoreLayout = (RelativeLayout) mView.findViewById(R.id.profile_more);

		mFb = Facebook.getInstance(getActivity());
		mTw = Twitter.getInstance(getActivity());
//		setNetworksView();

//		setMoreButtonVisibility();
    	redrawCategories();
		mMoreLayout.getLayoutParams().height = (mDisplayPixel*60);

    	return mView;
  	}

 	@Override
  	public void onActivityCreated(Bundle savedInstanceState){
	    super.onActivityCreated(savedInstanceState);
	    Log.d(TAG, "onActivityCreated()");

	    Bitmap picture = actUser.getPicture();
	    if (picture == null)
	    	picture = actUser.getDefaultPicture(getActivity());
	    userName.setText(actUser.getFullName());
	    userImage.setImageBitmap(picture);

		//GUI components for social networks accounts
//		if(socialNetworks.size() > 0) {// more known networks to by added
//			mMoreAdd.setOnClickListener(new View.OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					new NetworkChooseDialog().show(getFragmentManager(), TAG);
//				}
//			});
//			mMoreAdd.setVisibility(View.VISIBLE);
//		}
//		else
//			mMoreAdd.setVisibility(View.INVISIBLE);
//		if(socialNetworks.size() != totalNetworks) { //at least one network is added
//			mMoreArrow.setOnClickListener(new View.OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					showMoreAccounts = !showMoreAccounts;
//					setMoreButtonVisibility();
//				}
//			});
//			mMoreArrow.setVisibility(View.VISIBLE);
//		}
//		else
			mMoreArrow.setVisibility(View.INVISIBLE);
  	}

  	private void redrawCategories() {
		final AchievementList achievementList = AchievementList.getInstance();

		userLevel.setText(getString(R.string.profile_level) + " " + achievementList.getLevel());
		mPoints.setText(String.valueOf(achievementList.getTotalPoints()));

		List<GamificationCategory> rulesList = new ArrayList<>();
		rulesList.add(new GamificationCategory("0", getString(R.string.profile_category_app)));
		rulesList.add(new GamificationCategory("1", getString(R.string.profile_category_friends)));
		rulesList.add(new GamificationCategory("2", getString(R.string.profile_category_senzors)));

		mCategoryListAdapter = new GamCategoryListAdapter(mContext, rulesList, getActivity().getLayoutInflater(), achievementList);

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
			startActivity(intent);
			}
		});
  	}

	private void setMoreButtonVisibility() {
		// just for offic release, will be changed soon
		{
			mMoreArrow.setVisibility(View.INVISIBLE);
			mMoreVisible.setVisibility(View.INVISIBLE);
		}

		if(socialNetworks.size() == totalNetworks) { //none social network is paired
			mMoreArrow.setVisibility(View.INVISIBLE);
			mMoreVisible.setVisibility(View.INVISIBLE);
		}
		else {	//at least one network is connected, allow to show the profile
			if (showMoreAccounts) { // SHOW info
				mMoreLayout.getLayoutParams().height = (mDisplayPixel*60)+((totalNetworks-unconnectedNetworks)*(mDisplayPixel*65));
				mMoreLayout.requestLayout();
				mMoreVisible.setVisibility(View.VISIBLE);
				rotate(90);
			} else { // HIDE info
				mMoreLayout.getLayoutParams().height = 60*mDisplayPixel;
				mMoreLayout.requestLayout();
				mMoreVisible.setVisibility(View.INVISIBLE);
				rotate(0);
			}
		}
	}

	private void setNetworksView() {
		RelativeLayout fbLayout = (RelativeLayout) mView.findViewById(R.id.profile_facebook);
		RelativeLayout twLayout = (RelativeLayout) mView.findViewById(R.id.profile_twitter);
		ViewGroup.LayoutParams fbPar = fbLayout.getLayoutParams();
		ViewGroup.LayoutParams twPar = twLayout.getLayoutParams();

		if(!mFb.isPaired()) {
			fbLayout.setVisibility(View.INVISIBLE);
			fbPar.height = 0;
			unconnectedNetworks++;
		}
		else {
			mFbName = (TextView) mView.findViewById(R.id.profile_facebook_name);
			if(mFb.getUserName() != null) mFbName.setText(mFb.getUserName());
			fbLayout.setVisibility(View.VISIBLE);
			fbPar.height = 60*mDisplayPixel;
			mFb.addObserver(this);
			mFb.downloadUserData();
		}
		if(!mTw.isPaired()) {
			twLayout.setVisibility(View.INVISIBLE);
			twPar.height = 0;
			unconnectedNetworks++;
		}
		else {
			mTwName = (TextView) mView.findViewById(R.id.profile_twitter_name);
			if(mTw.getUserName() != null) mTwName.setText(mTw.getUserName());
			twLayout.setVisibility(View.VISIBLE);
			twPar.height = 60*mDisplayPixel;
		}
	}

	/**
	 * Rotates arrow that shows and hides additional info
	 * about connected social networks.
	 */
	private void rotate(float end) {
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

	/**
	 * Redraws Facebook icons after user has logged in,
	 * so it is visible immediately
	 */
	public void updateFacebookLoginView() {
		setNetworksView();
		setMoreButtonVisibility();
	}

	/** Observer.
	 * Waits until *Facebook* downloads data about user
	 * and changes this data in view.
	 */
	@Override
	public void update(Observable observable, Object o) {
		Log.d(TAG, "Facebook new data: "+o.toString());
		if(o.toString().equals("userName"))
			fbSetOnClickLogout();
		else if(o.toString().equals("connect_error")) {
			if(isAdded()) mFbName.setText(getResources().getString(R.string.social_no_connection));
			else mFbName.setText("No connection"); // falls when trying to get resources
		}
		else if(o.toString().equals("not_logged"))
			fbSetOnClickLogin();
	}

	private void fbSetOnClickLogout() {
		mFbName.setText(mFb.getUserName());
		mFbName.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				mFb.logOut();
				fbSetOnClickLogin();
				return true;
			}
		});
	}

	private void fbSetOnClickLogin() {
		if(isAdded()) mFbName.setText(getResources().getString(R.string.login_login));
		else mFbName.setText("Login"); // workaround against exceptions
		mFbName.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mFb.logIn(getActivity());
			}
		});
	}

	/**
	 * Dialog shows social networks (Facebook and Twitter for now), that are not
	 * already paired and opens their own GUI to connect.
	 */
//	public class NetworkChooseDialog extends DialogFragment {
//		@Override
//		public Dialog onCreateDialog(Bundle savedInstanceState) {
//			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//			builder.setTitle(R.string.profile_new_account)
//				.setItems(socialNetworks.toArray(new CharSequence[socialNetworks.size()]),
//						new DialogInterface.OnClickListener() {
//							public void onClick(DialogInterface dialog, int which) {
//								if (which == 0 && !mFb.isPaired())
//									mFb.logIn(getActivity());
//								if (which == 1 && !mFb.isPaired() ||
//										which == 0 && mFb.isPaired())
//									Toast.makeText(getActivity(), "Not implemented yet", Toast.LENGTH_LONG).show();
//							}
//						})
//				.setNegativeButton(R.string.action_close, new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int id) {
//					}
//				});
//			return builder.create();
//		}
//	}
}
