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
import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

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
	private MyTimeZone mSelectedZone = null;
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
			mGateName = savedInstanceState.getString(KEY_GATE_NAME);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mGateName = ((EditText) getView().findViewById(R.id.fragment_gate_edit_text)).getText().toString();
		outState.putString(KEY_GATE_NAME, mGateName);
	}

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
		Gate gate = Controller.getInstance(mActivity).getGatesModel().getGate(mGateId);

		//super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_gate_edit, container, false);

		((EditText) view.findViewById(R.id.fragment_gate_edit_text)).setText((mGateName == null) ? (gate.getName()) : mGateName);
		((TextView) view.findViewById(R.id.fragment_gate_edit_gate_id)).setText(gate.getId());

		Spinner spinner = (Spinner) view.findViewById(R.id.fragment_gate_edit_spinner);
		List<MyTimeZone> timeZones = new ArrayList<>();
		for (String id : DateTimeZone.getAvailableIDs()) {
			int millis = DateTimeZone.forID(id).getOffset(null);
			Period period = Duration.millis(millis).toPeriod();
			PeriodFormatter hm = new PeriodFormatterBuilder()
					.printZeroAlways()
					.minimumPrintedDigits(2) // gives the '01'
					.appendHours()
					.appendSeparator(":")
					.appendMinutes()
					.toFormatter();
			String result = hm.print(period);
			String name = String.format("GMT%s%s %s", millis >= 0 ? "+" : "", result, DateTimeZone.forID(id).toTimeZone().getDisplayName());
			timeZones.add(new MyTimeZone(name, millis / (1000 * 60)));
		}
		ArrayAdapter<MyTimeZone> adapter = new ArrayAdapter<>(mActivity, android.R.layout.simple_spinner_item, timeZones);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				mSelectedZone = ((MyTimeZone) parent.getItemAtPosition(position));
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

	public MyTimeZone getNewGateZone() {
		return mSelectedZone;
	}

	public class MyTimeZone {
		public String name;
		public int offsetInMinutes;

		public MyTimeZone(String name, int offsetInMinutes) {
			this.name = name;
			this.offsetInMinutes = offsetInMinutes;
		}

		@Override
		public String toString() {
			return name;
		}
	}
}
