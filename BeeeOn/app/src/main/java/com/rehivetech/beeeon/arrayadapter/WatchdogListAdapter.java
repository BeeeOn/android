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

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.geofence.SimpleGeofence;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.values.BaseValue;
import com.rehivetech.beeeon.household.watchdog.Watchdog;
import com.rehivetech.beeeon.util.UnitsHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tomáš on 25. 2. 2015.
 */
public class WatchdogListAdapter extends BaseAdapter {

	Context mContext;
	Controller mController;
	LayoutInflater mInflater;
	List<Watchdog> mRules;
	SharedPreferences mPrefs;
	UnitsHelper mUnitsHelper;

	View.OnClickListener mSwitchOnClickListener;

	public WatchdogListAdapter(Context context, LayoutInflater inflater) {
		mContext = context;
		mController = Controller.getInstance(mContext);
		mInflater = inflater;
		mRules = new ArrayList<Watchdog>();

		// UserSettings can be null when user is not logged in!
		mPrefs = mController.getUserSettings();
		mUnitsHelper = (mPrefs == null) ? null : new UnitsHelper(mPrefs, mContext);
	}

	public void setSwitchOnclickListener(View.OnClickListener clickListener) {
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

	public Watchdog getRule(int position) {
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
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.watchdog_listview_item, parent, false);

			holder = new ViewHolder();

			holder.ItemIcon = (ImageView) convertView.findViewById(R.id.watchdogItemIcon);
			holder.ItemRuleName = (TextView) convertView.findViewById(R.id.watchdogItemRuleName);
			holder.ItemSubLabel = (TextView) convertView.findViewById(R.id.watchdogItemSensorName);
			holder.ItemOperator = (ImageView) convertView.findViewById(R.id.watchdogItemOperator);
			holder.ItemTreshold = (TextView) convertView.findViewById(R.id.watchdogItemTreshold);
			holder.ItemAction = (ImageView) convertView.findViewById(R.id.watchdogItemAction);
			holder.ItemSwitch = (SwitchCompat) convertView.findViewById(R.id.watchdogItemSwitch);

			convertView.setTag(holder);
		} else {
			// when we've inflated enough layouts, we just take them from memory
			holder = (ViewHolder) convertView.getTag();
		}

		// sets position of Watchdog object to retrieve it outside from the gate
		holder.ItemSwitch.setTag(position);

		final Watchdog rule = (Watchdog) this.getItem(position);

		holder.ItemRuleName.setText(rule.getName());

		switch (rule.getType()) {
			case Watchdog.TYPE_SENSOR:
				List<String> modulesIds = rule.getModules();
				if (modulesIds.size() > 0) {
					Module moduleFirst = mController.getDevicesModel().getModule(rule.getGateId(), modulesIds.get(0));
					if (moduleFirst == null) return convertView;

					holder.ItemIcon.setImageResource(moduleFirst.getIconResource());
					holder.ItemSubLabel.setText(moduleFirst.getName());

					String par_treshold = rule.getParam(Watchdog.PAR_TRESHOLD);
					if (par_treshold != null) {
						if (mUnitsHelper != null) {
							BaseValue valueObj = BaseValue.createFromModuleType(moduleFirst.getType());
							valueObj.setValue(par_treshold);
							holder.ItemTreshold.setText(mUnitsHelper.getStringValueUnit(valueObj));
						} else {
							holder.ItemTreshold.setText(par_treshold);
						}
					}
				}
				break;

			case Watchdog.TYPE_GEOFENCE:
				holder.ItemIcon.setImageResource(R.drawable.dev_geofence);

				String geoId = rule.getGeoRegionId();
				if (geoId != null && !geoId.isEmpty()) {
					SimpleGeofence geoArea = mController.getGeofenceModel().getGeofence(mController.getActualUser().getId(), geoId);
					if (geoArea == null) return convertView;
					holder.ItemSubLabel.setText(geoArea.getName());
				}

				break;
		}

		holder.ItemOperator.setImageResource(rule.getOperatorType().getIconResource());
		holder.ItemAction.setImageResource(rule.getActionIconResource());

		holder.ItemSwitch.setChecked(rule.isEnabled());
		if (mSwitchOnClickListener != null) {
			holder.ItemSwitch.setOnClickListener(mSwitchOnClickListener);
		}

		return convertView;
	}

	public void updateData(List<Watchdog> data) {
		this.mRules = data;
		notifyDataSetChanged();
	}

	private static class ViewHolder {
		public ImageView ItemIcon;
		public TextView ItemRuleName;
		public TextView ItemSubLabel;
		public ImageView ItemOperator;
		public TextView ItemTreshold;
		public ImageView ItemAction;
		public SwitchCompat ItemSwitch;
	}
}
