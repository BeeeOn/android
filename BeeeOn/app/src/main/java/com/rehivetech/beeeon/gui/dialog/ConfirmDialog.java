package com.rehivetech.beeeon.gui.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;

import com.avast.android.dialogs.core.BaseDialogFragment;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.activity.GateEditActivity;

/**
 * Created by vico on 30.6.2015.
 */
public class ConfirmDialog extends BaseDialogFragment {
	public static String TAG = "confirmDialog";

	public static int TYPE_DELETE_GATE = 10;
	public static int TYPE_DELETE_USER = 11;
	public static int TYPE_DELETE_WATCHDOG = 12;
	public static int TYPE_DELETE_DEVICE = 13;
	public static int TYPE_CHANGE_OWNERSHIP = 20;

	private ConfirmDialogListener mCallback;

	private static final String EXTRA_TITLE = "extra_title";
	private static final String EXTRA_MESSAGE = "extra_message";
	private static final String EXTRA_BUTTON_TEXT_RES = "extra_button_text_res";

	private static final String EXTRA_CONFIRM_TYPE = "extra_confirm_type";
	private static final String EXTRA_DATA_ID = "extra_data_id";

	public static <T extends FragmentActivity & ConfirmDialogListener> void confirm(T activity, String title, String message, @StringRes int buttonTextRes, int confirmType, String dataId) {
		ConfirmDialog confirmDialog = new ConfirmDialog();

		Bundle args = new Bundle();
		args.putString(EXTRA_TITLE, title);
		args.putString(EXTRA_MESSAGE, message);
		args.putInt(EXTRA_BUTTON_TEXT_RES, buttonTextRes);
		args.putInt(EXTRA_CONFIRM_TYPE, confirmType);
		args.putString(EXTRA_DATA_ID, dataId);
		confirmDialog.setArguments(args);

		confirmDialog.show(activity.getSupportFragmentManager(), TAG);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mCallback = (ConfirmDialogListener) getActivity();
		} catch (ClassCastException e) {
			throw new ClassCastException(String.format("%s must implement ConfirmDialogListener", activity.toString()));
		}
	}

	@Override
	public BaseDialogFragment.Builder build(final BaseDialogFragment.Builder builder) {
		final Bundle args = getArguments();

		builder.setTitle(args.getString(EXTRA_TITLE));
		builder.setMessage(args.getString(EXTRA_MESSAGE));
		builder.setPositiveButton(args.getInt(EXTRA_BUTTON_TEXT_RES), new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mCallback.onConfirm(args.getInt(EXTRA_CONFIRM_TYPE), args.getString(EXTRA_DATA_ID));
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

	public interface ConfirmDialogListener {
		void onConfirm(int confirmType, String dataId);
	}
}