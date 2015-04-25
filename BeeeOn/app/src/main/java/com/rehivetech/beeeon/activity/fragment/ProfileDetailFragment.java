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
import com.rehivetech.beeeon.socialNetworks.BeeeOnSocialNetwork;
import com.rehivetech.beeeon.socialNetworks.BeeeOnTwitter;
import com.rehivetech.beeeon.socialNetworks.BeeeOnVKontakte;
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
	private final int totalNetworks = 3;
	private int unconnectedNetworks = 0;
	private BeeeOnFacebook mFb;
	private BeeeOnTwitter mTw;
	private BeeeOnVKontakte mVk;
	private TextView mFbName;
	private TextView mTwName;
	private TextView mVkName;

	public ProfileDetailFragment() {
		mContext = getActivity();
		Controller controller = Controller.getInstance(mContext);
	    actUser = controller.getActualUser();
//		controller.getActiveAdapter().getId();
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
		setOnClickLogout(mFb, mFbName);

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
		final AchievementList achievementList = AchievementList.getInstance(mContext);

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
				// DOWNLOAD data
				if(mFb.isPaired()) mFb.downloadUserData();
				if(mTw.isPaired()) mTw.downloadUserData();
				if(mVk.isPaired()) mVk.downloadUserData();
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
		RelativeLayout vkLayout = (RelativeLayout) mView.findViewById(R.id.profile_vkontakte);
		mFbName = (TextView) mView.findViewById(R.id.profile_facebook_name);
		mTwName = (TextView) mView.findViewById(R.id.profile_twitter_name);
		mVkName = (TextView) mView.findViewById(R.id.profile_vkontakte_name);
		ViewGroup.LayoutParams fbPar = fbLayout.getLayoutParams();
		ViewGroup.LayoutParams twPar = twLayout.getLayoutParams();
		ViewGroup.LayoutParams vkPar = vkLayout.getLayoutParams();

		mFb.addObserver(this);
		mTw.addObserver(this);
		mVk.addObserver(this);
		if(!mFb.isPaired()) {
			fbLayout.setVisibility(View.GONE);
			fbPar.height = 0;
			unconnectedNetworks++;
		}
		else {
			fbLayout.setVisibility(View.VISIBLE);
			fbPar.height = 60*mDisplayPixel;
			setOnClickLogout(mFb, mFbName);
		}
		if(!mTw.isPaired()) {
			twLayout.setVisibility(View.GONE);
			twPar.height = 0;
			unconnectedNetworks++;
		}
		else {
			twLayout.setVisibility(View.VISIBLE);
			twPar.height = 60*mDisplayPixel;
			if(mTw.getUserName() != null ) setOnClickLogout(mTw, mTwName);
			else setOnClickLogin(mTw, mTwName);
		}
		if(!mVk.isPaired()) {
			vkLayout.setVisibility(View.GONE);
			vkPar.height = 0;
			unconnectedNetworks++;
		}
		else {
			vkLayout.setVisibility(View.VISIBLE);
			vkPar.height = 60*mDisplayPixel;
			setOnClickLogout(mVk, mVkName);
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

	@Override
  	public void onPause() {
		super.onPause();
	    Log.d(TAG, "onPause()");
  	}

	@Override
  	public void onDestroy(){
	    super.onDestroy();
		Log.d(TAG, "onDestroy()");
  	}

	/** Observer.
	 * Waits until *Facebook* downloads data about user
	 * and changes this data in view.
	 */
	@Override
	public void update(Observable observable, Object o) {
		Log.d(TAG, "Newly downloaded data: "+o.toString());
		if(o.toString().equals("facebook"))
			setOnClickLogout(mFb, mFbName);
		else if(o.toString().equals("vkontakte"))
			setOnClickLogout(mVk, mVkName);
		else if(o.toString().equals("facebook login")) {
			setNetworksView();
			setMoreButtonVisibility();
		}
		else if(o.toString().equals("not_logged"))
			setOnClickLogin(mFb, mFbName);
		else if(o.toString().equals("connect_error")) {
			if(isAdded()) {
				if(mFb.isPaired()) mFbName.setText(getResources().getString(R.string.social_no_connection));
				if(mTw.isPaired()) mTwName.setText(getResources().getString(R.string.social_no_connection));
				if(mVk.isPaired()) mVkName.setText(getResources().getString(R.string.social_no_connection));
			}
			else { // sometimes (?!) crashes when trying to get resources
				if(mFb.isPaired()) mFbName.setText("No connection");
				if(mTw.isPaired()) mTwName.setText("No connection");
				if(mVk.isPaired()) mVkName.setText("No connection");
			}
		}
	}

	private void setOnClickLogout(final BeeeOnSocialNetwork network, final TextView textView) {
		if(!network.isPaired() || network.getUserName() == null) return;
		textView.setText(network.getUserName());
		textView.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				network.logOut();
				setOnClickLogin(network, textView);
				return true;
			}
		});
	}

	private void setOnClickLogin(final BeeeOnSocialNetwork network, TextView textView) {
		if(isAdded()) textView.setText(getResources().getString(R.string.login_login));
		else textView.setText("Login"); // workaround against exceptions
		textView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				network.logIn(getActivity());
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
