package com.rehivetech.beeeon.gui.dialog;

import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.avast.android.dialogs.core.BaseDialogFragment;
import com.rehivetech.beeeon.R;

/**
 * @author Tomas Mlynaric
 */
public class EnterPasswordDialog extends EditTextDialog {
	@Override
	public Builder build(Builder builder) {
		super.build(builder);

		// TODO need to fix this so that we can add behavior to the checkbox
		CheckBox checkBox = (CheckBox) mView.findViewById(R.id.dialog_enter_password_checkbox);
		checkBox.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(getActivity(), "ASDFGH", Toast.LENGTH_LONG).show();
			}
		});

		return builder;
	}
}
