package com.rehivetech.beeeon.util;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.utils.ValueFormatter;
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

		ValueFormatter enumValueFormatter = getValueFormatterInstance(baseValue, context, controller);

		Legend legend = chart.getLegend();
		legend.setForm(Legend.LegendForm.CIRCLE);

		chart.setDrawBorders(true);
		chart.setBorderColor(context.getResources().getColor(R.color.beeeon_separator));
		chart.setDescription("");
		chart.setHighlightEnabled(false);

		//set bottom X axis style
		XAxis xAxis = chart.getXAxis();
		xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
		xAxis.setAxisLineColor(context.getResources().getColor(R.color.beeeon_text_hint));

		//set left Y axis style
		YAxis yAxis = chart.getAxisLeft();
		yAxis.setAxisLineColor(context.getResources().getColor(R.color.beeeon_text_hint));
		yAxis.setStartAtZero(false);

		//disable right Y axis
		chart.getAxisRight().setEnabled(false);

		if (baseValue instanceof BaseEnumValue) {
			final List<BaseEnumValue.Item> yLabels = ((BaseEnumValue) baseValue).getEnumItems();
			if (yLabels.size() > 2) {
				if (layout.getVisibility() != View.VISIBLE) {
					int j = 1;
					for (int i = yLabels.size() - 1; i > -1; i--) {
						TextView label = new TextView(context);
						label.setText(String.format("%d. %s", j++, context.getString(yLabels.get(i).getStringResource())));
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
