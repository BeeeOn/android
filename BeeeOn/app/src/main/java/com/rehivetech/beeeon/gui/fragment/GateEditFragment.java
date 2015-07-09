package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.activity.GateEditActivity;
import com.rehivetech.beeeon.household.gate.Gate;

import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by david on 17.6.15.
 */
public class GateEditFragment extends Fragment {
	private static final String KEY_GATE_ID = "Gate_Id";
	private static final String KEY_GATE_NAME = "Gate_name";
	private static final String KEY_GATE_ZONE = "Gate_zone";
	private GateEditActivity mActivity;
	private String mSelectedZone = null;
	private String mGateId;
	private String mGateName = null;

	public static GateEditFragment newInstance(String gateId) {
		GateEditFragment gateEditFragment = new GateEditFragment();
		Bundle args = new Bundle();
		args.putString(KEY_GATE_ID, gateId);
		gateEditFragment.setArguments(args);
		return gateEditFragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mGateId = getArguments().getString(KEY_GATE_ID);
		if (savedInstanceState != null) {
			mSelectedZone = savedInstanceState.getString(KEY_GATE_ZONE);
			mGateName = savedInstanceState.getString(KEY_GATE_NAME);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mGateName = ((EditText) getView().findViewById(R.id.fragment_gate_edit_text)).getText().toString();
		outState.putString(KEY_GATE_NAME, mGateName);
		outState.putString(KEY_GATE_ZONE, mSelectedZone);
	}

	/*
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			if (savedInstanceState != null) {
				mSelectedZone = savedInstanceState.getString(KEY_GATE_ZONE);
				mGateName = savedInstanceState.getString(KEY_GATE_NAME);
				((EditText) getView().findViewById(R.id.fragment_gate_edit_text)).setText(mGateName);
			}
		}
	*/
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mActivity = (GateEditActivity) getActivity();
		} catch (ClassCastException e) {
			throw new ClassCastException(String.format("%s must be subclass of GateEditActivity", activity.toString()));
		}
	}

	public static List<String> getMainTimeZones() {
		String[] TZ = TimeZone.getAvailableIDs();
		ArrayList<String> TZ1 = new ArrayList<>();
		for (int i = 0; i < TZ.length; i++) {
			if (!(TZ1.contains(TimeZone.getTimeZone(TZ[i]).getDisplayName()))) {
				TZ1.add(TimeZone.getTimeZone(TZ[i]).getDisplayName());
			}
		}
		return TZ1;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Gate gate = Controller.getInstance(mActivity).getGatesModel().getGate(mGateId);

		//super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_gate_edit, container, false);

		((EditText) view.findViewById(R.id.fragment_gate_edit_text)).setText((mGateName == null) ? (gate.getName()) : mGateName);
		((TextView) view.findViewById(R.id.fragment_gate_edit_gate_id)).setText(gate.getId());

		Spinner spinner = (Spinner) view.findViewById(R.id.fragment_gate_edit_spinner);
		String[] zones = {"UTC-12", "UTC-11", "UTC-10", "UTC-9.30", "UTC-9", "UTC-8", "UTC-7", "UTC-6", "UTC-5", "UTC-4.30", "UTC-4", "UTC-3.30", "UTC-3", "UTC-2",
				"UTC-1", "UTC", "UTC+1", "UTC+2", "UTC+3", "UTC+3.30", "UTC+4", "UTC+4.30", "UTC+5", "UTC+5.30", "UTC+5.45", "UTC+6", "UTC+6.30", "UTC+7", "UTC+8",
				"UTC+8.45", "UTC+9", "UTC+9.30", "UTC+10", "UTC+10.30", "UTC+11", "UTC+11.30", "UTC+12", "UTC+12.45", "UTC+13", "UTC+14"};
		Float[] offsetZones = {-12f, -11f, -10f, -9.3f, -9f, -8f, -7f, -6f, -5f, -4.3f, -4f, -3.3f, -3f, -2f, -1f, 0f, 1f, 2f, 3f, 3.3f, 4f, 4.3f, 5f, 5.30f, 5.45f, 6f, 6.30f, 7f, 8f, 8.45f, 9f, 9.30f, 10f, 10.30f, 11f, 11.30f, 12f, 12.45f, 13f, 14f};
		List<MyTimeZone> list = new ArrayList<>();
		for (int i = 0; i < zones.length; i++) {
			list.add(new MyTimeZone(zones[i], offsetZones[i]));
		}

		List<String> xList = new ArrayList<>();
		for (int i = 0; i < 24; i++) {
			xList.add(DateTimeZone.forOffsetHours(i).toString());
		}
		List<String> mList = new ArrayList<>();
		for (String id : DateTimeZone.getAvailableIDs()) {
			DateTimeZone zone = DateTimeZone.forID(id);
			zone.toTimeZone().getDisplayName();

			mList.add(id);
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<>(mActivity, android.R.layout.simple_spinner_item, xList);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				mSelectedZone = parent.getItemAtPosition(position).toString();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// nothing to do
			}
		});
		return view;
	}

	public String getNewGateName() {
		return ((EditText) getView().findViewById(R.id.fragment_gate_edit_text)).getText().toString();
	}

	public String getNewGateZone() {
		return mSelectedZone;
	}

	public class MyTimeZone {
		public String name;
		public float offset;

		public MyTimeZone(String name, float offset) {
			this.name = name;
			this.offset = offset;
		}

		@Override
		public String toString() {
			return name;
		}
	}
}
