package com.rehivetech.beeeon.gui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;

import com.rehivetech.beeeon.gui.listItem.IListItem;
import com.rehivetech.beeeon.gui.listItem.SensorListItem;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.util.Log;

import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Class for list of sensors
 */
public class SenListAdapter extends BaseAdapter implements StickyListHeadersAdapter, SectionIndexer {
	private static final String TAG = SenListAdapter.class.getSimpleName();

	private final Context mContext;

	private LayoutInflater mInflater;

	private List<IListItem> mListItem;

	private List<IListItem> mListHeader;

	private List<Integer> mListHeaderPos;

	public SenListAdapter(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mListHeader = new ArrayList<IListItem>();
		mListItem = new ArrayList<IListItem>();
		mListHeaderPos = new ArrayList<Integer>();
	}

	public void addItem(IListItem item) {
		mListItem.add(item);
	}

	public void addHeader(IListItem item) {
		mListHeader.add(item);
		mListHeaderPos.add(mListItem.size());
	}

	@Override
	public int getCount() {
		return mListItem.size();
	}

	@Override
	public IListItem getItem(int position) {
		if (position < 0) {
			position = 0;
		} else if (position > mListItem.size() - 1) {
			position = mListItem.size() - 1;
		}
		return mListItem.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = mInflater.inflate(mListItem.get(position).getLayout(), parent, false);
		mListItem.get(position).setView(convertView);
		return convertView;
	}

	public Module getModule(int position) {
		return ((SensorListItem) mListItem.get(position)).getModule();
	}

	@Override
	public long getHeaderId(int position) {
		return mListHeader.get(getSectionForPosition(position)).hashCode();
	}

	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		convertView = mInflater.inflate(mListHeader.get(getSectionForPosition(position)).getLayout(), parent, false);
		mListHeader.get(getSectionForPosition(position)).setView(convertView);
		return convertView;
	}

	@Override
	public Object[] getSections() {
		return mListHeader.toArray(new IListItem[mListHeader.size()]);
	}

	@Override
	public int getPositionForSection(int section) {
		if (mListHeaderPos.size() == 0) {
			return 0;
		}

		if (section >= mListHeaderPos.size()) {
			section = mListHeaderPos.size() - 1;
		} else if (section < 0) {
			section = 0;
		}
		return mListHeaderPos.get(section);
	}

	@Override
	public int getSectionForPosition(int position) {
		for (int i = 0; i < mListHeaderPos.size(); i++) {
			if (position < mListHeaderPos.get(i)) {
				return i - 1;
			}
		}
		return mListHeaderPos.size() - 1;
	}

	public void log() {
		Log.i(TAG, "ITEMS:");
		for (int i = 0; i < mListItem.size(); i++) {
			Log.i(TAG, "  " + String.valueOf(i) + ": " + mListItem.get(i).getId());
		}

		Log.i(TAG, "###########################");

		Log.i(TAG, "HEADERS:");
		for (int i = 0; i < mListHeader.size(); i++) {
			Log.i(TAG, "  " + String.valueOf(i) + ": " + mListHeader.get(i).getId());
		}

		Log.i(TAG, "###########################");

		Log.i(TAG, "POSITIONS:");
		for (Integer actItem : mListHeaderPos) {
			Log.i(TAG, "  " + String.valueOf(actItem));
		}
	}
}