package com.rehivetech.beeeon.gui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.avast.android.dialogs.core.BaseDialogBuilder;
import com.avast.android.dialogs.core.BaseDialogFragment;
import com.rehivetech.beeeon.R;

import java.util.List;

/**
 * @author Tomas Mlynaric
 * @since 25. 4. 2015.
 */
public class EditTextDialog extends BaseDialogFragment {

	public static final String ARG_TITLE = "title";
	public static final String ARG_LAYOUT_RES = "layout_res";
	public static final String ARG_EDIT_TEXT_VALUE = "edit_text_value";
	public static final String ARG_EDIT_TEXT_HINT = "edit_text_hint";
	public static final String ARG_EDIT_TEXT_INPUT_TYPE = "edit_text_input_type";
	public static final String ARG_POSITIVE_BUTTON_TEXT = "positive_button_text";
	public static final String ARG_NEGATIVE_BUTTON_TEXT = "negative_button_text";
	public static final String ARG_SHOW_KEYBOARD = "show_keyboard";

	public static final int R_DEFAULT_LAYOUT = R.layout.fragment_dialog_edit_text;

	public static EditTextDialogBuilder createBuilder(Context context, FragmentManager fragmentManager) {
		return new EditTextDialogBuilder(context, fragmentManager);
	}

	protected View mView;

	@SuppressLint("InflateParams")
	@Override
	public Builder build(Builder builder) {
		LayoutInflater inflater = builder.getLayoutInflater();
		mView = inflater.inflate(this.getArguments().getInt(ARG_LAYOUT_RES), null, false);

		final TextInputLayout textInputLayout = (TextInputLayout) mView.findViewById(R.id.dialog_edit_text_input_layout);

		// setting EditText options
		EditText editText = textInputLayout.getEditText();
		if (editText != null) {
			editText.setText(this.getArguments().getString(ARG_EDIT_TEXT_VALUE));
			textInputLayout.setHint(this.getArguments().getString(ARG_EDIT_TEXT_HINT));
			int inputType = this.getArguments().getInt(ARG_EDIT_TEXT_INPUT_TYPE);
			if (inputType != 0) {
				editText.setInputType(this.getArguments().getInt(ARG_EDIT_TEXT_INPUT_TYPE));
			}
		}

		// shows keyboard immediately
		boolean showKeyboard = this.getArguments().getBoolean(ARG_SHOW_KEYBOARD);
		if (showKeyboard) {
			textInputLayout.requestFocus();
			getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		}

		// setting title from args
		builder.setTitle(this.getArguments().getString(ARG_TITLE));
		builder.setView(mView);

		// positive button
		String positiveButtonText = this.getArguments().getString(ARG_POSITIVE_BUTTON_TEXT);
		if (!TextUtils.isEmpty(positiveButtonText)) {
			builder.setPositiveButton(positiveButtonText, new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					for (IPositiveButtonDialogListener listener : EditTextDialog.this.getPositiveButtonDialogListeners()) {
						listener.onPositiveButtonClicked(EditTextDialog.this.mRequestCode, mView, EditTextDialog.this);
					}
				}
			});
		}

		// negative button
		String negativeButtonText = this.getArguments().getString(ARG_NEGATIVE_BUTTON_TEXT);
		if (!TextUtils.isEmpty(negativeButtonText)) {
			builder.setNegativeButton(negativeButtonText, new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					for (INegativeButtonDialogListener listener : EditTextDialog.this.getNegativeButtonDialogListeners()) {
						listener.onNegativeButtonClicked(EditTextDialog.this.mRequestCode, mView, EditTextDialog.this);
					}

					EditTextDialog.this.dismiss();
				}
			});
		}

		return builder;
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (this.getArguments() == null) {
			throw new IllegalArgumentException("use EditTextDialogBuilder to construct this dialog");
		}
	}

	public static class EditTextDialogBuilder extends BaseDialogBuilder<EditTextDialogBuilder> {
		@LayoutRes
		private int mLayoutRes = R_DEFAULT_LAYOUT;
		private String mEditTextValue;
		private String mEditTextHint;
		private int mEditTextInputType;
		private String mTitle;
		private String mPositiveButtonText;
		private String mNegativeButtonText;
		private boolean mShowKeyboard;

		protected EditTextDialogBuilder(Context context, FragmentManager fragmentManager) {
			super(context, fragmentManager, EditTextDialog.class);
		}

		@Override
		protected EditTextDialogBuilder self() {
			return this;
		}

		/**
		 * Must be LinearLayout with TextInputLayout and EditText inside!
		 *
		 * @param layoutRes id of TextInputLayout is R.id.dialog_edit_text_input_layout and EditText is R.id.dialog_edit_text
		 */
		public EditTextDialogBuilder setLayoutRes(@LayoutRes int layoutRes) {
			mLayoutRes = layoutRes;
			return this;
		}

		public EditTextDialogBuilder setTitle(String title) {
			this.mTitle = title;
			return this;
		}

		public EditTextDialogBuilder setTitle(@StringRes int stringRes) {
			this.mTitle = this.mContext.getString(stringRes);
			return this;
		}

		public EditTextDialogBuilder setEditTextValue(String text) {
			this.mEditTextValue = text;
			return this;
		}

		public EditTextDialogBuilder setEditTextValue(@StringRes int stringRes) {
			this.mEditTextValue = this.mContext.getString(stringRes);
			return this;
		}

		public EditTextDialogBuilder setInputType(int type) {
			this.mEditTextInputType = type;
			return this;
		}

		public EditTextDialogBuilder setHint(String editTextHint) {
			this.mEditTextHint = editTextHint;
			return this;
		}

		public EditTextDialogBuilder setHint(@StringRes int stringRes) {
			this.mEditTextHint = this.mContext.getString(stringRes);
			return this;
		}

		public EditTextDialogBuilder setPositiveButtonText(String text) {
			this.mPositiveButtonText = text;
			return this;
		}

		public EditTextDialogBuilder setPositiveButtonText(@StringRes int stringRes) {
			this.mPositiveButtonText = this.mContext.getString(stringRes);
			return this;
		}

		public EditTextDialogBuilder setNegativeButtonText(String text) {
			this.mNegativeButtonText = text;
			return this;
		}

		public EditTextDialogBuilder setNegativeButtonText(@StringRes int stringRes) {
			this.mNegativeButtonText = this.mContext.getString(stringRes);
			return this;
		}

		public EditTextDialogBuilder showKeyboard() {
			this.mShowKeyboard = true;
			return this;
		}

		protected Bundle prepareArguments() {
			Bundle args = new Bundle();
			args.putString(ARG_TITLE, this.mTitle);
			args.putInt(ARG_LAYOUT_RES, this.mLayoutRes);
			args.putString(ARG_EDIT_TEXT_VALUE, this.mEditTextValue);
			args.putString(ARG_EDIT_TEXT_HINT, this.mEditTextHint);
			args.putInt(ARG_EDIT_TEXT_INPUT_TYPE, this.mEditTextInputType);
			args.putString(ARG_POSITIVE_BUTTON_TEXT, this.mPositiveButtonText);
			args.putString(ARG_NEGATIVE_BUTTON_TEXT, this.mNegativeButtonText);
			args.putBoolean(ARG_SHOW_KEYBOARD, this.mShowKeyboard);
			return args;
		}
	}

	protected List<IPositiveButtonDialogListener> getPositiveButtonDialogListeners() {
		return this.getDialogListeners(IPositiveButtonDialogListener.class);
	}

	protected List<INegativeButtonDialogListener> getNegativeButtonDialogListeners() {
		return this.getDialogListeners(INegativeButtonDialogListener.class);
	}

	public interface IPositiveButtonDialogListener {
		void onPositiveButtonClicked(int requestCode, View view, BaseDialogFragment fragment);
	}

	public interface INegativeButtonDialogListener {
		void onNegativeButtonClicked(int requestCode, View view, BaseDialogFragment fragment);
	}
}