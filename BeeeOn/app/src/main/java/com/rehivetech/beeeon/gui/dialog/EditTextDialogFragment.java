package com.rehivetech.beeeon.gui.dialog;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.EditText;

import com.avast.android.dialogs.core.BaseDialogBuilder;
import com.avast.android.dialogs.core.BaseDialogFragment;
import com.rehivetech.beeeon.R;

import java.util.List;

/**
 * Created by mlyko on 25. 4. 2015.
 */
public class EditTextDialogFragment extends BaseDialogFragment {
	public static String TAG = "edit_text_picker";

	public static final String ARG_TITLE = "title";
	public static final String ARG_EDIT_TEXT_VALUE = "edit_text_value";
	public static final String ARG_EDIT_TEXT_HINT = "edit_text_hint";
	public static final String ARG_EDIT_TEXT_SET_INPUT_TYPE = "set_edit_text_input_type";
	public static final String ARG_EDIT_TEXT_INPUT_TYPE = "edit_text_input_type";
	public static final String ARG_POSITIVE_BUTTON_TEXT = "positive_button_text";
	public static final String ARG_NEGATIVE_BUTTON_TEXT = "negative_button_text";
	public static final String ARG_SHOW_KEYBOARD = "show_keyboard";

	public EditTextDialogFragment() {
	}

	public static EditTextDialogBuilder createBuilder(Context context, FragmentManager fragmentManager) {
		return new EditTextDialogBuilder(context, fragmentManager);
	}

	protected List<IEditTextDialogListener> getDialogListeners() {
		return this.getDialogListeners(IEditTextDialogListener.class);
	}

	@Override
	public Builder build(Builder builder) {
		LayoutInflater inflater = builder.getLayoutInflater();
		View view = inflater.inflate(R.layout.fragment_dialog_edit_text, null, false);

		final EditText editText = (EditText) view.findViewById(R.id.dialog_edit_text);
		editText.setText(this.getArguments().getString(ARG_EDIT_TEXT_VALUE));
		editText.setHint(this.getArguments().getString(ARG_EDIT_TEXT_HINT));

		boolean editTextInputTypeAllowed = this.getArguments().getBoolean(ARG_EDIT_TEXT_SET_INPUT_TYPE);
		if(editTextInputTypeAllowed) {
			int editTextInputType = this.getArguments().getInt(ARG_EDIT_TEXT_INPUT_TYPE);
			editText.setInputType(editTextInputType);
		}

		// shows keyboard immediately
		boolean showKeyboard = this.getArguments().getBoolean(ARG_SHOW_KEYBOARD);
		if(showKeyboard) {
			editText.requestFocus();
			getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		}

		builder
				.setTitle(this.getArguments().getString(ARG_TITLE))
				.setView(view);

		// positive button
		String positiveButtonText = this.getArguments().getString(ARG_POSITIVE_BUTTON_TEXT);
		if (!TextUtils.isEmpty(positiveButtonText)) {
			builder.setPositiveButton(positiveButtonText, new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					for (IEditTextDialogListener listener : EditTextDialogFragment.this.getDialogListeners()) {
						listener.onPositiveButtonClicked(EditTextDialogFragment.this.mRequestCode, editText, EditTextDialogFragment.this);
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
					for (IEditTextDialogListener listener : EditTextDialogFragment.this.getDialogListeners()) {
						listener.onNegativeButtonClicked(EditTextDialogFragment.this.mRequestCode, editText, EditTextDialogFragment.this);
					}
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
		private String mEditTextValue;
		private boolean mEditTextInputTypeAllowed;
		private String mEditTextHint;
		private int mEditTextInputType;
		private String mTitle;
		private String mPositiveButtonText;
		private String mNegativeButtonText;
		private boolean mShowKeyboard;

		protected EditTextDialogBuilder(Context context, FragmentManager fragmentManager) {
			super(context, fragmentManager, EditTextDialogFragment.class);
		}

		@Override
		protected EditTextDialogBuilder self() {
			return this;
		}

		public EditTextDialogBuilder setTitle(String title) {
			this.mTitle = title;
			return this;
		}

		public EditTextDialogBuilder setEditTextValue(String text) {
			this.mEditTextValue = text;
			return this;
		}

		public EditTextDialogBuilder setEditTextInputType(int type){
			this.mEditTextInputTypeAllowed = true;
			this.mEditTextInputType = type;
			return this;
		}

		public EditTextDialogBuilder setEditTextHint(String editTextHint) {
			this.mEditTextHint = editTextHint;
			return this;
		}

		public EditTextDialogBuilder setPositiveButtonText(String text) {
			this.mPositiveButtonText = text;
			return this;
		}

		public EditTextDialogBuilder setNegativeButtonText(String text) {
			this.mNegativeButtonText = text;
			return this;
		}

		public EditTextDialogBuilder showKeyboard(){
			this.mShowKeyboard = true;
			return this;
		}

		protected Bundle prepareArguments() {
			Bundle args = new Bundle();
			args.putString(ARG_TITLE, this.mTitle);
			args.putString(ARG_EDIT_TEXT_VALUE, this.mEditTextValue);
			args.putString(ARG_EDIT_TEXT_HINT, this.mEditTextHint);
			args.putInt(ARG_EDIT_TEXT_INPUT_TYPE, this.mEditTextInputType);
			args.putBoolean(ARG_EDIT_TEXT_SET_INPUT_TYPE, this.mEditTextInputTypeAllowed);
			args.putString(ARG_POSITIVE_BUTTON_TEXT, this.mPositiveButtonText);
			args.putString(ARG_NEGATIVE_BUTTON_TEXT, this.mNegativeButtonText);
			args.putBoolean(ARG_SHOW_KEYBOARD, this.mShowKeyboard);
			return args;
		}
	}

	public interface IEditTextDialogListener {
		void onPositiveButtonClicked(int requestCode, View view, EditTextDialogFragment fragment);

		void onNegativeButtonClicked(int requestCode, View view, EditTextDialogFragment fragment);
	}
}