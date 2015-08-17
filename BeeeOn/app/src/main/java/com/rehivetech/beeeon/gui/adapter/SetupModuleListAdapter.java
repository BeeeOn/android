package com.rehivetech.beeeon.gui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.device.Device;

public class SetupModuleListAdapter extends BaseAdapter {

	private Context mContext;
	private Device mDevice;
	private EditText mName;

	private LayoutInflater mInflater;

	public SetupModuleListAdapter(Context context, Device device) {
		mContext = context;
		mDevice = device;

	}

	@Override
	public int getCount() {
		return mDevice.getAllModules().size();
	}

	@Override
	public String getItem(int position) {
		return mName.getText().toString();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// Create basic View
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View itemView = mInflater.inflate(R.layout.adapter_list_module_setup, parent, false);
		// Get GUI elements
		ImageView img = (ImageView) itemView.findViewById(R.id.list_module_setup_module_item_icon);
		mName = (EditText) itemView.findViewById(R.id.list_module_setup_sensor_item_name);
		// Set image resource by module type
		img.setImageResource(mDevice.getAllModules().get(position).getIconResource());
		// Set name of module
		mName.setText(mDevice.getAllModules().get(position).getName(mContext));


		// TODO Auto-generated method stub
		return itemView;
	}

}