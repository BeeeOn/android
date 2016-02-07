package com.rehivetech.beeeon.gui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.EditText;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.view.FloatingActionButton;

/**
 * Created by martin on 7.2.16.
 */
public abstract class BaseAddDashBoardItemFragment extends BaseApplicationFragment{

	protected static final String ARG_GATE_ID = "gate_id";

	protected String mGateId;

	protected TextInputLayout mTextInputLayout;
	protected EditText mItemNameEditText;

	protected FloatingActionButton mButtonDone;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();

		if (args != null) {
			mGateId = args.getString(ARG_GATE_ID);
		}
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mTextInputLayout = (TextInputLayout) view.findViewById(R.id.fragment_add_dashboard_item_text_input);
		mItemNameEditText = (EditText) view.findViewById(R.id.fragment_add_dashboard_item_name_edit_text);
		mButtonDone = (FloatingActionButton) view.findViewById(R.id.fragment_add_dashboard_item_button_done);
	}
}