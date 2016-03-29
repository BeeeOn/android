package com.rehivetech.beeeon.gui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.values.BaseValue;
import com.rehivetech.beeeon.util.UnitsHelper;
import com.rehivetech.beeeon.util.Utils;

/**
 * @author Martin Matejcik
 * @author Tomas Mlynaric
 * @since 8.12.2015
 */
@SuppressLint("ViewConstructor")
public class ModuleGraphMarkerView extends MarkerView {

	private LineChart mChart;

	private TextView mTextMin;
	private TextView mTextAvg;
	private TextView mTextMax;
	private TextView mTextXVal;

	private BaseValue mValueUnion;
	private UnitsHelper mUnitsHelper;

	/**
	 * Constructor. Sets up the MarkerView with a custom layout resource.
	 *
	 * @param context
	 * @param layoutResource
	 */
	public ModuleGraphMarkerView(Context context, int layoutResource, LineChart chart, Module module) {
		super(context, layoutResource);
		mChart = chart;

		mValueUnion = BaseValue.createFromModule(module);
		mUnitsHelper = Utils.getUnitsHelper(context);

		mTextMin = (TextView) findViewById(R.id.util_chart_markerview_text_min);
		mTextAvg = (TextView) findViewById(R.id.util_chart_markerview_text_avg);
		mTextMax = (TextView) findViewById(R.id.util_chart_markerview_text_max);
		mTextXVal = (TextView) findViewById(R.id.util_chart_markerview_text_xval);

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

	/**
	 * When changed position of marker
	 * @param e chart entry
	 * @param highlight
	 */
	@Override
	public void refreshContent(Entry e, Highlight highlight) {
		clearTexts();

		for (ILineDataSet dataSet : mChart.getData().getDataSets()) {
			if (dataSet.getLabel().contains("min")) {
				mValueUnion.setValue(String.valueOf(dataSet.getYValForXIndex(e.getXIndex())));
				mTextMin.setText(UnitsHelper.format(mUnitsHelper, mValueUnion));
			} else if (dataSet.getLabel().contains("avg")) {
				mValueUnion.setValue(String.valueOf(dataSet.getYValForXIndex(e.getXIndex())));
				mTextAvg.setText(UnitsHelper.format(mUnitsHelper, mValueUnion));
			} else if (dataSet.getLabel().contains("max")) {
				mValueUnion.setValue(String.valueOf(dataSet.getYValForXIndex(e.getXIndex())));
				mTextMax.setText(UnitsHelper.format(mUnitsHelper, mValueUnion));
			}
		}
		// sets x value (index)
		mTextXVal.setText(mChart.getXValue(e.getXIndex()));

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
