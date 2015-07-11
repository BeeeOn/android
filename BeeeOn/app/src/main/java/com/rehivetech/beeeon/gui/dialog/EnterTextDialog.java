package com.rehivetech.beeeon.gui.dialog;

import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.avast.android.dialogs.core.BaseDialogFragment;
import com.rehivetech.beeeon.R;

/**
 * Created by vico on 8.7.2015.
 */
public class EnterTextDialog extends BaseDialogFragment {
	public static String TAG = "enterDialog";


	private IEnterTextDialogListener mCallback;
	private View mView;
	private boolean mClose;

	public static void enterText(FragmentActivity activity, View view,IEnterTextDialogListener listener) {
		EnterTextDialog enterTextDialog = new EnterTextDialog();
		enterTextDialog.setView(view);

		enterTextDialog.setEnterTextListener(listener);
		enterTextDialog.show(activity.getSupportFragmentManager(), TAG);
	}

	@Override
	public BaseDialogFragment.Builder build(final BaseDialogFragment.Builder builder) {
		builder.setView(mView);
		builder.setTitle(R.string.enter_text_dialog_title);

		builder.setPositiveButton(R.string.ok, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mCallback != null) {
					mCallback.onEnterText();

				}
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

	public void setEnterTextListener(IEnterTextDialogListener listener) {
		mCallback = listener;
	}

	public void setView(View view) {
		mView = view;
	}

	public void closeDialog() {
		dismiss();
	}

	public interface IEnterTextDialogListener {
		void onEnterText();
	}
}