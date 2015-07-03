package com.rehivetech.beeeon.gui.fragment;

import android.support.v4.app.FragmentActivity;
import android.view.View;


import com.avast.android.dialogs.core.BaseDialogFragment;
import com.rehivetech.beeeon.R;

/**
 * Created by vico on 30.6.2015.
 */
public class ConfirmDialogFragment extends BaseDialogFragment {
	public static String TAG = "confirmDialog";
	private String mSetTitles;
	private String mSetMessage;
	private int mPositiveTextButton;

	private DeleteConfirmDialogEvent mCallBack;

	public interface DeleteConfirmDialogEvent {
		void onDeleteDialogButtonClick();
	}

	public static void confirm(FragmentActivity activity, String TitlesRes, String MessageRes, int ButtonTextRes, DeleteConfirmDialogEvent callBack) {
		ConfirmDialogFragment confirmDialogFragment = new ConfirmDialogFragment();
		confirmDialogFragment.setCallBack(callBack);
		confirmDialogFragment.setTitles(TitlesRes);
		confirmDialogFragment.setMessage(MessageRes);
		confirmDialogFragment.setPositiveText(ButtonTextRes);
		confirmDialogFragment.show(activity.getSupportFragmentManager(), TAG);
	}


	@Override
	public BaseDialogFragment.Builder build(final BaseDialogFragment.Builder builder) {
		builder.setTitle(mSetTitles);
		builder.setMessage(mSetMessage);
		builder.setPositiveButton(mPositiveTextButton, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mCallBack != null) {
					mCallBack.onDeleteDialogButtonClick();
					dismiss();

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


	public void setCallBack(DeleteConfirmDialogEvent callBack) {
		mCallBack = callBack;
	}

	public void setTitles(String setConfirmTitles) {
		mSetTitles = setConfirmTitles;
	}

	public void setMessage(String setConfirmMessage) {
		mSetMessage = setConfirmMessage;
	}

	public void setPositiveText(int setConfirmText) {
		mPositiveTextButton = setConfirmText;
	}
}