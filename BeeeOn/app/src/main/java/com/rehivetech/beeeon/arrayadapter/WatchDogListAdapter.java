package com.rehivetech.beeeon.arrayadapter;

import android.content.Context;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.adapter.WatchDogRule;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.util.UnitsHelper;

import java.util.List;

/**
 * Created by Tomáš on 25. 2. 2015.
 */
public class WatchDogListAdapter extends BaseAdapter {

    Context mContext;
    Controller mController;
    LayoutInflater mInflater;
    List<WatchDogRule> mRules;


    public WatchDogListAdapter(Context context, List<WatchDogRule> rules, LayoutInflater inflater){
        mContext = context;
        mController = Controller.getInstance(mContext);
        mInflater = inflater;
        mRules = rules;
    }

    @Override
    public int getCount() {
        return mRules.size();
    }

    @Override
    public Object getItem(int position) {
        return mRules.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if(convertView == null){
            convertView = mInflater.inflate(R.layout.watchdog_listview_item, parent, false);

            holder = new ViewHolder();

            holder.ItemIcon = (ImageView) convertView.findViewById(R.id.watchdogItemIcon);
            holder.ItemRuleName = (TextView) convertView.findViewById(R.id.watchdogItemRuleName);
            holder.ItemSensorName = (TextView) convertView.findViewById(R.id.watchdogItemSensorName);
            holder.ItemOperator = (ImageView) convertView.findViewById(R.id.watchdogItemOperator);
            holder.ItemTreshold = (TextView) convertView.findViewById(R.id.watchdogItemTreshold);
            holder.ItemAction = (ImageView) convertView.findViewById(R.id.watchdogItemAction);
            holder.ItemSwitch = (SwitchCompat) convertView.findViewById(R.id.watchdogItemSwitch);

            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }

        WatchDogRule rule = (WatchDogRule) this.getItem(position);

        holder.ItemIcon.setImageResource(rule.getDevice().getIconResource());
        holder.ItemRuleName.setText(rule.getName());
        holder.ItemSensorName.setText(rule.getDevice().getName());
        holder.setItemOperator(rule.getOperator());

        UnitsHelper unitsHelper = new UnitsHelper(mController.getUserSettings(), mContext);
        holder.ItemTreshold.setText(unitsHelper.getStringValueUnit(rule.getTreshold()));
        holder.setItemAction(rule.getAction());
        holder.ItemSwitch.setChecked(rule.getIsActive());

        return convertView;
    }

    public void updateData(List<WatchDogRule> data){
        this.mRules = data;
        notifyDataSetChanged();
    }

    private static class ViewHolder{
        public ImageView ItemIcon;
        public TextView ItemRuleName;
        public TextView ItemSensorName;
        public ImageView ItemOperator;
        public TextView ItemTreshold;
        public ImageView ItemAction;
        public SwitchCompat ItemSwitch;

        void setItemOperator(WatchDogRule.OperatorType type){
            switch(type){
                case SMALLER:
                    ItemOperator.setImageResource(R.drawable.ic_action_previous_item);
                    break;

                case GREATER:
                    ItemOperator.setImageResource(R.drawable.ic_action_next_item);
                    break;
            }
        }

        void setItemAction(WatchDogRule.ActionType type){
            switch(type){
                case NOTIFICATION:
                    ItemAction.setImageResource(R.drawable.ic_notification);
                break;

                case ACTOR_ACTION:
                    ItemAction.setImageResource(R.drawable.ic_shutdown);
                break;
            }
        }
    }
}
