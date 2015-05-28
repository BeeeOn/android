package com.rehivetech.beeeon.activity.listItem;

import android.view.View;

public interface ListItem {
	public static final String ID_UNDEFINED = "id_undefined";

	// TODO test pryc -> presunout do sekce Applications

	public enum ListItemType {
		LOCATION, SENSOR
	}

	public void setView(View view);

	public int getLayout();

	public String getId();

	public ListItemType getType();

	public void setIsSelected();

	public void setNotSelected();
}
