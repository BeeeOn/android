package com.rehivetech.beeeon.gui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.device.Device;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mlyko on 28. 7. 2015.
 */
public class DeviceRecycleAdapter extends RecyclerView.Adapter<DeviceRecycleAdapter.ViewHolder> {

	List<Device> mDevicesList = new ArrayList<>();

	public DeviceRecycleAdapter(List<Device> devicesList) {
		mDevicesList = devicesList;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		// create a new view
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_device, parent, false);
		ViewHolder vh = new ViewHolder(v);
		return vh;
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		Device device = mDevicesList.get(position);
		if(device == null) return; // TODO should show error view or loading or sth

		holder.mTitle.setText(device.getType().getNameRes());
		holder.mSubTitle.setText(device.getType().getManufacturerRes());

		int battery = device.getBattery();

		// no battery, hide info
		if(battery < 0){
			holder.mAdditional.setVisibility(View.INVISIBLE);
		}
		// otherwise set text and show
		else{
			holder.mAdditional.setText(battery + "%");
			holder.mAdditional.setVisibility(View.VISIBLE);
		}

	}

	@Override
	public int getItemCount() {
		return mDevicesList.size();
	}

	/**
	 * Updates data so that it doesn't have to be whole initialized every time data are updated
	 * @param devices
	 */
	public void updateData(List<Device> devices){
		this.mDevicesList = devices;
		this.notifyDataSetChanged();
	}

	/**
	 * ViewHolder for data
	 */
	public static class ViewHolder  extends RecyclerView.ViewHolder{
		public TextView mTitle;
		public TextView mSubTitle;
		public TextView mAdditional;

		public ViewHolder(View itemView) {
			super(itemView);
			mTitle = (TextView) itemView.findViewById(R.id.list_item_title);
			mSubTitle = (TextView) itemView.findViewById(R.id.list_item_subtitle);
			mAdditional = (TextView) itemView.findViewById(R.id.list_item_additional);
		}
	}
}
