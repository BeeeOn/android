package com.rehivetech.beeeon.gui.fragment;

import android.support.v4.app.FragmentActivity;
import android.view.View;


import com.avast.android.dialogs.core.BaseDialogFragment;
import com.rehivetech.beeeon.R;

/**
 * Created by vico on 30.6.2015.
 */
public class CriticalConfirmDialogs extends BaseDialogFragment {
	public static String TAG = "jayne";
	private int mSetTitles;
	private int mSetMessage;
	private int mPositiveTextButton;

	private DeleteConfirmDialogEvent mCallBack;

	public interface DeleteConfirmDialogEvent {
		void onDeleteDialogButtonClick(CriticalConfirmDialogs criticalConfirmDialogs);
	}

	public void show(FragmentActivity activity, int TitlesRes, int MessageRes, int ButtonTextRes, DeleteConfirmDialogEvent callBack) {
		CriticalConfirmDialogs criticalConfirmDialogs = new CriticalConfirmDialogs();
		criticalConfirmDialogs.setCallBack(callBack);
		criticalConfirmDialogs.setTitles(TitlesRes);
		criticalConfirmDialogs.setMessage(MessageRes);
		criticalConfirmDialogs.setPositiveText(ButtonTextRes);
		criticalConfirmDialogs.show(activity.getSupportFragmentManager(), TAG);
	}


	@Override
	public BaseDialogFragment.Builder build(final BaseDialogFragment.Builder builder) {
		builder.setTitle(mSetTitles);
		builder.setMessage(mSetMessage);
		builder.setPositiveButton(mPositiveTextButton, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mCallBack != null) {
					mCallBack.onDeleteDialogButtonClick(CriticalConfirmDialogs.this);
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

	public void setTitles(int setConfirmTitles) {
		mSetTitles = setConfirmTitles;
	}

	public void setMessage(int setConfirmMessage) {
		mSetMessage = setConfirmMessage;
	}

	public void setPositiveText(int setConfirmText) {
		mPositiveTextButton = setConfirmText;
	}
}