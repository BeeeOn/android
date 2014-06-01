package cz.vutbr.fit.intelligenthomeanywhere.extension.watches.smartwatch2;


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

import cz.vutbr.fit.intelligenthomeanywhere.Constants;
import cz.vutbr.fit.intelligenthomeanywhere.R;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.Adapter;

public class DialogFragmentDefaultAdapter extends SherlockDialogFragment {

	private ListView listView;

	private final static int POS_NONE = 0;

	List<String> mAdapterStringList;
	List<Adapter> mAdapterList;

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
				String adapterId = (pos == POS_NONE) ? null : mAdapterList.get(
						pos - 1).getId();
				prefs.edit()
						.putString(Constants.SW2_PREF_DEF_ADAPTER, adapterId)
						.commit();
				dismiss();
			}
		});

		mAdapterStringList = new ArrayList<String>();
		mAdapterStringList.add(getString(R.string.none));
		for (Adapter adapter : mAdapterList) {
			mAdapterStringList.add(adapter.getName());
		}

		listView.setAdapter(new ArrayAdapter<String>(getSherlockActivity(),
				android.R.layout.simple_list_item_1, mAdapterStringList
						.toArray(new String[mAdapterStringList.size()])));

		return new AlertDialog.Builder(getSherlockActivity()).setView(view)
				.setTitle(R.string.choose_default_adapter).create();
	}

}
