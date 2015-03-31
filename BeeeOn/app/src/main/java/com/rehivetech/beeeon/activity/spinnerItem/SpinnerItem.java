package com.rehivetech.beeeon.activity.spinnerItem;

import android.view.View;

public interface SpinnerItem {
	public static final String ID_UNDEFINED = "id_undefined";

	public enum SpinnerItemType {
		HEADER, DEVICE, GEOFENCE
	}

	public void setView(View view);

	public Object getObject();

	public int getLayout();

	public String getId();

	public SpinnerItemType getType();
	
	public void setIsSelected();
	
	public void setNotSelected();
}
