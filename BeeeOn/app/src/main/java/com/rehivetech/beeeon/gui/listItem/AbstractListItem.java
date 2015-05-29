package com.rehivetech.beeeon.gui.listItem;

import android.view.View;

public abstract class AbstractListItem implements IListItem {
	private String mId = ID_UNDEFINED;
	private ListItemType mType;
	private View mMView;

	public AbstractListItem(String id, ListItemType type) {
		mId = id;
		mType = type;
	}

	@Override
	public String getId() {
		return mId;
	}

	@Override
	public ListItemType getType() {
		return mType;
	}

	public void setMView(View view) {
		mMView = view;
	}

	public View getMView() {
		return mMView;
	}
}
