package com.rehivetech.beeeon.activity.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
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

import com.melnykov.fab.FloatingActionButton;
import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.activity.AchievementOverviewActivity;
import com.rehivetech.beeeon.arrayadapter.GamCategoryListAdapter;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gamification.AchievementList;
import com.rehivetech.beeeon.gamification.GamificationCategory;
import com.rehivetech.beeeon.household.User;
import com.rehivetech.beeeon.socialNetworks.Facebook;
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
	private SharedPreferences mPrefs;
  	private GamCategoryListAdapter mCategoryListAdapter;

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
	private ArrayList<CharSequence> socialNetworks = new ArrayList<>();
	private String FBlogin;
	private String TWlogin;
	private Facebook mFb;
	private TextView mFbName;

	public ProfileDetailFragment() {
		Controller controller = Controller.getInstance(getActivity());
		mPrefs = controller.getUserSettings();
	    actUser = controller.getActualUser();
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
		View mView = inflater.inflate(R.layout.profile_detail, container, false);
    	userName = (TextView) mView.findViewById(R.id.profile_name);
    	userLevel = (TextView) mView.findViewById(R.id.profile_detail);
    	userImage = (ImageView) mView.findViewById(R.id.profile_image);
    	mCategoryList = (ListView) mView.findViewById(R.id.gam_category_list);
		mPoints = (TextView) mView.findViewById(R.id.profile_points);
		mMoreArrow = (FloatingActionButton) mView.findViewById(R.id.profile_more_arrow);
		mMoreAdd = (FloatingActionButton) mView.findViewById(R.id.profile_more_add);
		mMoreVisible = (RelativeLayout) mView.findViewById(R.id.profile_more_accounts);
		mMoreLayout = (RelativeLayout) mView.findViewById(R.id.profile_more);

		RelativeLayout fbLayout = (RelativeLayout) mView.findViewById(R.id.profile_facebook);
		RelativeLayout twLayout = (RelativeLayout) mView.findViewById(R.id.profile_twitter);
		ViewGroup.LayoutParams fbPar = fbLayout.getLayoutParams();
		ViewGroup.LayoutParams twPar = twLayout.getLayoutParams();

		// Shared preferences
		FBlogin = mPrefs.getString(Constants.PERSISTANCE_PREF_LOGIN_FACEBOOK,null);
		TWlogin = mPrefs.getString(Constants.PERSISTANCE_PREF_LOGIN_TWITTER,null);
		if(FBlogin == null) {
			fbLayout.setVisibility(View.INVISIBLE);
			fbPar.height = 0;
			socialNetworks.add("Facebook");
		}
		else {
			mFbName = (TextView) mView.findViewById(R.id.profile_facebook_name);
			fbLayout.setVisibility(View.VISIBLE);
			fbPar.height = 70;
			mFb = Facebook.getInstance();
			mFb.addObserver(this);
			mFb.downloadUserData();
		}
		if(TWlogin == null) {
			twLayout.setVisibility(View.INVISIBLE);
			twPar.height = 0;
			socialNetworks.add("Twitter");
		}
		else {
			twLayout.setVisibility(View.VISIBLE);
			twPar.height = 70;
		}

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
	    userName.setText(actUser.getFullName());
	    userImage.setImageBitmap(picture);

		//GUI components for social networks accounts
		if(socialNetworks.size() > 0) {// more known networks to by added
			mMoreAdd.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					new NetworkChooseDialog().show(getFragmentManager(), TAG);
				}
			});
			mMoreAdd.setVisibility(View.VISIBLE);
		}
		else
			mMoreAdd.setVisibility(View.INVISIBLE);
		if(socialNetworks.size() != totalNetworks) { //at least one network is added
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
		// list of all achievements (.probably. downloaded from server)
		final AchievementList achievementList = AchievementList.getInstance();

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
//			intent.putExtra("achievementList", achievementList); // list of all achievements
			startActivity(intent);
			}
		});
  	}

	private void setMoreVisibility() {
		if(socialNetworks.size() == totalNetworks) { //none social network is paired
			mMoreArrow.setVisibility(View.INVISIBLE);
			mMoreVisible.setVisibility(View.INVISIBLE);
		}
		else {	//at least one network is connected, allow to show the profile
			if (showMoreAccounts) { // SHOW info
				mMoreLayout.getLayoutParams().height = 60+((totalNetworks-socialNetworks.size())*70);
				mMoreLayout.requestLayout();
				mMoreVisible.setVisibility(View.VISIBLE);
				rotate(90);
			} else { // HIDE info
				mMoreLayout.getLayoutParams().height = 60;
				mMoreLayout.requestLayout();
				mMoreVisible.setVisibility(View.INVISIBLE);
				rotate(0);
			}
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

	/** Observer.
	 * Waits until *Facebook* downloads data about user
	 * and changes this data in view.
	 */
	@Override
	public void update(Observable observable, Object o) {
		Log.d(TAG, "Facebook "+o.toString()+" downloaded");
		if(o.toString().equals("userName"))
			fbSetOnClickLogout();
		else if(o.toString().equals("connect_error"))
			mFbName.setText(getResources().getString(R.string.NetworkError___SHORT_NO_CONNECTION));
		else if(o.toString().equals("not_logged"))
			fbSetOnClickLogin();
//		else if(o.toString().equals("profilePicture"))
//			mFbName.setText(mFb.getUserPicture());
	}

	private void fbSetOnClickLogout() {
		mFbName.setText(mFb.getUserName());
		mFbName.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				mFb.logOut(getActivity());
				fbSetOnClickLogin();
				return true;
			}
		});
	}

	private void fbSetOnClickLogin() {
		if(isAdded()) mFbName.setText(getResources().getString(R.string.login_login));
		else mFbName.setText("LogIn");
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
	public class NetworkChooseDialog extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.profile_new_account)
				.setItems(socialNetworks.toArray(new CharSequence[socialNetworks.size()]),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								if (which == 0 && FBlogin == null)
									mFb.logIn(getActivity());
								if (which == 1 && FBlogin == null ||
									which == 0 && FBlogin != null)
									Toast.makeText(getActivity(), "Not implemented yet", Toast.LENGTH_LONG).show();
							}
						})
				.setNegativeButton(R.string.action_close, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
					}
				});
			return builder.create();
		}
	}
}
