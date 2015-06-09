package com.rehivetech.beeeon.gui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.rehivetech.beeeon.R;

/**
 * Created by david on 8.6.15.
 */
public class AddGateDialogFragment extends DialogFragment {
	private NoticeDialogListener mCallback;

	@Override
	public Dialog onCreateDialog (Bundle savedInstanceState) {
		// using the Builder classs to construct the dialog

		LayoutInflater inflater = getActivity().getLayoutInflater();
		final View view = inflater.inflate(R.layout.fragment_add_gate_overlay_dialog,null);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.adddapter_overlay_dialog_title);
		builder.setView(view);
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				EditText editText = (EditText) view.findViewById(R.id.add_gate_overlay_dialog_edit_text);
				String identifier = editText.getText().toString();
				if(identifier.length() == 0) {
					// FIXME: this condition is not working, always false, the same with identifier.isEmpty()
					Toast.makeText(getActivity(),R.string.toast_field_must_be_filled,Toast.LENGTH_LONG);
				} else {
					mCallback.onPositiveButtonClick(AddGateDialogFragment.this, identifier);
				}
			}
		});
		builder.setNegativeButton(R.string.action_close, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// TODO: delete the callback if not neccessary
				AddGateDialogFragment.this.onCancel(dialog);
			}
		});

		return builder.create();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Checks if the activity implements the interface
		try {
			mCallback = (NoticeDialogListener) getActivity();
		} catch (ClassCastException e) {
			throw new ClassCastException(String.format("%s must implement NoticeDialogListener",activity.toString()));
		}
	}

	public interface NoticeDialogListener {
		void onPositiveButtonClick (AddGateDialogFragment addGateDialogFragment, String id);
	}
}
