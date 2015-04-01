package com.rehivetech.beeeon.arrayadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.rehivetech.beeeon.activity.spinnerItem.HeaderSpinnerItem;
import com.rehivetech.beeeon.activity.spinnerItem.SpinnerItem;


import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;


/**
 * Class for spinner of multi objects
 */
public class SpinnerMultiAdapter extends BaseAdapter{
	private static final String TAG = SpinnerMultiAdapter.class.getSimpleName();

	private final Context mContext;
	private LayoutInflater mInflater;
	private List<SpinnerItem> mSpinnerItem;
	private TreeSet<Integer> mHeaderSet = new TreeSet<Integer>();


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
		mHeaderSet.add(mSpinnerItem.size() -1);
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


	public View getDropDownView(int position, View convertView, ViewGroup parent){
		// TODO optimalizovat s pouzitim recaklovanych convertview?
		convertView = mInflater.inflate(mSpinnerItem.get(position).getDropDownLayout(), parent, false);
		mSpinnerItem.get(position).setDropDownView(convertView);
		return convertView;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO recycle convertView
		convertView = mInflater.inflate(mSpinnerItem.get(position).getLayout(), parent, false);
		mSpinnerItem.get(position).setView(convertView);
		return convertView;
	}

	/**
	 * Because we can have headers, this gets item position without headers
	 * @param position
	 * @return real item position
	 */
	public int getRealPosition(int position){
		int tempPos = position;
		for(int i = 0; i <= position; i++){
			if(mHeaderSet.contains(i)) tempPos++;
		}

		return tempPos;
	}
}
