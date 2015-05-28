package com.rehivetech.beeeon.gui.dialog;

import android.widget.EditText;

public interface ILocationPickerDialogListener {
	void onPositiveButtonClicked(int var1, EditText var2, LocationPickerDialogFragment var3);

	void onNegativeButtonClicked(int var1, EditText var2, LocationPickerDialogFragment var3);
}
