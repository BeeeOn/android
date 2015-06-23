package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.activity.GateDetailActivity;
import com.rehivetech.beeeon.household.gate.Gate;

/**
 * Created by david on 23.6.15.
 */
public class GateDetailFragment extends Fragment {
	private String mGateId;
	private static final String GATE_ID = "GATE_ID";
	private GateDetailActivity mActivity;

	public static GateDetailFragment newInstance(String gateId) {
		GateDetailFragment gateDetailFragment = new GateDetailFragment();
		Bundle args = new Bundle();
		args.putString(GATE_ID, gateId);
		gateDetailFragment.setArguments(args);
		return gateDetailFragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mActivity = (GateDetailActivity) getActivity();
		} catch (ClassCastException e) {
			throw new ClassCastException(String.format("%s must be created by GateDetailActivity", activity.toString()));
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mGateId = getArguments().getString(GATE_ID);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_gate_detail, container, false);
		Gate gate =  Controller.getInstance(mActivity).getGatesModel().getGate(mGateId);

		return view;
	}
}
