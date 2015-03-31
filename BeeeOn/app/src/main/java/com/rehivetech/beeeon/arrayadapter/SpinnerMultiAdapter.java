package com.rehivetech.beeeon.arrayadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.activity.listItem.ListItem;
import com.rehivetech.beeeon.activity.listItem.SensorListItem;
import com.rehivetech.beeeon.activity.spinnerItem.DeviceSpinnerItem;
import com.rehivetech.beeeon.activity.spinnerItem.HeaderSpinnerItem;
import com.rehivetech.beeeon.activity.spinnerItem.SpinnerItem;
import com.rehivetech.beeeon.adapter.device.Device;
import com.rehivetech.beeeon.util.Log;

import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Class for spinner of multi objects
 */
public class SpinnerMultiAdapter extends BaseAdapter{
	private static final String TAG = SpinnerMultiAdapter.class.getSimpleName();

	// when used with header first, this should be first item
	public static final int FIRST_ITEM_POS = 1; 	// TODO change it dynamically - based on addHeader

	private final Context mContext;
	private LayoutInflater mInflater;
	private List<SpinnerItem> mSpinnerItem;

	public SpinnerMultiAdapter(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mSpinnerItem = new ArrayList<>();
	}

	public void addItem(SpinnerItem item) {
		mSpinnerItem.add(item);
	}

	public void addHeader(String name) {
		addItem(new HeaderSpinnerItem(name));
	}
	
	@Override
	public int getCount() {
		return mSpinnerItem.size();
	}

	@Override
	public SpinnerItem getItem(int position) {
		if (position < 0) {
			position = 0;
		} else if (position > mSpinnerItem.size() - 1) {
			position = mSpinnerItem.size() - 1;
		}
		return mSpinnerItem.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO recycle convertView
		convertView = mInflater.inflate(mSpinnerItem.get(position).getLayout(), parent, false);
		mSpinnerItem.get(position).setView(convertView);
		return convertView;
	}
}
