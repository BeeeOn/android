package com.rehivetech.beeeon.gui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.widget.TextView;

import com.github.mikephil.charting.charts.Chart;
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
	private Chart mChart;


	/**
	 * Constructor. Sets up the MarkerView with a custom layout resource.
	 *
	 * @param context context
	 * @param layoutResource the layout resource to use for the MarkerView
	 * @param chart  the chart reference
	 */
	public ChartMarkerView(Context context, int layoutResource, Chart chart) {
		super(context, layoutResource);
		mTextView = (TextView) findViewById(R.id.util_chart_helper_markerview_text);
		mChart = chart;
	}

	@Override
	public void draw(Canvas canvas, float posx, float posy) {
		// take offsets into consideration
		posx += getXOffset();
		posy = canvas.getHeight() / 2 + getYOffset();

		// translate to the correct position and draw
		canvas.translate(posx, posy);
		draw(canvas);
		canvas.translate(-posx, -posy);
	}

	@Override
	public void refreshContent(Entry e, Highlight highlight) {
		String xValue = mChart.getXValue(e.getXIndex());
		mTextView.setText(String.format("%s\n%s ", e.getVal(), xValue));

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
