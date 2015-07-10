package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.activity.AddGateActivity;
import com.rehivetech.beeeon.gui.dialog.EditTextDialogFragment;

public class AddGateFragment extends TrackFragment {

	public OnAddGateListener mCallback;
	private FragmentActivity mActivity;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			// Get activity and controller
			mCallback = (AddGateActivity) getActivity();
			mActivity = (AddGateActivity) getActivity();
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
				setScanQrButtonEnabled(false);
				mCallback.showQrScanner();
			}
		});

		view.findViewById(R.id.add_gate_write_it_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// overlay dialog must popup here
				EditTextDialogFragment
						.createBuilder(mActivity, mActivity.getSupportFragmentManager())
						.setTitle(mActivity.getString(R.string.enter_text_dialog_title))
						.showKeyboard()
						.setEditTextInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL)
						.setPositiveButtonText(mActivity.getString(R.string.ok))
						.setNegativeButtonText(mActivity.getString(R.string.action_close))
						.show();
			}
		});

		return view;
	}

	public void setScanQrButtonEnabled(boolean enabled) {
		View view = getView();
		if (view == null)
			return;

		Button button = (Button) view.findViewById(R.id.add_gate_qr_button);
		if (button == null)
			return;

		if (enabled) {
			button.setEnabled(true);
			button.setText(R.string.addadapter_qr_button);
			button.setTextColor(getResources().getColor(R.color.white));
		} else {
			button.setEnabled(false);
			button.setText(R.string.addadapter_qr_button_loading);
			button.setTextColor(getResources().getColor(R.color.gray_light));
		}
	}

	public interface OnAddGateListener {
		void showQrScanner();

		void showEnterCodeDialog();
	}
}
