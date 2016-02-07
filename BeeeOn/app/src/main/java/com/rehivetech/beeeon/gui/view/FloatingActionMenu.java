package com.rehivetech.beeeon.gui.view;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;

import com.rehivetech.beeeon.gui.view.behavior.FloatingActionMenuBehavior;

/**
 * Own FloatingActionMenu using coordinator layout behavior
 */
@CoordinatorLayout.DefaultBehavior(FloatingActionMenuBehavior.class)
public class FloatingActionMenu extends com.github.clans.fab.FloatingActionMenu {
	public FloatingActionMenu(Context context) {
		super(context);
	}

	public FloatingActionMenu(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FloatingActionMenu(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
}
