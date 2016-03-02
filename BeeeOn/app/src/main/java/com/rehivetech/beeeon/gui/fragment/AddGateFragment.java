package com.rehivetech.beeeon.gui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.avast.android.dialogs.core.BaseDialogFragment;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.dialog.EditTextDialog;
import com.rehivetech.beeeon.util.Utils;

public class AddGateFragment extends BaseApplicationFragment implements EditTextDialog.IPositiveButtonDialogListener {

	private static final int REQUEST_DIALOG_GATE_CODE = 1;
	private OnAddGateListener mCallback;

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

		try {
			// Get activity and controller
			mCallback = (OnAddGateListener) getActivity();
		} catch (ClassCastException e) {
			throw new ClassCastException(String.format("%s must implement OnAddGateListener", context.toString()));
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_gate_add, container, false);

		view.findViewById(R.id.gate_add_qr_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setScanQrButtonEnabled(false);
				mCallback.showQrScanner();
			}
		});

		view.findViewById(R.id.gate_add_write_it_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// overlay dialog must popup here
				EditTextDialog
						.createBuilder(mActivity, mActivity.getSupportFragmentManager())
						.setTitle(mActivity.getString(R.string.gate_add_dialog_title_enter_text))
						.showKeyboard()
						.setTargetFragment(AddGateFragment.this, REQUEST_DIALOG_GATE_CODE)
						.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL)
						.setPositiveButtonText(mActivity.getString(R.string.fragment_configuration_widget_dialog_btn_ok))
						.setNegativeButtonText(mActivity.getString(R.string.activity_fragment_btn_cancel))
						.show();
			}
		});

		return view;
	}

	public void setScanQrButtonEnabled(boolean enabled) {
		View view = getView();
		if (view == null)
			return;

		Button button = (Button) view.findViewById(R.id.gate_add_qr_button);
		if (button == null)
			return;

		if (enabled) {
			button.setEnabled(true);
			button.setText(R.string.gate_add_btn_qr);
			button.setTextColor(ContextCompat.getColor(mActivity, R.color.white));
		} else {
			button.setEnabled(false);
			button.setText(R.string.gate_add_btn_qr_loading);
			button.setTextColor(ContextCompat.getColor(mActivity, R.color.gray_light));
		}
	}

	@Override
	public void onPositiveButtonClicked(int requestCode, View view, BaseDialogFragment fragment) {
		TextInputLayout textInputLayout = (TextInputLayout) view.findViewById(R.id.dialog_edit_text_input_layout);
		if(!Utils.validateInput(mActivity, textInputLayout)){
			return;
		}

		EditText editText = textInputLayout.getEditText();
		if (editText != null) {
			mCallback.doRegisterGateTask(editText.getText().toString(), false);
		}
	}

	public interface OnAddGateListener {
		void showQrScanner();
		void doRegisterGateTask(String id, final boolean scanned);
	}
}
