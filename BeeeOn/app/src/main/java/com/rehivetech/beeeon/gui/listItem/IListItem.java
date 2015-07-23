package com.rehivetech.beeeon.gui.listItem;

import android.view.View;

public interface IListItem {
	String ID_UNDEFINED = "id_undefined";

	// TODO test pryc -> presunout do sekce Applications

	enum ListItemType {
		LOCATION, MODULE
	}

	void setView(View view);

	int getLayout();

	String getId();

	ListItemType getType();

	void setIsSelected();

	void setNotSelected();
}
