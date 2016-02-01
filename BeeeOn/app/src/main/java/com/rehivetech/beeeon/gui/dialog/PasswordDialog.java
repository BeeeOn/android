package com.rehivetech.beeeon.gui.dialog;

import android.annotation.SuppressLint;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.avast.android.dialogs.fragment.SimpleDialogFragment;
import com.rehivetech.beeeon.R;

/**
 * Created by martin on 1.2.16.
 */
public class PasswordDialog extends SimpleDialogFragment {

	private static final String TAG = "password_dialog";

	public static void show(FragmentActivity activity, Fragment fragment) {
		PasswordDialog dialog = new PasswordDialog();
		dialog.setTargetFragment(fragment, 0);
		dialog.show(activity.getSupportFragmentManager(), TAG);
	}

	@SuppressLint("InflateParams")
	@Override
	protected Builder build(Builder builder) {

		builder.setTitle(R.string.device_search_enter_password);

		View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_device_enter_password, null);
		final EditText editText = (EditText) view.findViewById(R.id.dialog_enter_password_edit_text);
		builder.setView(view);

		builder.setPositiveButton(R.string.device_search_enter_password_ok, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		return builder;
	}
}
