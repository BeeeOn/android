package com.rehivetech.beeeon.gui.dialog;

import android.support.annotation.StringRes;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.avast.android.dialogs.core.BaseDialogFragment;
import com.rehivetech.beeeon.R;

/**
 * Created by vico on 30.6.2015.
 */
public class ConfirmDialog extends BaseDialogFragment {
	public static String TAG = "confirmDialog";

	private String mTitle;
	private String mMessage;
	private int mButtonTextRes;

	private ConfirmDialogListener mCallback;

	public static void confirm(FragmentActivity activity, String title, String message, @StringRes int buttonTextRes, ConfirmDialogListener listener) {
		ConfirmDialog confirmDialog = new ConfirmDialog();
		confirmDialog.setConfirmListener(listener);
		confirmDialog.setTitle(title);
		confirmDialog.setMessage(message);
		confirmDialog.setButtonText(buttonTextRes);
		confirmDialog.show(activity.getSupportFragmentManager(), TAG);
	}

	@Override
	public BaseDialogFragment.Builder build(final BaseDialogFragment.Builder builder) {
		builder.setTitle(mTitle);
		builder.setMessage(mMessage);
		builder.setPositiveButton(mButtonTextRes, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mCallback != null) {
					mCallback.onConfirm();
				}
				dismiss();
			}
		});
		builder.setNegativeButton(R.string.notification_cancel, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		return builder;
	}

	public void setConfirmListener(ConfirmDialogListener callback) {
		mCallback = callback;
	}

	public void setTitle(String title) {
		mTitle = title;
	}

	public void setMessage(String message) {
		mMessage = message;
	}

	public void setButtonText(@StringRes int buttonTextRes) {
		mButtonTextRes = buttonTextRes;
	}

	public interface ConfirmDialogListener {
		void onConfirm();
	}
}