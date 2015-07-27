package com.rehivetech.beeeon.gui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.rehivetech.beeeon.R;

/**
 * @author martin on 15.7.2015.
 */
@SuppressLint("ViewConstructor")
public class ChartMarkerView extends MarkerView {

	private TextView mTextView;

	/**
	 * Constructor. Sets up the MarkerView with a custom layout resource.
	 *
	 * @param context
	 * @param layoutResource the layout resource to use for the MarkerView
	 */
	public ChartMarkerView(Context context, int layoutResource) {
		super(context, layoutResource);
		mTextView = (TextView) findViewById(R.id.util_chart_helper_markerview_text);
	}

	@Override
	public void refreshContent(Entry e, Highlight highlight) {
		mTextView.setText(Float.toString(e.getVal()));
	}

	@Override
	public int getXOffset() {
		return -(getWidth() / 2);
	}

	@Override
	public int getYOffset() {
		return -getHeight();
	}
}
