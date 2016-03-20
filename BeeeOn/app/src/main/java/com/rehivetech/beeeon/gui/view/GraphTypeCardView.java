package com.rehivetech.beeeon.gui.view;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;

import com.rehivetech.beeeon.R;

/**
 * Created by martin on 28.2.16.
 */
public class GraphTypeCardView extends CardView {
	public GraphTypeCardView(Context context) {
		super(context);
	}

	public GraphTypeCardView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GraphTypeCardView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();

		//FIXME obtain colors from style attrs
		if (isSelected()) {
			this.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.beeeon_primary));
		} else {
			this.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.cardview_light_background));
		}
	}
}
