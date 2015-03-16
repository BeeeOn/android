package com.rehivetech.beeeon.activity.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.adapter.Adapter;
import com.rehivetech.beeeon.adapter.WatchDogRule;
import com.rehivetech.beeeon.arrayadapter.GamCategoryListAdapter;
import com.rehivetech.beeeon.controller.Controller;
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

  private TextView userName;
  private TextView userDetail;
  private ImageView userImage;
  private ListView mCategoryList;

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
    userDetail.setText(getString(R.string.gam_level) + " 1");
    userImage.setImageBitmap(picture);
  }

  private void redrawCategories() {

    List<GamificationCategory> rulesList = new ArrayList<>();
    rulesList.add(new GamificationCategory("1", getString(R.string.gam_category_app), 4, 8));
    rulesList.add(new GamificationCategory("2", getString(R.string.gam_category_friends), 2, 7));
    rulesList.add(new GamificationCategory("3", getString(R.string.gam_category_senzors), 17, 20));

    GamCategoryListAdapter mCategoryListAdapter = new GamCategoryListAdapter(getActivity(), rulesList, getActivity().getLayoutInflater());

    mCategoryList.setAdapter(mCategoryListAdapter);
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
