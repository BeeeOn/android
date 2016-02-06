package com.rehivetech.beeeon.gui.dialog;

import android.annotation.SuppressLint;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.avast.android.dialogs.fragment.SimpleDialogFragment;
import com.rehivetech.beeeon.R;

/**
 * @author Martin Matejcik
 * @author Tomas Mlynaric
 */
public class EnterPasswordDialog extends SimpleDialogFragment {

	private static final String TAG = "password_dialog";

	public static void show(FragmentActivity activity, Fragment fragment, int requestCode) {
		EnterPasswordDialog dialog = new EnterPasswordDialog();
		dialog.setTargetFragment(fragment, requestCode);
		dialog.show(activity.getSupportFragmentManager(), TAG);
	}

	@SuppressLint("InflateParams")
	@Override
	protected Builder build(Builder builder) {

		builder.setTitle(R.string.device_search_enter_password);

		final View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_device_enter_password, null);

		final EditText editText = (EditText) view.findViewById(R.id.dialog_edit_text);
		AppCompatCheckBox checkBox = (AppCompatCheckBox) view.findViewById(R.id.dialog_enter_password_checkbox);
		checkBox.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (((AppCompatCheckBox) v).isChecked()) {
					editText.setEnabled(false);

				} else {
					editText.setEnabled(true);
				}
			}
		});
		builder.setView(view);

		builder.setPositiveButton(R.string.device_search_enter_password_ok, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				for (PasswordDialogListener listener : EnterPasswordDialog.this.getDialogListeners(PasswordDialogListener.class)) {
					listener.onPositiveButtonClicked(mRequestCode, view, EnterPasswordDialog.this);
				}
			}
		});
		return builder;
	}

	public interface PasswordDialogListener {
		void onPositiveButtonClicked(int requestCode, View view, EnterPasswordDialog dialog);
	}
}
