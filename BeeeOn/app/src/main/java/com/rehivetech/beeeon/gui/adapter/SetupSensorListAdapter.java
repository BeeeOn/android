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

public class SetupSensorListAdapter extends BaseAdapter {

	private Context mContext;
	private Device mDevice;
	private EditText mName;

	private LayoutInflater mInflater;

	public SetupSensorListAdapter(Context context, Device device) {
		mContext = context;
		mDevice = device;

	}

	@Override
	public int getCount() {
		return mDevice.getModules().size();
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
		View itemView = mInflater.inflate(R.layout.setup_sensor_listview_item, parent, false);
		// Get GUI elements
		ImageView img = (ImageView) itemView.findViewById(R.id.setup_sensor_item_icon);
		mName = (EditText) itemView.findViewById(R.id.setup_sensor_item_name);
		// Set image resource by sensor type
		img.setImageResource(mDevice.getModules().get(position).getIconResource());
		// Set name of sensor
		mName.setText(mDevice.getModules().get(position).getName(mContext));


		// TODO Auto-generated method stub
		return itemView;
	}

}
