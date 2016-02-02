package com.rehivetech.beeeon.gui.dialog;

import android.annotation.SuppressLint;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.avast.android.dialogs.fragment.SimpleDialogFragment;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.util.Utils;

import java.util.List;

/**
 * Created by martin on 1.2.16.
 */
public class PasswordDialog extends SimpleDialogFragment {

	private static final String TAG = "password_dialog";

	public static void show(FragmentActivity activity, Fragment fragment, int requestCode) {
		PasswordDialog dialog = new PasswordDialog();
		dialog.setTargetFragment(fragment, requestCode);
		dialog.show(activity.getSupportFragmentManager(), TAG);
	}

	@SuppressLint("InflateParams")
	@Override
	protected Builder build(Builder builder) {

		builder.setTitle(R.string.device_search_enter_password);

		final TextInputLayout textInputLayout = (TextInputLayout) LayoutInflater.from(getContext()).inflate(R.layout.dialog_device_enter_password, null);
		final EditText editText = (EditText) textInputLayout.findViewById(R.id.dialog_enter_password_edit_text);
		builder.setView(textInputLayout);

		builder.setPositiveButton(R.string.device_search_enter_password_ok, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(editText.getText().toString().isEmpty()){
					textInputLayout.setError(getString(R.string.activity_utils_toast_field_must_be_filled));
					return;
				}

				for (PasswordDialogListener listener : PasswordDialog.this.getDialogListeners(PasswordDialogListener.class)) {
					listener.onPositiveButtonClicked(mRequestCode, editText.getText().toString());
				}
				dismiss();
			}
		});
		return builder;
	}


	public interface PasswordDialogListener{
		void onPositiveButtonClicked(int requestCode, String password);
	}
}
