package com.rehivetech.beeeon.gui.menuItem;

import android.view.View;

public interface IMenuItem {
	public static final String ID_SETTINGS = "id_settings";
	public static final String ID_ABOUT = "id_about";
	public static final String ID_LOGOUT = "id_logout";
	public static final String ID_UNDEFINED = "id_undefined";

	// TODO test pryc -> presunout do sekce Applications

	public enum MenuItemType {
		GATE, GROUP_IMAGE, GROUP, CUSTOM_VIEW, LOCATION, PROFILE, SEPARATOR, SETTING, EMPTY, APPLICATION
	}

	public void setView(View view);

	public int getLayout();

	public String getId();

	public MenuItemType getType();

	public void setIsSelected();

	public void setNotSelected();
}
