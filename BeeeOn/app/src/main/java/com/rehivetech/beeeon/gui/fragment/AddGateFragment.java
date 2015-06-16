package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.activity.AddGateActivity;

public class AddGateFragment extends TrackFragment {

	public OnAddGateListener mCallback;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			// Get activity and controller
			mCallback = (AddGateActivity) getActivity();
		} catch (ClassCastException e) {
			throw new ClassCastException(String.format("%s must implement OnAddGateListener", activity.toString()));
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_add_gate, container, false);

		view.findViewById(R.id.add_gate_qr_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mCallback.showQrScanner();
			}
		});

		view.findViewById(R.id.add_gate_write_it_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// overlay dialog must popup here
				mCallback.showEnterCodeDialog();
			}
		});

		return view;
	}

	public interface OnAddGateListener {
		void showQrScanner();

		void showEnterCodeDialog();
	}
}
