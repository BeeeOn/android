package com.rehivetech.beeeon.gui.view.behavior;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.view.View;

import com.github.clans.fab.FloatingActionButton;
import com.rehivetech.beeeon.util.Utils;

/**
 * Behavior for Floating action button
 */
public class FloatingActionButtonBehavior extends CoordinatorLayout.Behavior<FloatingActionButton> {

	private int mToolbarHeight = -1;

	public FloatingActionButtonBehavior() {
		super();
	}

	public FloatingActionButtonBehavior(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
		return dependency instanceof Snackbar.SnackbarLayout
				|| dependency instanceof AppBarLayout;
	}

	@Override
	public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionButton fab, View dependency) {
		super.onDependentViewChanged(parent, fab, dependency);

		if (mToolbarHeight == -1) {
			mToolbarHeight = Utils.getToolbarHeight(fab.getContext());
		}

		float translationY;
		if (dependency instanceof Snackbar.SnackbarLayout) {
			translationY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());
		} else {
			CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) fab
					.getLayoutParams();
			int famBottomMargin = lp.bottomMargin;
			int height = fab.getHeight();
			int distanceToScroll = height + famBottomMargin;
			float ratio = dependency.getY() / (float) mToolbarHeight;
			translationY = - distanceToScroll * ratio;
		}
		fab.setTranslationY(translationY);

		return true;
	}

}