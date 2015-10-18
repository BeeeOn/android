package com.rehivetech.beeeon.gui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.rehivetech.beeeon.R;

/**
 * Created by martin on 18.10.15.
 */
@SuppressLint("ViewConstructor")
public class CustomViewChartMarkerView extends MarkerView{

	private TextView mTextView;

	private Chart mChart;
	private String mValueUnit;

	/**
	 * Constructor. Sets up the MarkerView with a custom layout resource.
	 *
	 * @param context app context
	 * @param layoutResource id of layout resource
	 * @param chart chart instance
	 * @param valueUnit unit of chart values
	 */
	public CustomViewChartMarkerView(Context context, int layoutResource, Chart chart, String valueUnit) {
		super(context, layoutResource);

		mTextView = (TextView) findViewById(R.id.util_chart_customiew_markerview_text);
		mChart = chart;
		mValueUnit = valueUnit;
	}

	@Override
	public void refreshContent(Entry e, Highlight highlight) {
		String xValue = mChart.getXValue(e.getXIndex());
		mTextView.setText(String.format("%s %s\n%s ", e.getVal(), mValueUnit, xValue));
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
