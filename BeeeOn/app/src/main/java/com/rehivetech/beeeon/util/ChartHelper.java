package com.rehivetech.beeeon.util;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.utils.*;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.device.values.BaseEnumValue;
import com.rehivetech.beeeon.household.device.values.BaseValue;

import java.util.List;

final public class ChartHelper {

	/**
	 * Private constructor to avoid instantiation.
	 */
	private ChartHelper() {
	}

	/**
	 * Set chart params, legend and value formatter
	 * @param chart chart instance
	 * @param context context
	 * @param baseValue
	 * @param layout layout for adding textviews
	 * @param controller Controller instance
	 */
	public static void prepareChart(BarLineChartBase chart, final Context context, BaseValue baseValue, ViewGroup layout, Controller controller) {

		int padding = context.getResources().getDimensionPixelOffset(R.dimen.customview_text_padding);
		ValueFormatter enumValueFormatter = getValueFormatterInstance(baseValue, context, controller);

		chart.getLegend().setEnabled(false);

		//TextView to get text color and typeface from textAppearance
		TextView tempText = new TextView(context);
		tempText.setTextAppearance(context, R.style.TextAppearance_AppCompat_Caption);

		chart.setDrawBorders(true);
		chart.setBorderColor(context.getResources().getColor(R.color.gray));
		chart.setDescription("");
		chart.setHighlightEnabled(false);
		chart.setGridBackgroundColor(context.getResources().getColor(R.color.white));
		//set bottom X axis style
		XAxis xAxis = chart.getXAxis();
		xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
		xAxis.setAxisLineColor(context.getResources().getColor(R.color.beeeon_secondary_text));
		xAxis.setTextSize(Utils.convertPixelsToDp(tempText.getTextSize()));
		xAxis.setTypeface(tempText.getTypeface());
		xAxis.setTextColor(tempText.getCurrentTextColor());

		//set left Y axis style
		YAxis yAxis = chart.getAxisLeft();
		yAxis.setAxisLineColor(context.getResources().getColor(R.color.beeeon_secondary_text));
		yAxis.setStartAtZero(false);
		yAxis.setTextSize(Utils.convertPixelsToDp(tempText.getTextSize()));
		yAxis.setTypeface(tempText.getTypeface());
		yAxis.setTextColor(tempText.getCurrentTextColor());


		//disable right Y axis
		chart.getAxisRight().setEnabled(false);

		if (baseValue instanceof BaseEnumValue) {
			final List<BaseEnumValue.Item> yLabels = ((BaseEnumValue) baseValue).getEnumItems();
			if (yLabels.size() > 2) {
				if (layout.getVisibility() != View.VISIBLE) {
					int j = 1;
					TextView headline = new TextView(context);
					headline.setText(context.getString(R.string.chart_y_axis));
					headline.setTextAppearance(context, R.style.TextAppearance_AppCompat_Subhead);
					headline.setPadding(0, padding, 0, padding);
					layout.addView(headline);
					for (int i = yLabels.size() - 1; i > -1; i--) {
						TextView label = new TextView(context);
						label.setText(String.format("%d. %s", j++, context.getString(yLabels.get(i).getStringResource())));
						label.setTextAppearance(context, R.style.TextAppearance_AppCompat_Caption);
						layout.addView(label);
					}
				}
			} else {
				yAxis.setShowOnlyMinMax(true);
			}

			yAxis.setValueFormatter(enumValueFormatter);
			yAxis.setLabelCount(yLabels.size() - 1);
			yAxis.setAxisMinValue(0);
			yAxis.setAxisMaxValue(yLabels.size() - 1);
		}
	}

	/**
	 * Prepare ValueFormatter for bar and line chart
	 * @param baseValue
	 * @param context Context
	 * @param controller Controller instance
	 * @return specific valueFormatter
	 */
	public static ValueFormatter getValueFormatterInstance(final BaseValue baseValue, final Context context, Controller controller) {
		final UnitsHelper unitsHelper = new UnitsHelper(controller.getUserSettings(), context);
		if (baseValue instanceof BaseEnumValue) {
			final List<BaseEnumValue.Item> yLabels = ((BaseEnumValue) baseValue).getEnumItems();
			if (yLabels.size() > 2) {
				return new ValueFormatter() {
					@Override
					public String getFormattedValue(float value) {
						return String.format("%.0f.", yLabels.size() - value);
					}
				};
			}
			return new ValueFormatter() {

				@Override
				public String getFormattedValue(float value) {
					return context.getString(yLabels.get((int) value).getStringResource());
				}
			};
		}
		return new ValueFormatter() {
			@Override
			public String getFormattedValue(float value) {
				return value + unitsHelper.getStringUnit(baseValue);
			}
		};
	}
}
