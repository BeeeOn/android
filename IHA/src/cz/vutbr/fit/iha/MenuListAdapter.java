package cz.vutbr.fit.iha;

import java.util.ArrayList;
import java.util.List;

import cz.vutbr.fit.iha.activity.menuItem.MenuItem;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;

/**
 * Class for location left drawer menu
 */
public class MenuListAdapter extends BaseAdapter implements
		StickyListHeadersAdapter, SectionIndexer {
	private final Context mContext;

	private LayoutInflater mInflater;

	private List<MenuItem> mListItem;

	private List<MenuItem> mListHeader;

	private List<Integer> mListHeaderPos;
	
//	private List<MenuItem> mListAllItems;

	public MenuListAdapter(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mListHeader = new ArrayList<MenuItem>();
		mListItem = new ArrayList<MenuItem>();
		mListHeaderPos = new ArrayList<Integer>();
//		mListAllItems = new ArrayList<MenuItem>();
	}

	public void addItem(MenuItem item) {
		mListItem.add(item);
//		mListAllItems.add(item);
	}

	public void addHeader(MenuItem item) {
		mListHeader.add(item);
//		mListAllItems.add(item);
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
		} else if (position > mListItem.size()-1) {
			position = mListItem.size()-1;
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
		return mListHeader.toArray(new MenuItem[mListHeader.size()]);
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
		Log.i("MENU","ITEMS:");
		for (int i = 0; i < mListItem.size(); i++) {
			Log.i("MENU","  " + String.valueOf(i) + ": "+mListItem.get(i).getId());
		}
		
		Log.i("MENU","###########################");
		
		Log.i("MENU", "HEADERS:");
		for (int i = 0; i < mListHeader.size(); i++) {
			Log.i("MENU","  " + String.valueOf(i) + ": "+mListHeader.get(i).getId());
		}
		
		Log.i("MENU","###########################");
		
//		Log.i("MENU", "ALL:");
//		for (int i = 0; i < mListAllItems.size(); i++) {
//			Log.i("MENU","  " + String.valueOf(i) + ": "+mListAllItems.get(i).getId());
//		}
		
		Log.i("MENU","###########################");
		
		Log.i("MENU","POSITIONS:");
		for (Integer actItem:mListHeaderPos) {
			Log.i("MENU", "  " + String.valueOf(actItem));
		}
	}
}
