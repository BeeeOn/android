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

import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by martin on 1.2.16.
 */
public class ManualSearchDialog extends SimpleDialogFragment {
	private static final String TAG = "manual_search";

	private static final String IP_ADDRESS_PATTERN = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

	private static Pattern sPattern;

	static {
		sPattern = Pattern.compile(IP_ADDRESS_PATTERN);
	}

	public static void show(FragmentActivity activity, Fragment fragment, int requestCode) {
		ManualSearchDialog dialog = new ManualSearchDialog();
		dialog.setTargetFragment(fragment, requestCode);
		dialog.show(activity.getSupportFragmentManager(), TAG);
	}


	@SuppressLint("InflateParams")
	@Override
	protected Builder build(Builder builder) {

		builder.setTitle(R.string.device_search_manual_button);
		final TextInputLayout textInputLayout = (TextInputLayout) LayoutInflater.from(getContext()).inflate(R.layout.dialog_device_manual_search, null);
		builder.setView(textInputLayout);
		final EditText editText = (EditText) textInputLayout.findViewById(R.id.dialog_manual_search_editText);

		builder.setPositiveButton(R.string.device_search_manual_search_dialog_button, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!validateInput(editText.getText().toString())) {
					textInputLayout.setError(getString(R.string.device_search_manual_error));
				} else {
					for (ManualSearchDialogListener listener : ManualSearchDialog.this.getDialogListeners(ManualSearchDialogListener.class)) {
						listener.onPositiveButtonClicked(mRequestCode, editText.getText().toString());
					}
					dismiss();
				}
			}
		});
		return builder;
	}

	private static boolean validateInput(String inputString) {
		return sPattern.matcher(inputString).matches();
	}


	public interface ManualSearchDialogListener {
		void onPositiveButtonClicked(int requestCode, String ipAddress);
	}
}
