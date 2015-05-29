package com.rehivetech.beeeon.gui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;

import com.rehivetech.beeeon.gui.menuItem.IMenuItem;
import com.rehivetech.beeeon.util.Log;

import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Class for location left drawer menu
 */
public class MenuListAdapter extends BaseAdapter implements StickyListHeadersAdapter, SectionIndexer {
	private static final String TAG = MenuListAdapter.class.getSimpleName();

	private final Context mContext;

	private LayoutInflater mInflater;

	private List<IMenuItem> mListItem;

	private List<IMenuItem> mListHeader;

	private List<Integer> mListHeaderPos;

	// private List<IMenuItem> mListAllItems;

	public MenuListAdapter(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mListHeader = new ArrayList<IMenuItem>();
		mListItem = new ArrayList<IMenuItem>();
		mListHeaderPos = new ArrayList<Integer>();
		// mListAllItems = new ArrayList<IMenuItem>();
	}

	public void addItem(IMenuItem item) {
		mListItem.add(item);
		// mListAllItems.add(item);
	}

	public void addHeader(IMenuItem item) {
		mListHeader.add(item);
		// mListAllItems.add(item);
		mListHeaderPos.add(mListItem.size());
	}

	@Override
	public int getCount() {
		return mListItem.size();
	}

	@Override
	public Object getItem(int position) {
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
		return mListHeader.toArray(new IMenuItem[mListHeader.size()]);
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

		// Log.i(TAG, "ALL:");
		// for (int i = 0; i < mListAllItems.size(); i++) {
		// Log.i(TAG,"  " + String.valueOf(i) + ": "+mListAllItems.get(i).getId());
		// }

		Log.i(TAG, "###########################");

		Log.i(TAG, "POSITIONS:");
		for (Integer actItem : mListHeaderPos) {
			Log.i(TAG, "  " + String.valueOf(actItem));
		}
	}
}
