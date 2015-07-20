package com.rehivetech.beeeon.gui.spinnerItem;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.location.Location;

public class ModuleSpinnerItem extends AbstractSpinnerItem {
	private Module mModule;
	private Location mLocation;
	private Context mContext;

	public ModuleSpinnerItem(Module module, Location location, String id, Context context) {
		super(id, SpinnerItemType.MODULE);
		mModule = module;
		mLocation = location;
		mContext = context;
	}

	@Override
	public Module getObject() {
		return mModule;
	}

	@Override
	public void setView(View convertView) {

		TextView ItemLabel = (TextView) convertView.findViewById(R.id.custom_spinner2_label);
		TextView ItemSubLabel = (TextView) convertView.findViewById(R.id.custom_spinner2_sublabel);
		ImageView ItemIcon = (ImageView) convertView.findViewById(R.id.custom_spinner2_icon);

		// Set the results into TextViews
		ItemLabel.setText(mModule.getName(mContext));

		if (mLocation != null) {
			ItemSubLabel.setText(mLocation.getName());
		}

		// Set the results into ImageView
		ItemIcon.setImageResource(mModule.getIconResource());
	}

	@Override
	public int getLayout() {
		return R.layout.spinner_icon_twoline_item;
	}

	@Override
	public void setDropDownView(View convertView) {
		TextView ItemLabel = (TextView) convertView.findViewById(R.id.custom_spinner2_dropdown_label);
		TextView ItemSubLabel = (TextView) convertView.findViewById(R.id.custom_spinner2_dropdown_sublabel);
		ImageView ItemIcon = (ImageView) convertView.findViewById(R.id.custom_spinner2_dropdown_icon);

		// Set the results into TextViews
		ItemLabel.setText(mModule.getName(mContext));

		if (mLocation != null) {
			ItemSubLabel.setText(mLocation.getName());
		}

		// Set the results into ImageView
		ItemIcon.setImageResource(mModule.getIconResource());

		setMView(convertView);
	}

	@Override
	public int getDropDownLayout() {
		return R.layout.spinner_icon_twoline_dropdown_item;
	}

	@Override
	public void setIsSelected() {
		getMView().setBackgroundColor(getMView().getResources().getColor(R.color.gray_light));
	}

	@Override
	public void setNotSelected() {
		getMView().setBackgroundColor(getMView().getResources().getColor(R.color.beeeon_background));
	}

}
