package com.rehivetech.beeeon.activity.spinnerItem;

import android.view.View;

import com.rehivetech.beeeon.activity.listItem.ListItem;

public abstract class AbstractSpinnerItem implements SpinnerItem {
	private String mId = ID_UNDEFINED;
	private SpinnerItemType mType;
	private View mMView;

	public AbstractSpinnerItem(String id, SpinnerItemType type) {
		mId = id;
		mType = type;
	}

	@Override
	public String getId() {
		return mId;
	}

	@Override
	public SpinnerItemType getType() {
		return mType;
	}
	
	public void setMView(View view) {
		mMView = view;
	}
	
	public View getMView(){
		return mMView;
	}


	// if not set these methods, use the same layout for dropdown and selected
	public int getDropDownLayout(){
		return getLayout();
	}

	public void setDropDownView(View view){
		setView(view);
	}
}
