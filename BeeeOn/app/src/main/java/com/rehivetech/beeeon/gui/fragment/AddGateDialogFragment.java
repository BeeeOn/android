package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.rehivetech.beeeon.R;

/**
 * Created by david on 8.6.15.
 */
public class AddGateDialogFragment extends DialogFragment {
	private NoticeDialogListener mCallback;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// using the Builder classs to construct the dialog

		LayoutInflater inflater = getActivity().getLayoutInflater();
		final View view = inflater.inflate(R.layout.fragment_add_gate_overlay_dialog, null);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.adddapter_overlay_dialog_title);
		builder.setView(view);
		builder.setPositiveButton(R.string.ok, null);
		builder.setNegativeButton(R.string.action_close, null);
		
		final AlertDialog dialog = builder.create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(final DialogInterface dialog) {
				Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
				positiveButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(final View v) {
						EditText editText = (EditText) view.findViewById(R.id.add_gate_overlay_dialog_edit_text);
						String identifier = editText.getText().toString();
						if (identifier.isEmpty()) {
							// when the editText is empty...
							Toast.makeText(getActivity(), R.string.toast_field_must_be_filled, Toast.LENGTH_LONG).show();
						} else {
							mCallback.onPositiveButtonClick(AddGateDialogFragment.this, identifier);
						}
					}
				});
				Button negativeButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
				negativeButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dismiss();
					}
				});
			}
		});

		return dialog;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Checks if the activity implements the interface
		try {
			mCallback = (NoticeDialogListener) getActivity();
		} catch (ClassCastException e) {
			throw new ClassCastException(String.format("%s must implement NoticeDialogListener", activity.toString()));
		}
	}

	public interface NoticeDialogListener {
		void onPositiveButtonClick(AddGateDialogFragment addGateDialogFragment, String id);
	}
}
