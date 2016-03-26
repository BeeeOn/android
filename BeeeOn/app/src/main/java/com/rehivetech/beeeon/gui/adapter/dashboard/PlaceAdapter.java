package com.rehivetech.beeeon.gui.adapter.dashboard;

import android.app.Activity;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.activity.BaseApplicationActivity;
import com.rehivetech.beeeon.model.place.Place;
import com.rehivetech.beeeon.network.provider.PlacesProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by martin on 26.3.16.
 */
public class PlaceAdapter extends ArrayAdapter<Place> {
	private static final String TAG = PlaceAdapter.class.getSimpleName();

	private List<Place> mPlaces = new ArrayList<>();

	private PlacesProvider mProvider;

	public PlaceAdapter(Activity context) {
		super(context, android.R.layout.simple_dropdown_item_1line);
		mProvider = new PlacesProvider(context);
	}

	@Override
	public int getCount() {
		return mPlaces.size();
	}

	@Override
	public Place getItem(int position) {
		return mPlaces.get(position);
	}

	@Override
	public Filter getFilter() {
		return new Filter() {
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults results = new FilterResults();

				try {
					mPlaces.clear();
					if (constraint != null) {

						List<Place> places = mProvider.getPlaces(constraint.toString());
						if (places != null) {
							results.values = places;
							results.count = places.size();
							mPlaces.addAll(places);
						}
					}
				} catch (IOException e) {
					((BaseApplicationActivity) getContext()).runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(getContext(),getContext().getString(R.string.ClientError___XML), Toast.LENGTH_SHORT).show();
						}
					});
				}

				return results;
			}

			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				if (results != null && results.count != 0) {
					notifyDataSetChanged();
				} else {
					notifyDataSetInvalidated();
				}
			}

			@Override
			public CharSequence convertResultToString(Object resultValue) {
				return ((Place) resultValue).getAddress();
			}
		};
	}
}
