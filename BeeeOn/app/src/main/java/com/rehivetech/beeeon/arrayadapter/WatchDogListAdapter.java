package com.rehivetech.beeeon.arrayadapter;

import android.content.Context;
import android.media.Image;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.adapter.device.Device;

import org.w3c.dom.Text;

import java.util.Enumeration;
import java.util.List;

/**
 * Created by Tomáš on 25. 2. 2015.
 */
public class WatchDogListAdapter extends BaseAdapter {

    Context mContext;
    LayoutInflater mInflater;
    List<WRule> mRules;


    public WatchDogListAdapter(Context context, List<WRule> rules, LayoutInflater inflater){
        mContext = context;
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
            holder.ItemSensorName = (TextView) convertView.findViewById(R.id.watchdogItemSensorName);
            holder.ItemOperator = (TextView) convertView.findViewById(R.id.watchdogItemOperator);
            holder.ItemTreshold = (TextView) convertView.findViewById(R.id.watchdogItemTreshold);
            holder.ItemAction = (ImageView) convertView.findViewById(R.id.watchdogItemAction);
            holder.ItemCheckbox = (CheckBox) convertView.findViewById(R.id.watchdogItemSwitch);

            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }

        WRule rule = (WRule) this.getItem(position);

        holder.ItemSensorName.setText(rule.name);
        holder.ItemOperator.setText(rule.operator);
        holder.ItemTreshold.setText(rule.treshold);
        holder.setItemAction(rule.action);
        holder.ItemCheckbox.setChecked(rule.isActive);

        return convertView;
    }

    public void updateData(List<WRule> data){
        this.mRules = data;
        notifyDataSetChanged();
    }

    private static class ViewHolder{
        public ImageView ItemIcon;
        public TextView ItemSensorName;
        public TextView ItemOperator;
        public TextView ItemTreshold;
        public ImageView ItemAction;
        public CheckBox ItemCheckbox;
        //TODO este switch treba pridat

        void setItemAction(ActionType type){
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

    public enum ActionType{ NOTIFICATION, ACTOR_ACTION };

    public static class WRule{
        String name; // TODO pryc -> bude z device->name
        String operator;
        ActionType action;
        //Device device;
        String treshold; // TODO jako nejaka hodnota devicu
        boolean isActive;

        public WRule(String na, String op, ActionType act, String tresh, boolean isAct ){
            name = na;
            operator = op;
            action = act;
            treshold = tresh;
            isActive = isAct;
        }
    }
}
