package com.rehivetech.beeeon.arrayadapter;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.adapter.Adapter;
import com.rehivetech.beeeon.adapter.device.Device;
import com.rehivetech.beeeon.adapter.device.Facility;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;

public class SensorListAdapter extends BaseAdapter {

	private static final int MARGIN_LEFT_RIGHT = 2;
	private static final int MARGIN_TOP = 10;
	private static final int MARGIN_BOTTOM = 0;
	private static final int MARGIN_TOP_M_L = -2;

	// Declare Variables
	private Context mContext;
	private LayoutInflater inflater;
	private boolean mShowAdd;
	private OnClickListener mListener;
	private List<Device> mDevices;

	private final Controller mController;

	public SensorListAdapter(Context context, List<Device> devices, OnClickListener listener) {
		mContext = context;
		mController = Controller.getInstance(context.getApplicationContext());
		mDevices = devices;
		mShowAdd = false;
		if(!devices.isEmpty()) {
			Adapter tmpAda = mController.getAdapter(devices.get(0).getFacility().getAdapterId());
			if(tmpAda != null)
				mShowAdd = mController.isUserAllowed(tmpAda.getRole());
		}
		mListener = listener;
	}

	@Override
	public int getCount() {
		return mDevices.size() + (mShowAdd ? 1 : 0);
	}

	@Override
	public Object getItem(int position) {
		return mDevices.get(position).getName();
	}

	@Override
	public long getItemId(int position) {
		return position; // TODO: what's this?
	}

	public Device getDevice(int position) {
		return mDevices.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (position < mDevices.size()) {
			return addItem(position, convertView, parent);
		}
		return addAddSensor(convertView, parent);
	}

	private View addAddSensor(View convertView, ViewGroup parent) {
		inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View itemView = inflater.inflate(R.layout.sensor_listview_addsensor, parent, false);
		itemView.setClickable(false);
		itemView.setOnClickListener(null);
		ImageView img = (ImageView) itemView.findViewById(R.id.sensor_listview_addsensor_image);
		if (img != null)
			img.setOnClickListener(mListener);
		return itemView;
	}

	private View addItem(int position, View convertView, ViewGroup parent) {
		inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View itemView = inflater.inflate(R.layout.sensor_listview_item, parent, false);

		// Locate the TextViews in drawer_list_item.xml
		TextView txtTitle = (TextView) itemView.findViewById(R.id.titleofsensor);
		TextView txtValue = (TextView) itemView.findViewById(R.id.valueofsensor);
		TextView txtUnit = (TextView) itemView.findViewById(R.id.unitofsensor);
		TextView txtTime = (TextView) itemView.findViewById(R.id.timeofsensor);

		// Locate the ImageView in drawer_list_item.xml
		ImageView imgIcon = (ImageView) itemView.findViewById(R.id.iconofsensor);

		Device device = mDevices.get(position);
		Facility facility = device.getFacility();
		Adapter adapter = mController.getAdapter(facility.getAdapterId());

		// UserSettings can be null when user is not logged in!
		SharedPreferences prefs = mController.getUserSettings();

		TimeHelper timeHelper = (prefs == null) ? null : new TimeHelper(prefs);
		UnitsHelper unitsHelper = (prefs == null) ? null : new UnitsHelper(prefs, mContext);

		// Set the results into TextViews
		txtTitle.setText(device.getName());

		if (unitsHelper != null) {
			txtValue.setText(unitsHelper.getStringValue(device.getValue()));
			txtUnit.setText(unitsHelper.getStringUnit(device.getValue()));
		}

		if (timeHelper != null) {
			txtTime.setText(String.format("%s %s", mContext.getString(R.string.last_update), timeHelper.formatLastUpdate(facility.getLastUpdate(), adapter)));
		}

		// Set title selected for animation if is text long
		txtTitle.setSelected(true);

		// Set the results into ImageView
		imgIcon.setImageResource(device.getIconResource());

		// Set layout with right background
		LinearLayout layout = (LinearLayout) itemView.findViewById(R.id.layoutofsensor);

		List<Device> facDevices = facility.getDevices();
		if (facDevices.size() == 0) {
			// This shouldn't happen
			return itemView;
		}

		boolean isFirst = facDevices.get(0).getId().equals(device.getId());
		boolean isLast = facDevices.get(facDevices.size() - 1).getId().equals(device.getId());
		boolean isSingle = isFirst && isLast;

		float scale = parent.getResources().getDisplayMetrics().density;

		int left, right, top, bottom, backgroundRes;
		left = right = (int) scale * MARGIN_LEFT_RIGHT;
		bottom = (int) scale * MARGIN_BOTTOM;

		if (isSingle) {
			// SOLO device from FACILITY
			backgroundRes = R.drawable.iha_item_solo_bg;
			top = (int) scale * MARGIN_TOP;
		} else if (isFirst) {
			// FIRST device from FACILITY
			backgroundRes = R.drawable.iha_item_first_bg;
			top = (int) scale * MARGIN_TOP;
		} else if (isLast) {
			// LAST device from FACILITY
			backgroundRes = R.drawable.iha_item_last_bg;
			top = (int) scale * MARGIN_TOP_M_L;
		} else {
			// MIDLE device from FACILITY
			backgroundRes = R.drawable.iha_item_midle_bg;
			top = (int) scale * MARGIN_TOP_M_L;
		}

		layout.setBackgroundResource(backgroundRes);
		((LayoutParams) layout.getLayoutParams()).setMargins(left, top, right, bottom);

		return itemView;
	}

}
