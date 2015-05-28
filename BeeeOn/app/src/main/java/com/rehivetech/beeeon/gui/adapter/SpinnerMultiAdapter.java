package com.rehivetech.beeeon.gui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.rehivetech.beeeon.gui.spinnerItem.HeaderSpinnerItem;
import com.rehivetech.beeeon.gui.spinnerItem.SpinnerItem;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;


/**
 * Class for spinner of multi objects
 */
public class SpinnerMultiAdapter extends BaseAdapter {
	private static final String TAG = SpinnerMultiAdapter.class.getSimpleName();

	private final Context mContext;
	private LayoutInflater mInflater;
	private List<SpinnerItem> mSpinnerItems;
	private TreeSet<Integer> mHeaderSet = new TreeSet<Integer>();


	public SpinnerMultiAdapter(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mSpinnerItems = new ArrayList<>();
	}

	public void addItem(SpinnerItem item) {
		mSpinnerItems.add(item);
	}

	public void addHeader(String name) {
		addItem(new HeaderSpinnerItem(name));
		mHeaderSet.add(mSpinnerItems.size() - 1);
	}

	@Override
	public int getCount() {
		return mSpinnerItems.size();
	}

	@Override
	public SpinnerItem getItem(int position) {
		if (position < 0) {
			position = 0;
		} else if (position > mSpinnerItems.size() - 1) {
			position = mSpinnerItems.size() - 1;
		}
		return mSpinnerItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}


	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		// TODO optimalizovat s pouzitim recaklovanych convertview?
		convertView = mInflater.inflate(mSpinnerItems.get(position).getDropDownLayout(), parent, false);
		mSpinnerItems.get(position).setDropDownView(convertView);
		return convertView;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO recycle convertView
		convertView = mInflater.inflate(mSpinnerItems.get(position).getLayout(), parent, false);
		mSpinnerItems.get(position).setView(convertView);
		return convertView;
	}

	/**
	 * Because we can have headers, this gets item position without headers
	 *
	 * @param position
	 * @return real item position
	 */
	public int getRealPosition(int position, SpinnerItem.SpinnerItemType itemType) {
		int tempPos = 0, resultPos;
		for (resultPos = 0; resultPos < mSpinnerItems.size(); resultPos++) {
			SpinnerItem.SpinnerItemType tempType = getItem(resultPos).getType();

			// if type (is ANY and is header) or does not match
			if (itemType == null && tempType == SpinnerItem.SpinnerItemType.HEADER)
				continue;
			else if (itemType != null && tempType != itemType)
				continue;

			// if we found, end cycling
			if (tempPos == position) {
				break;
			}

			tempPos++;
		}

		return (resultPos >= mSpinnerItems.size()) ? 0 : resultPos;
	}

	/**
	 * Overloaded method for getting any item except header
	 *
	 * @param position
	 * @return
	 */
	public int getRealPosition(int position) {
		return getRealPosition(position, null);
	}
}
