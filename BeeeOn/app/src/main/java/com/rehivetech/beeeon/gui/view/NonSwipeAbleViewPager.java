package com.rehivetech.beeeon.gui.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by martin on 10.12.15.
 */
public class NonSwipeAbleViewPager extends ViewPager {

	public NonSwipeAbleViewPager(Context context) {
		super(context);
	}

	public NonSwipeAbleViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return false;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		return false;
	}
}
