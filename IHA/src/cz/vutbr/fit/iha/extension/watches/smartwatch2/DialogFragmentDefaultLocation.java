package cz.vutbr.fit.iha.extension.watches.smartwatch2;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockDialogFragment;

import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.adapter.location.Location;
import cz.vutbr.fit.iha.controller.Controller;

public class DialogFragmentDefaultLocation extends SherlockDialogFragment {

	private ListView listView;

	private final static int POS_NONE = 0;

	List<String> mLocationList;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater factory = LayoutInflater.from(getSherlockActivity());
		final View view = factory.inflate(
				R.layout.sw2_fragment_default_location, null);

		listView = (ListView) view.findViewById(R.id.number_email_list);

		listView.setBackgroundColor(Color.WHITE);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(getSherlockActivity());
				String location = (pos == POS_NONE) ? null : mLocationList
						.get(pos);
				prefs.edit()
						.putString(Constants.SW2_PREF_DEF_LOCATION, location)
						.commit();
				dismiss();
			}
		});

		Controller controller = Controller.getInstance(getSherlockActivity());

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getSherlockActivity());
		String adapterId = prefs
				.getString(Constants.SW2_PREF_DEF_ADAPTER, null);

		if (adapterId == null) {
			throw new NullPointerException();
		}

		Adapter adapter = controller.getAdapter(adapterId, false);
		if (adapter == null) {
			throw new NullPointerException();
		}

		mLocationList = new ArrayList<String>();
		mLocationList.add(getString(R.string.none));
		for (Location location : adapter.getLocations())
			mLocationList.add(location.getName());

		listView.setAdapter(new ArrayAdapter<String>(getSherlockActivity(),
				android.R.layout.simple_list_item_1, mLocationList
						.toArray(new String[mLocationList.size()])));

		return new AlertDialog.Builder(getSherlockActivity()).setView(view)
				.setTitle(R.string.choose_default_location).create();
	}

}
