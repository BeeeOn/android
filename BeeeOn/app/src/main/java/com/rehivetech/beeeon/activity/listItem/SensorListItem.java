package com.rehivetech.beeeon.activity.listItem;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;

public class SensorListItem extends AbstractListItem {
	private final Context mContext;
	private final Controller mController;
	private Module mModule;
	private boolean mSeparatorVisible;

	public SensorListItem(Module module, String id, Context context, boolean separator) {
		super(id, ListItemType.SENSOR);
		mModule = module;
		mSeparatorVisible = separator;
		mContext = context;
		mController = Controller.getInstance(context);

	}

	@Override
	public void setView(View itemView) {

		// Locate the TextViews in drawer_list_item.xml
		TextView txtTitle = (TextView) itemView.findViewById(R.id.titleofsensor);
		TextView txtValue = (TextView) itemView.findViewById(R.id.valueofsensor);
		TextView txtUnit = (TextView) itemView.findViewById(R.id.unitofsensor);
		TextView txtTime = (TextView) itemView.findViewById(R.id.timeofsensor);

		// Separators
		View sepMidle = itemView.findViewById(R.id.sensor_sep_midle);

		// Locate the ImageView in drawer_list_item.xml
		ImageView imgIcon = (ImageView) itemView.findViewById(R.id.iconofsensor);

		// UserSettings can be null when user is not logged in!
		SharedPreferences prefs = mController.getUserSettings();

		TimeHelper timeHelper = (prefs == null) ? null : new TimeHelper(prefs);
		UnitsHelper unitsHelper = (prefs == null) ? null : new UnitsHelper(prefs, mContext);

		// Set the results into TextViews
		txtTitle.setText(mModule.getName());

		if (unitsHelper != null) {
			txtValue.setText(unitsHelper.getStringValue(mModule.getValue()));
			txtUnit.setText(unitsHelper.getStringUnit(mModule.getValue()));
		}

		Device device = mModule.getDevice();
		Adapter adapter = mController.getAdaptersModel().getAdapter(device.getAdapterId());

		if (timeHelper != null) {
			txtTime.setText(String.format("%s %s", mContext.getString(R.string.last_update), timeHelper.formatLastUpdate(device.getLastUpdate(), adapter)));
		}

		// Set title selected for animation if is text long
		txtTitle.setSelected(true);

		// Set the results into ImageView
		imgIcon.setImageResource(mModule.getIconResource());

		if (mSeparatorVisible) {
			sepMidle.setVisibility(View.VISIBLE);
		} else {
			sepMidle.setVisibility(View.GONE);
		}
		setMView(itemView);
	}

	@Override
	public int getLayout() {
		return R.layout.sensor_listview_item;
	}

	@Override
	public void setIsSelected() {
		getMView().setBackgroundColor(getMView().getResources().getColor(R.color.light_gray));
	}

	@Override
	public void setNotSelected() {
		getMView().setBackgroundColor(getMView().getResources().getColor(R.color.beeeon_drawer_bg));
	}

	public Module getModule() {
		return mModule;
	}

}
