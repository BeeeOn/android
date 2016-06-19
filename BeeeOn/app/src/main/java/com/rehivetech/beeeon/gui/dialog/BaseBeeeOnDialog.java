package com.rehivetech.beeeon.gui.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.avast.android.dialogs.fragment.SimpleDialogFragment;
import com.rehivetech.beeeon.R;

import icepick.Icepick;

/**
 * @author mlyko
 * @since 31.05.2016
 */
public abstract class BaseBeeeOnDialog extends SimpleDialogFragment {
	protected View mRootView;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Icepick.restoreInstanceState(this, savedInstanceState);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Icepick.saveInstanceState(this, outState);
	}

	/**
	 * Forces to specify layout which will be used for this type of dialogs
	 *
	 * @return Layout resource id
	 */
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
						listener.onPositiveButtonClicked(mRequestCode, mRootView, BaseBeeeOnDialog.this);
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
						listener.onNegativeButtonClicked(mRequestCode, mRootView, BaseBeeeOnDialog.this);
					}
					dismiss();
				}
			});
		}

		return builder;
	}

	public void setDeleteButton(Builder builder, CharSequence text) {
		if (!TextUtils.isEmpty(text)) {
			builder.setNeutralButton(text, new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					for (IDeleteButtonDialogListener listener : getDialogListeners(IDeleteButtonDialogListener.class)) {
						listener.onDeleteButtonClicked(mRequestCode, mRootView, BaseBeeeOnDialog.this);
					}
					dismiss();
				}
			});
		}
		else{
			builder.setNeutralButton(null, null);
		}
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
			neutralButton.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
		}
		Button neutralButtonStacked = (Button) getDialog().findViewById(R.id.sdl_button_neutral_stacked);
		if (neutralButtonStacked != null) {
			neutralButtonStacked.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
		}

		Button negativeButton = (Button) getDialog().findViewById(R.id.sdl_button_negative);
		if (negativeButton != null) {
			negativeButton.setTextColor(ContextCompat.getColor(getContext(), R.color.gray_material_400));
		}
		Button negativeButtonStacked = (Button) getDialog().findViewById(R.id.sdl_button_negative_stacked);
		if (negativeButtonStacked != null) {
			negativeButtonStacked.setTextColor(ContextCompat.getColor(getActivity(), R.color.gray_material_400));
		}
	}

	public static class BaseBeeeOnDialogBuilder extends SimpleDialogFragment.SimpleDialogBuilder {

		public BaseBeeeOnDialogBuilder(Context context, FragmentManager fragmentManager) {
			super(context, fragmentManager, ServerDetailDialog.class);
		}
	}

	public interface IPositiveButtonDialogListener {
		void onPositiveButtonClicked(int requestCode, View view, BaseBeeeOnDialog baseDialog);
	}

	public interface INegativeButtonDialogListner {
		void onNegativeButtonClicked(int requestCode, View view, BaseBeeeOnDialog baseDialog);
	}

	public interface IDeleteButtonDialogListener {
		void onDeleteButtonClicked(int requestCode, View view, BaseBeeeOnDialog baseDialog);
	}
}
