package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.activity.GateEditActivity;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.gate.GateInfo;
import com.rehivetech.beeeon.util.TimezoneWrapper;

import java.util.List;

/**
 * Created by david on 17.6.15.
 */
public class GateEditFragment extends Fragment {
	private static final String EXTRA_GATE_ID = "gate_id";

	private static final String KEY_GATE_NAME = "gate_name";
	private static final String KEY_GATE_TIMEZONE_INDEX = "gate_timezone_index";

	private GateEditActivity mActivity;

	private String mGateId;
	private List<TimezoneWrapper> mTimezones = TimezoneWrapper.getTimezones();

	private EditText mGateNameEditText;
	private Spinner mTimezoneSpinner;
	private TextView mGateIdTextView;

	public static GateEditFragment newInstance(String gateId) {
		GateEditFragment gateEditFragment = new GateEditFragment();
		Bundle args = new Bundle();
		args.putString(EXTRA_GATE_ID, gateId);
		gateEditFragment.setArguments(args);
		return gateEditFragment;
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

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mGateId = getArguments().getString(EXTRA_GATE_ID);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_gate_edit, container, false);

		mGateNameEditText = ((EditText) view.findViewById(R.id.fragment_gate_edit_text));
		mGateIdTextView = ((TextView) view.findViewById(R.id.fragment_gate_edit_gate_id));
		mTimezoneSpinner = ((Spinner) view.findViewById(R.id.fragment_gate_edit_spinner));

		ArrayAdapter<TimezoneWrapper> adapter = new ArrayAdapter<>(mActivity, R.layout.update_gate_spinner_item, mTimezones);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mTimezoneSpinner.setAdapter(adapter);

		return view;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		fillData(savedInstanceState);
	}

	public void fillData(Bundle savedInstanceState) {
		GateInfo gate = Controller.getInstance(mActivity).getGatesModel().getGateInfo(mGateId);
		if (gate == null)
			return;

		if (savedInstanceState != null) {
			mGateNameEditText.setText(savedInstanceState.getString(KEY_GATE_NAME));
			mGateIdTextView.setText(mGateId);
			mTimezoneSpinner.setSelection(savedInstanceState.getInt(KEY_GATE_TIMEZONE_INDEX));
		} else {
			mGateNameEditText.setText(gate.getName());
			mGateIdTextView.setText(gate.getId());

			int offsetInMillis = gate.getUtcOffset() * 60 * 1000;
			int index = mTimezones.indexOf(TimezoneWrapper.getZoneByOffset(offsetInMillis));
			mTimezoneSpinner.setSelection(index != -1 ? index : 0);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		String gateName = mGateNameEditText.getText().toString();
		outState.putString(KEY_GATE_NAME, gateName);

		outState.putInt(KEY_GATE_TIMEZONE_INDEX, mTimezoneSpinner.getSelectedItemPosition());
	}

	public Gate getNewGate() {
		View view = getView();
		if (view == null)
			return null;

		Gate gate = new Gate();
		gate.setId(mGateId);

		String newGateName = ((TextView) view.findViewById(R.id.fragment_gate_edit_text)).getText().toString();
		gate.setName(newGateName);

		Spinner spinner = (Spinner) getView().findViewById(R.id.fragment_gate_edit_spinner);
		TimezoneWrapper timezone = (TimezoneWrapper) spinner.getSelectedItem();
		int offsetInMinutes = timezone.offsetInMillis / (1000 * 60);
		gate.setUtcOffset(offsetInMinutes);

		return gate;
	}

}
