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
import android.widget.TextView;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.adapter.Adapter;
import com.rehivetech.beeeon.adapter.device.Device;
import com.rehivetech.beeeon.adapter.device.Facility;
import com.rehivetech.beeeon.adapter.location.Location;
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
	private boolean mShowHeader;
	private OnClickListener mListener;
	private List<Device> mDevices;

	private final Controller mController;

	public SensorListAdapter(Context context, List<Device> devices, boolean header) {
		mContext = context;
		mController = Controller.getInstance(context.getApplicationContext());
		mDevices = devices;
        mShowHeader = header;
	}

	@Override
	public int getCount() {
		return mDevices.size();
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
		return addItem(position, convertView, parent);
	}

	private View addItem(int position, View convertView, ViewGroup parent) {
		inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View itemView = inflater.inflate(R.layout.sensor_listview_item, parent, false);

		// Locate the TextViews in drawer_list_item.xml
		TextView txtTitle = (TextView) itemView.findViewById(R.id.titleofsensor);
		TextView txtValue = (TextView) itemView.findViewById(R.id.valueofsensor);
		TextView txtUnit = (TextView) itemView.findViewById(R.id.unitofsensor);
		TextView txtTime = (TextView) itemView.findViewById(R.id.timeofsensor);

        // Separators
        View sepMidle = itemView.findViewById(R.id.sensor_sep_midle);

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
        boolean isFirstInLoc = mController.getFacilitiesByLocation(adapter.getId(),facility.getLocationId()).get(0).equals(facility);

        // IF is this facility first facility in location
        if(isFirstInLoc && isFirst) {
            itemView.findViewById(R.id.sensor_header).setVisibility(View.VISIBLE);
            Location loc = mController.getLocation(adapter.getId(), facility.getLocationId());
            if(loc == null) {
                // This shouldn't happen
                return itemView;
            }
            ((TextView)itemView.findViewById(R.id.sensor_header_text)).setText(loc.getName());
        }

		if (isSingle && !isFirstInLoc) {
			// SOLO device from FACILITY
		} else if(isSingle && isFirstInLoc) {
            // SOLO device from FACILITY
        } else if (isFirst) {
			// FIRST device from FACILITY
            sepMidle.setVisibility(View.GONE);
		} else if (isLast) {
			// LAST device from FACILITY
		} else {
			// MIDLE device from FACILITY
            sepMidle.setVisibility(View.GONE);
		}

        // IF is last in list
        if(position == getCount()-1) {
            itemView.findViewById(R.id.sensor_last_item).setVisibility(View.VISIBLE);
        }

		return itemView;
	}

}
