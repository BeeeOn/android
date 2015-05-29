package com.rehivetech.beeeon.gui.spinnerItem;

import android.view.View;

public interface ISpinnerItem {
	public static final String ID_UNDEFINED = "id_undefined";

	public enum SpinnerItemType {
		HEADER, MODULE, GEOFENCE
	}

	public void setDropDownView(View view);

	public void setView(View view);

	public Object getObject();

	public int getDropDownLayout();

	public int getLayout();

	public String getId();

	public SpinnerItemType getType();

	public void setIsSelected();

	public void setNotSelected();
}
