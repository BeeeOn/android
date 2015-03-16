package com.rehivetech.beeeon.arrayadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gamification.GamificationCategory;

import java.util.List;

/**
 * @author Jan Lamacz
 */
public class GamCategoryListAdapter extends BaseAdapter {
  Context mContext;
  Controller mController;
  LayoutInflater mInflater;
  List<GamificationCategory> mRules;

  public GamCategoryListAdapter(Context context, List<GamificationCategory> rules, LayoutInflater inflater){
    mContext = context;
    mController = Controller.getInstance(mContext);
    mInflater = inflater;
    mRules = rules;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder holder;

    if(convertView == null){
      convertView = mInflater.inflate(R.layout.profile_listview_item, parent, false);
      holder = new ViewHolder();
      holder.categoryName = (TextView) convertView.findViewById(R.id.profile_category_name);
      holder.categoryPoints = (TextView) convertView.findViewById(R.id.profile_category_points);
      holder.categoryDone = (TextView) convertView.findViewById(R.id.profile_category_done);
      holder.categoryIcon = (ImageView) convertView.findViewById(R.id.profile_category_icon);
      holder.catalogProgress = (ProgressBar) convertView.findViewById(R.id.profile_catalog_progress);
      convertView.setTag(holder);
    }
    else{
      // when we've inflated enough layouts, we just take them from memory
      holder = (ViewHolder) convertView.getTag();
    }

    // set values of item
    GamificationCategory category = (GamificationCategory) this.getItem(position);

    holder.categoryName.setText(category.getName());
    holder.categoryPoints.setText(String.valueOf(category.getPoints()));
    holder.categoryDone.setText(category.getDone() + " / " + category.getTotal());
    holder.catalogProgress.setProgress(category.getDone()*100/category.getTotal());
//    holder.categoryIcon.setImageDrawable(convertView.getResources().getDrawable(R.drawable.ic_launcher));

    return convertView;
  }

  @Override
  public int getCount() {return mRules.size();}

  @Override
  public Object getItem(int position) {return mRules.get(position);}

  @Override
  public long getItemId(int position) {return position;}

  private static class ViewHolder{
    public TextView categoryName;
    public TextView categoryPoints;
    public TextView categoryDone;
    public ImageView categoryIcon;
    public ProgressBar catalogProgress;
  }
}
