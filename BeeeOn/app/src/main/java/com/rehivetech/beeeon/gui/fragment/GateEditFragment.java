package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.activity.GateEditActivity;

/**
 * Created by david on 17.6.15.
 */
public class GateEditFragment extends Fragment {
	private GateEditActivity mActivity;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mActivity = (GateEditActivity) getActivity();
		} catch (ClassCastException e) {
			throw new ClassCastException(String.format("%s must be subclass of GateEditActivity", activity.toString()));
		}
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_gate_edit, container, false);
		Spinner spinner = (Spinner) view.findViewById(R.id.fragment_gate_edit_spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mActivity,R.array.time_zones_array,android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		return view;
	}
}
