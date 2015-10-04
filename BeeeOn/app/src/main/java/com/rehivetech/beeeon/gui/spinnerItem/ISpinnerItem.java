package com.rehivetech.beeeon.gui.spinnerItem;

import android.view.View;

public interface ISpinnerItem {
	String ID_UNDEFINED = "id_undefined";

	enum SpinnerItemType {
		HEADER, MODULE
	}

	void setDropDownView(View view);

	void setView(View view);

	Object getObject();

	int getDropDownLayout();

	int getLayout();

	String getId();

	SpinnerItemType getType();

	void setIsSelected();

	void setNotSelected();
}
