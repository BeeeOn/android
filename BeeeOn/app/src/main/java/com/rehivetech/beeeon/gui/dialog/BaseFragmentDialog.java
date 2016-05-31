package com.rehivetech.beeeon.gui.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.avast.android.dialogs.fragment.SimpleDialogFragment;
import com.rehivetech.beeeon.R;

/**
 * @author mlyko
 * @since 31.05.2016
 */
public abstract class BaseFragmentDialog extends SimpleDialogFragment {
	protected View mRootView;

	@LayoutRes
	public abstract int getLayoutResource();

	@SuppressLint("InflateParams")
	@Override
	protected Builder build(Builder builder) {
		mRootView = builder.getLayoutInflater().inflate(getLayoutResource(), null, false);
		builder.setView(mRootView);

		// title
		final CharSequence title = getTitle();
		if (!TextUtils.isEmpty(title)) {
			builder.setTitle(title);
		}

		// positive button
		final CharSequence positiveButtonText = getPositiveButtonText();
		// only shown when user is author
		if (!TextUtils.isEmpty(positiveButtonText)) {
			builder.setPositiveButton(positiveButtonText, new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					for (IPositiveButtonDialogListener listener : getDialogListeners(IPositiveButtonDialogListener.class)) {
						listener.onPositiveButtonClicked(mRequestCode, mRootView, BaseFragmentDialog.this);
					}
					// dismiss should be called inside of listener
				}
			});
		}

		// negative button
		final CharSequence negativeButtonText = getNegativeButtonText();
		if (!TextUtils.isEmpty(negativeButtonText)) {
			builder.setNegativeButton(negativeButtonText, new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					for (INegativeButtonDialogListner listener : getDialogListeners(INegativeButtonDialogListner.class)) {
						listener.onNegativeButtonClicked(mRequestCode, mRootView, BaseFragmentDialog.this);
					}
					dismiss();
				}
			});
		}

		// delete button
		final CharSequence neutralButtonText = getNeutralButtonText();
		if (!TextUtils.isEmpty(neutralButtonText)) {
			builder.setNeutralButton(neutralButtonText, new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					for (IDeleteButtonDialogListener listener : getDialogListeners(IDeleteButtonDialogListener.class)) {
						listener.onDeleteButtonClicked(mRequestCode, mRootView, BaseFragmentDialog.this);
					}
					dismiss();
				}
			});
		}

		return builder;
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

		// setup delete button as red
		Button neutralButton = (Button) getDialog().findViewById(R.id.sdl_button_neutral);
		if (neutralButton != null) {
			neutralButton.setTextColor(getResources().getColor(R.color.black));
		}
		Button neutralButtonStacked = (Button) getDialog().findViewById(R.id.sdl_button_neutral_stacked);
		if (neutralButtonStacked != null) {
			neutralButtonStacked.setTextColor(getResources().getColor(R.color.black));
		}

		Button negativeButton = (Button) getDialog().findViewById(R.id.sdl_button_negative);
		if (negativeButton != null) {
			negativeButton.setTextColor(getResources().getColor(R.color.gray_material_400));
		}
		Button negativeButtonStacked = (Button) getDialog().findViewById(R.id.sdl_button_negative_stacked);
		if (negativeButtonStacked != null) {
			negativeButtonStacked.setTextColor(ContextCompat.getColor(getActivity(), R.color.gray_material_400));
		}
	}

	public static class BaseFragmentDialogBuilder extends SimpleDialogFragment.SimpleDialogBuilder {

		public BaseFragmentDialogBuilder(Context context, FragmentManager fragmentManager) {
			super(context, fragmentManager, ServerDetailDialog.class);
		}

		@Override
		protected BaseFragmentDialogBuilder self() {
			return this;
		}
	}

	public interface IPositiveButtonDialogListener<T extends BaseFragmentDialog> {
		void onPositiveButtonClicked(int requestCode, View view, T dialog);
	}

	public interface INegativeButtonDialogListner<T extends BaseFragmentDialog> {
		void onNegativeButtonClicked(int requestCode, View view, T dialog);
	}

	public interface IDeleteButtonDialogListener<T extends BaseFragmentDialog> {
		void onDeleteButtonClicked(int requestCode, View view, T dialog);
	}
}
