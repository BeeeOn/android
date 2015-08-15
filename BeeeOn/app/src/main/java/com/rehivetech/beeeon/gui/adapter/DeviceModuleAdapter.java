package com.rehivetech.beeeon.gui.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rehivetech.beeeon.IconResourceType;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.util.UnitsHelper;

import java.util.List;

/**
 * @author martin on 13.8.2015.
 */
public class DeviceModuleAdapter extends RecyclerView.Adapter<DeviceModuleAdapter.DeviceModuleAdapterViewHolder> {

	public static final String TAG = DeviceModuleAdapter.class.getSimpleName();
	private Context mContext;
	private List<Module> mModuleList;

	public DeviceModuleAdapter(Context context, List<Module> moduleList) {
		mContext = context;
		mModuleList = moduleList;
	}

	@Override
	public DeviceModuleAdapter.DeviceModuleAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_device_module, parent, false);
		return new DeviceModuleAdapterViewHolder(view);
	}

	@Override
	public void onBindViewHolder(DeviceModuleAdapter.DeviceModuleAdapterViewHolder holder, int position) {
		Module module = mModuleList.get(position);
		SharedPreferences prefs = Controller.getInstance(mContext).getUserSettings();
		UnitsHelper unitsHelper = (prefs == null) ? null : new UnitsHelper(prefs, mContext);

		holder.mTitle.setText(module.getName(mContext));
		if (unitsHelper != null) {
			holder.mValue.setText(String.format("%s %s",
					unitsHelper.getStringValue(module.getValue()),
					unitsHelper.getStringUnit(module.getValue())));
		}
		holder.mIcon.setImageResource(module.getIconResource(IconResourceType.DARK));

	}

	@Override
	public int getItemCount() {
		return mModuleList.size();
	}

	public class DeviceModuleAdapterViewHolder extends RecyclerView.ViewHolder {

		public final ImageView mIcon;
		public final TextView mTitle;
		public final TextView mValue;

		public DeviceModuleAdapterViewHolder(View itemView) {
			super(itemView);
			mIcon = (ImageView) itemView.findViewById(R.id.list_module_item_icon);
			mTitle = (TextView) itemView.findViewById(R.id.list_module_item_title);
			mValue = (TextView) itemView.findViewById(R.id.list_module_item_value);
		}
	}
}
