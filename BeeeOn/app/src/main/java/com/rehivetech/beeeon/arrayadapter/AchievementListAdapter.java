package com.rehivetech.beeeon.arrayadapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gamification.AchievementListClickListener;
import com.rehivetech.beeeon.gamification.AchievementListItem;
import com.rehivetech.beeeon.gamification.AchievementListOnClickListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Jan Lamacz
 */
public class AchievementListAdapter extends BaseAdapter implements Filterable {
	private LayoutInflater mInflater;
	private int mDisplayPixel;
	private ItemFilter mFilter = new ItemFilter();

	private List<AchievementListItem> mAchievementList;
	private List<AchievementListItem> mFilteredList;
	private AchievementListOnClickListener mCallback;

	public AchievementListAdapter(LayoutInflater inflater, String categoryId, AchievementListOnClickListener callback, List<AchievementListItem> achievements, Context context) {
		mInflater = inflater;
		mCallback = callback;
		mAchievementList = achievements;
		mFilteredList = mAchievementList;
		mFilter.filter(categoryId);

		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		mDisplayPixel = (int) metrics.density;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.achievement_listview_item, parent, false);
			holder = new ViewHolder();
			holder.achievementLayout = (RelativeLayout) convertView.findViewById(R.id.achievement_item_layout);
			holder.achievementProgressLayout = (RelativeLayout) convertView.findViewById(R.id.achievement_progress_layout);
			holder.achievementName = (TextView) convertView.findViewById(R.id.achievement_list_name);
			holder.achievementDescription = (TextView) convertView.findViewById(R.id.achievement_list_description);
			holder.achievementPoints = (TextView) convertView.findViewById(R.id.achievement_list_points);
			holder.achievementDate = (TextView) convertView.findViewById(R.id.achievement_list_date);
			holder.achievementTick = (ImageView) convertView.findViewById(R.id.achievement_list_tick);
			holder.achievementShare = (ImageView) convertView.findViewById(R.id.achievement_list_share);
			holder.achievementProgress = (ProgressBar) convertView.findViewById(R.id.achievement_progress);
			holder.achievementProgressText = (TextView) convertView.findViewById(R.id.achievement_progress_text);
			convertView.setTag(holder);
		} else {
			// when we've inflated enough layouts, we just take them from memory
			holder = (ViewHolder) convertView.getTag();
		}

		// set values of item
		AchievementListItem achievement = this.getItem(position);

		holder.achievementName.setText(achievement.getName());
		holder.achievementDescription.setText(achievement.getDescription());
		holder.achievementPoints.setText(String.valueOf(achievement.getPoints()));
		if (achievement.isDone()) {
			if (achievement.getLevel() >= 3)
				setBg(holder.achievementPoints, convertView.getResources().getDrawable(R.drawable.hexagon_cyan3));
			else if (achievement.getLevel() == 2)
				setBg(holder.achievementPoints, convertView.getResources().getDrawable(R.drawable.hexagon_cyan2));
			else
				setBg(holder.achievementPoints, convertView.getResources().getDrawable(R.drawable.hexagon_cyan1));
			holder.achievementName.setTextColor(convertView.getResources().getColor(R.color.beeeon_primary_cyan));
			holder.achievementDescription.setTextColor(convertView.getResources().getColor(R.color.beeeon_secundary_pink));
			holder.achievementDate.setText(achievement.getDate());
			holder.achievementDate.setVisibility(View.VISIBLE);
			holder.achievementTick.setVisibility(View.VISIBLE);
			holder.achievementShare.setVisibility(View.VISIBLE);
			holder.achievementDescription.setVisibility(View.VISIBLE);
			holder.achievementShare.setImageResource(R.drawable.share);
			holder.achievementProgressText.setVisibility(View.GONE);
			holder.achievementProgressLayout.setVisibility(View.GONE);
			holder.achievementLayout.getLayoutParams().height = 100 * mDisplayPixel;
			holder.achievementShare.setOnClickListener(new AchievementListClickListener(mCallback, position));
		} else if (achievement.isVisible()) {
			setBg(holder.achievementPoints, convertView.getResources().getDrawable(R.drawable.hexagon_gray));
			holder.achievementName.setTextColor(convertView.getResources().getColor(R.color.beeeon_text_color));
			holder.achievementDescription.setTextColor(convertView.getResources().getColor(R.color.beeeon_text_hint));
			holder.achievementDate.setVisibility(View.INVISIBLE);
			holder.achievementTick.setVisibility(View.GONE);
			holder.achievementShare.setVisibility(View.GONE);
			holder.achievementDescription.setVisibility(View.VISIBLE);
			if (achievement.getTotalProgress() > 1) {
				holder.achievementLayout.getLayoutParams().height = 125 * mDisplayPixel;
				holder.achievementProgressLayout.setVisibility(View.VISIBLE);
				holder.achievementProgress.setMax(achievement.getTotalProgress());
				holder.achievementProgress.setProgress(achievement.getCurrentProgress());
				holder.achievementProgressText.setVisibility(View.VISIBLE);
				holder.achievementProgressText.setText(achievement.getProgressString());
			} else {
				holder.achievementLayout.getLayoutParams().height = 100 * mDisplayPixel;
				holder.achievementProgressLayout.setVisibility(View.GONE);
				holder.achievementProgressText.setVisibility(View.GONE);
			}
		} else {
			setBg(holder.achievementPoints, convertView.getResources().getDrawable(R.drawable.hexagon_lightgray));
			holder.achievementName.setTextColor(convertView.getResources().getColor(R.color.beeeon_separator));
			holder.achievementShare.setOnClickListener(null);
			holder.achievementLayout.getLayoutParams().height = 75 * mDisplayPixel;
			holder.achievementShare.setVisibility(View.VISIBLE);
			holder.achievementShare.setImageResource(R.drawable.question);
			holder.achievementDescription.setVisibility(View.GONE);
			holder.achievementDate.setVisibility(View.GONE);
			holder.achievementTick.setVisibility(View.GONE);
			holder.achievementProgressLayout.setVisibility(View.GONE);
			holder.achievementProgressText.setVisibility(View.GONE);
		}

		return convertView;
	}

	/**
	 * Sets background from Java.
	 * Made bcs setBackground works from API 16 and higher
	 * and setBackgroundDrawable is marked as deprecated.
	 */
	@SuppressWarnings("deprecation")
	private static void setBg(TextView view, Drawable image) {
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN)
			view.setBackgroundDrawable(image);
		else
			view.setBackground(image);
	}

	@Override
	public int getCount() {
		return mFilteredList.size();
	}

	@Override
	public AchievementListItem getItem(int position) {
		return mFilteredList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public Filter getFilter() {
		return mFilter;
	}

	/**
	 * Filter for specific category.
	 * Goes through all (downloaded) achievements and returns
	 * the ones belonging to @param constraint (categoryId)
	 * Filtered achievements are ordered by date they were obtained
	 * (from newest to oldest) and the not completed ones are
	 * taken to the very end of this list.
	 */
	private class ItemFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			String categoryId = constraint.toString();

			FilterResults results = new FilterResults();

			final List<AchievementListItem> list = mAchievementList;

			int count = list.size();
			final ArrayList<AchievementListItem> nlist = new ArrayList<>(count);

			AchievementListItem achievement;

			for (int i = 0; i < count; i++) {
				achievement = list.get(i);
				if (achievement.getCategory().contains(categoryId))
					nlist.add(achievement);
			}
			Collections.sort(nlist);
			results.values = nlist;
			results.count = nlist.size();

			return results;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			mFilteredList = (ArrayList<AchievementListItem>) results.values;
			notifyDataSetChanged();
		}
	}

	private static class ViewHolder {
		public RelativeLayout achievementLayout;
		public RelativeLayout achievementProgressLayout;
		public TextView achievementName;
		public TextView achievementDescription;
		public TextView achievementPoints;
		public TextView achievementDate;
		public ImageView achievementTick;
		public ImageView achievementShare;
		public ProgressBar achievementProgress;
		public TextView achievementProgressText;
	}
}
