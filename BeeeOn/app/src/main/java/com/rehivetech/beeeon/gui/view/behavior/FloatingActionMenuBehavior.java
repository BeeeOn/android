package com.rehivetech.beeeon.gui.view.behavior;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.view.View;

import com.github.clans.fab.FloatingActionMenu;
import com.rehivetech.beeeon.util.Utils;

/**
 * Behavior for Floating action menu
 */
public class FloatingActionMenuBehavior extends CoordinatorLayout.Behavior<FloatingActionMenu> {

	private int mToolbarHeight = -1;

	public FloatingActionMenuBehavior() {
		super();
	}

	public FloatingActionMenuBehavior(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionMenu child, View dependency) {
		return dependency instanceof Snackbar.SnackbarLayout
				|| dependency instanceof AppBarLayout;
	}

	@Override
	public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionMenu fam, View dependency) {
		super.onDependentViewChanged(parent, fam, dependency);

		if (mToolbarHeight == -1) {
			mToolbarHeight = Utils.getToolbarHeight(fam.getContext());
		}

		float translationY;
		if (dependency instanceof Snackbar.SnackbarLayout) {
			translationY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());
			fam.setTranslationY(translationY);
		}
		return true;
	}

}
