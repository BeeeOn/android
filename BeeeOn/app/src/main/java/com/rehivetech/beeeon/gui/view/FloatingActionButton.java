package com.rehivetech.beeeon.gui.view;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;

import com.rehivetech.beeeon.gui.view.behavior.FloatingActionButtonBehavior;

/**
 * Own FloatingAction button using coordinator layout behavior
 */
@CoordinatorLayout.DefaultBehavior(FloatingActionButtonBehavior.class)
public class FloatingActionButton extends com.github.clans.fab.FloatingActionButton {
	public FloatingActionButton(Context context) {
		super(context);
	}

	public FloatingActionButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}
}
