package com.rehivetech.beeeon.arrayadapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gamification.AchievementList;
import com.rehivetech.beeeon.gamification.GamificationCategory;
import com.rehivetech.beeeon.util.Log;

import java.util.List;

/**
 * @author Jan Lamacz
 */
public class GamCategoryListAdapter extends BaseAdapter {
	private static final String TAG = GamCategoryListAdapter.class.getSimpleName();

	Context mContext;
	Controller mController;
	LayoutInflater mInflater;
	List<GamificationCategory> mCategoryList;
	AchievementList mAchievementList;

  	public GamCategoryListAdapter(Context context, List<GamificationCategory> rules, LayoutInflater inflater,  AchievementList achievementList){
		Log.d(TAG, "constructor()");
    	mContext = context;
    	mController = Controller.getInstance(mContext);
    	mInflater = inflater;
    	mCategoryList = rules;
		mAchievementList = achievementList;
  	}

  	@Override
  	public View getView(int position, View convertView, ViewGroup parent) {
	    ViewHolder holder;

    	if(convertView == null){
      		convertView = mInflater.inflate(R.layout.profile_listview_item, parent, false);
	      	holder = new ViewHolder();
	    	holder.categoryName = (TextView) convertView.findViewById(R.id.profile_category_name);
      		holder.categoryDone = (TextView) convertView.findViewById(R.id.profile_category_done);
      		holder.categoryIcon = (ImageView) convertView.findViewById(R.id.profile_category_icon);
  			holder.catalogProgress = (ProgressBar) convertView.findViewById(R.id.profile_catalog_progress);
			holder.categoryStars[1] = (ImageView) convertView.findViewById(R.id.profile_category_star_1);
			holder.categoryStars[2] = (ImageView) convertView.findViewById(R.id.profile_category_star_2);
			holder.categoryStars[3] = (ImageView) convertView.findViewById(R.id.profile_category_star_3);
		    convertView.setTag(holder);
    	}
    	else{
      		// when we've inflated enough layouts, we just take them from memory
      		holder = (ViewHolder) convertView.getTag();
    	}

    	// sets Progress of achievements
    	GamificationCategory category = this.getItem(position);

    	holder.categoryName.setText(category.getName());
    	holder.categoryDone.setText(mAchievementList.getCompletedAchievements(category.getId()) + " / " +
									mAchievementList.getTotalAchievements(category.getId()));
    	holder.catalogProgress.setProgress(0);
//		holder.catalogProgress.setProgress((mAchievementList.getCompletedAchievements(category.getId()) * 100) /
//				mAchievementList.getTotalAchievements(category.getId()));
		Log.d(TAG, "x: " + category.getId() + ", " + mAchievementList.getTotalAchievements(category.getId()));

		// colors stars with pink gained in concrete category (0-3)
		for(int i = 1; i <= mAchievementList.getStarsCount(category.getId()); i++)
			setBg(holder.categoryStars[i], convertView.getResources().getDrawable(R.drawable.star_pink));

    	return convertView;
  	}

	/** Sets background from Java.
	 * Made bcs setBackground works from API 16 and higher
	 * and setBackgroundDrawable is marked as deprecated.
	 */
	@SuppressWarnings("deprecation")
	private static void setBg(ImageView view, Drawable image) {
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN)
			view.setBackgroundDrawable(image);
		else
			view.setBackground(image);
	}

  	@Override
  	public int getCount() {return mCategoryList.size();}

  	@Override
  	public GamificationCategory getItem(int position) {return mCategoryList.get(position);}

  	@Override
  	public long getItemId(int position) {return position;}

  	private static class ViewHolder{
    	public TextView categoryName;
	    public TextView categoryDone;
	    public ImageView categoryIcon;
	    public ProgressBar catalogProgress;
		public ImageView[] categoryStars = new ImageView[4];
  	}
}
