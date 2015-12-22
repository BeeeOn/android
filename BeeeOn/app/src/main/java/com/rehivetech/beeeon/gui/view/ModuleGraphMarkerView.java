package com.rehivetech.beeeon.gui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.util.Utils;

/**
 * Created by martin on 8.12.15.
 */
@SuppressLint("ViewConstructor")
public class ModuleGraphMarkerView extends MarkerView {

	private LineChart mChart;

	private TextView mTextMin;
	private TextView mTextAvg;
	private TextView mTextMax;
	private TextView mTextXval;

	private String mUnit;

	/**
	 * Constructor. Sets up the MarkerView with a custom layout resource.
	 *
	 * @param context
	 * @param layoutResource
	 */
	public ModuleGraphMarkerView(Context context, int layoutResource, LineChart chart, String unit) {
		super(context, layoutResource);
		mChart = chart;
		mUnit = unit;

		mTextMin = (TextView) findViewById(R.id.util_chart_markerview_text_min);
		mTextAvg = (TextView) findViewById(R.id.util_chart_markerview_text_avg);
		mTextMax = (TextView) findViewById(R.id.util_chart_markerview_text_max);
		mTextXval = (TextView) findViewById(R.id.util_chart_markerview_text_xval);

		int shapeSize = Utils.convertDpToPixel(10);

		GradientDrawable shapeMin = (GradientDrawable) ContextCompat.getDrawable(context, R.drawable.oval_white);
		shapeMin.setColor(Utils.getGraphColor(context, 1));
		shapeMin.setBounds(0, 0, shapeSize, shapeSize);
		mTextMin.setCompoundDrawables(shapeMin, null, null, null);

		GradientDrawable shapeAvg = (GradientDrawable) ContextCompat.getDrawable(context, R.drawable.oval_white);
		shapeAvg.setColor(Utils.getGraphColor(context, 0));
		shapeAvg.setBounds(0, 0, shapeSize, shapeSize);
		mTextAvg.setCompoundDrawables(shapeAvg, null, null, null);

		GradientDrawable shapeMax = (GradientDrawable) ContextCompat.getDrawable(context, R.drawable.oval_white);
		shapeMax.setColor(Utils.getGraphColor(context, 2));
		shapeMax.setBounds(0, 0, shapeSize, shapeSize);
		mTextMax.setCompoundDrawables(shapeMax, null, null, null);
	}

	@Override
	public void refreshContent(Entry e, Highlight highlight) {
		clearTexts();

		for (DataSet dataSet : mChart.getData().getDataSets()) {
			if (dataSet.getLabel().contains("min")) {
				mTextMin.setText(String.format("%s %s", dataSet.getYValForXIndex(e.getXIndex()), mUnit));

			} else if (dataSet.getLabel().contains("avg")) {
				mTextAvg.setText(String.format("%s %s", dataSet.getYValForXIndex(e.getXIndex()), mUnit));

			} else if (dataSet.getLabel().contains("max")) {
				mTextMax.setText(String.format("%s %s", dataSet.getYValForXIndex(e.getXIndex()), mUnit));
			}
		}
		mTextXval.setText(mChart.getXValue(e.getXIndex()));

		mTextMin.setVisibility(mTextMin.getText().length() > 0 ? VISIBLE : GONE);
		mTextAvg.setVisibility(mTextAvg.getText().length() > 0 ? VISIBLE : GONE);
		mTextMax.setVisibility(mTextMax.getText().length() > 0 ? VISIBLE : GONE);
	}

	@Override
	public int getXOffset(float xpos) {
		return -(getWidth() / 2);
	}

	@Override
	public int getYOffset(float ypos) {
		return -getHeight();
	}

	private void clearTexts() {
		mTextMin.setText("");
		mTextAvg.setText("");
		mTextMax.setText("");
	}
}
