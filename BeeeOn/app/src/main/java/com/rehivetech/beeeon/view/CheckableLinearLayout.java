/**
 * 
 */
package com.rehivetech.beeeon.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;

/**
 * Layout for customListView, used in custom_spinner_dropdown_item.xml
 * 
 * @author ThinkDeep
 * 
 */
public class CheckableLinearLayout extends LinearLayout implements Checkable {

	private CheckedTextView mCheckTextView;

	public CheckableLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		int childCount = getChildCount();
		for (int i = 0; i < childCount; ++i) {
			View child = getChildAt(i);
			if (child instanceof CheckedTextView) {
				mCheckTextView = (CheckedTextView) child;
			}
		}
	}

	@Override
	public boolean isChecked() {
		return (mCheckTextView != null) ? mCheckTextView.isChecked() : false;
	}

	@Override
	public void setChecked(boolean checked) {
		if (mCheckTextView != null)
			mCheckTextView.setChecked(checked);
	}

	@Override
	public void toggle() {
		if (mCheckTextView != null)
			mCheckTextView.toggle();
	}

}
