package com.rehivetech.beeeon.gui.dialog;

import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;

import com.avast.android.dialogs.core.BaseDialogFragment;
import com.rehivetech.beeeon.R;

/**
 * @author Peter Vican
 * @since 30.6.2015
 */
public class ConfirmDialog extends BaseDialogFragment {
	public static String DIALOG_TAG = "confirmDialog";

	public static final int TYPE_DELETE_GATE = 10;
	public static final int TYPE_DELETE_USER = 11;
	public static final int TYPE_DELETE_DASHBOARD_VIEW = 12;
	public static final int TYPE_DELETE_DEVICE = 13;
	public static final int TYPE_CHANGE_OWNERSHIP = 20;

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

		confirmDialog.show(activity.getSupportFragmentManager(), DIALOG_TAG);
	}

	public static <T extends Fragment & ConfirmDialogListener> void confirm(T fragment, String title, String message, @StringRes int buttonTextRes, int confirmType, String dataId) {
		ConfirmDialog confirmDialog = new ConfirmDialog();

		Bundle args = new Bundle();
		args.putString(EXTRA_TITLE, title);
		args.putString(EXTRA_MESSAGE, message);
		args.putInt(EXTRA_BUTTON_TEXT_RES, buttonTextRes);
		args.putInt(EXTRA_CONFIRM_TYPE, confirmType);
		args.putString(EXTRA_DATA_ID, dataId);
		confirmDialog.setArguments(args);
		confirmDialog.setTargetFragment(fragment, confirmType);

		confirmDialog.show(fragment.getFragmentManager(), DIALOG_TAG);
	}


	/**
	 * Changes colors of dialog's buttons.
	 * Because builder does not have access to whole view, we have to change colors here
	 *
	 * @param savedInstanceState
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Button negativeButton = (Button) getDialog().findViewById(R.id.sdl_button_negative);
		if (negativeButton != null) {
			negativeButton.setTextColor(ContextCompat.getColor(getActivity(), R.color.gray_material_400));
		}
		Button negativeButtonStacked = (Button) getDialog().findViewById(R.id.sdl_button_negative_stacked);
		if (negativeButtonStacked != null) {
			negativeButtonStacked.setTextColor(ContextCompat.getColor(getActivity(), R.color.gray_material_400));
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

				for (ConfirmDialogListener listener : getDialogListeners(ConfirmDialogListener.class)) {
					listener.onConfirm(args.getInt(EXTRA_CONFIRM_TYPE), args.getString(EXTRA_DATA_ID));
				}

				dismiss();
			}
		});
		builder.setNegativeButton(R.string.activity_fragment_btn_cancel, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		return builder;
	}

	public interface ConfirmDialogListener {
		/**
		 * Listener for confirming dialog
		 *
		 * @param confirmType which dialog requested confirmation
		 * @param dataId      any string data sent through dialog
		 */
		void onConfirm(int confirmType, String dataId);
	}
}