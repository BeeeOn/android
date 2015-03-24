package com.rehivetech.beeeon.arrayadapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.adapter.WatchDog;
import com.rehivetech.beeeon.adapter.device.Device;
import com.rehivetech.beeeon.adapter.device.DeviceType;
import com.rehivetech.beeeon.adapter.device.values.BaseValue;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.util.UnitsHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tomáš on 25. 2. 2015.
 */
public class WatchDogListAdapter extends BaseAdapter {

    Context mContext;
    Controller mController;
    LayoutInflater mInflater;
    List<WatchDog> mRules;
    SharedPreferences mPrefs;
    UnitsHelper mUnitsHelper;

    View.OnClickListener mSwitchOnClickListener;

    public WatchDogListAdapter(Context context, LayoutInflater inflater){
        mContext = context;
        mController = Controller.getInstance(mContext);
        mInflater = inflater;
        mRules = new ArrayList<WatchDog>();

        // UserSettings can be null when user is not logged in!
        mPrefs = mController.getUserSettings();
        mUnitsHelper = (mPrefs == null) ? null : new UnitsHelper(mPrefs, mContext);
    }

    public void setSwitchOnclickListener(View.OnClickListener clickListener){
        mSwitchOnClickListener = clickListener;
    }

    @Override
    public int getCount() {
        return mRules.size();
    }

    @Override
    public Object getItem(int position) {
        return mRules.get(position);
    }

    public WatchDog getRule(int position){
        return mRules.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        // when first time inflating layout (not when scrolling)
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
            // when we've inflated enough layouts, we just take them from memory
            holder = (ViewHolder) convertView.getTag();
        }

        // sets position of WatchDog object to retrieve it outside from the adapter
        holder.ItemSwitch.setTag(position);

        final WatchDog rule = (WatchDog) this.getItem(position);

        holder.ItemAction.setImageResource(rule.getActionIconResource());
        holder.ItemRuleName.setText(rule.getName());
        holder.ItemSwitch.setChecked(rule.isEnabled());
        if(mSwitchOnClickListener != null) {
            holder.ItemSwitch.setOnClickListener(mSwitchOnClickListener);
        }

        List<Device> devices = rule.getDevices();
        if(devices.size() > 0){
            Device deviceFirst = mController.getDevice(rule.getAdapterId(), devices.get(0).getId());
            if(deviceFirst == null) return convertView;

            holder.ItemOperator.setImageResource(rule.getOperatorIconResource());
            holder.ItemIcon.setImageResource(deviceFirst.getIconResource());
            holder.ItemSensorName.setText(deviceFirst.getName());

            if (mUnitsHelper != null) {
                BaseValue valueObj = DeviceType.createDeviceValue(deviceFirst.getType());
                valueObj.setValue(rule.getParams().get(WatchDog.PAR_TRESHOLD));
                holder.ItemTreshold.setText(mUnitsHelper.getStringValueUnit(valueObj));
            } else {
                holder.ItemTreshold.setText(rule.getParams().get(WatchDog.PAR_TRESHOLD));
            }
        }

        return convertView;
    }

    public void updateData(List<WatchDog> data){
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
    }
}
