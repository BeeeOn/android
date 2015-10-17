package com.rehivetech.beeeon.util;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.FillFormatter;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.interfaces.LineDataProvider;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.view.ChartMarkerView;
import com.rehivetech.beeeon.household.device.values.BaseValue;
import com.rehivetech.beeeon.household.device.values.EnumValue;

import java.util.List;

final public class ChartHelper {

	/**
	 * Private constructor to avoid instantiation.
	 */
	private ChartHelper() {
	}

	/**
	 * Set chart params, legend and value formatter
	 *
	 * @param chart      chart instance
	 * @param context    context
	 * @param baseValue
	 * @param yLabels    StringBuffer to save long x labels in bar chart
	 * @param controller Controller instance
	 */
	public static void prepareChart(final BarLineChartBase chart, final Context context, BaseValue baseValue,
									StringBuffer yLabels, Controller controller, String dataUnit) {
		YAxisValueFormatter enumValueFormatter = getValueFormatterInstance(baseValue, context, controller);

		chart.getLegend().setEnabled(false);
		chart.setNoDataText(context.getString(R.string.chart_helper_chart_no_data));

		//TextView to get text color and typeface from textAppearance
		TextView tempText = new TextView(context);
		tempText.setTextAppearance(context, R.style.TextAppearance_AppCompat_Caption);

		chart.setDrawBorders(true);
		chart.setBorderColor(context.getResources().getColor(R.color.gray));
		chart.setDescription("");
		chart.setGridBackgroundColor(context.getResources().getColor(R.color.white));
		//set bottom X axis style
		XAxis xAxis = chart.getXAxis();
		xAxis.setAvoidFirstLastClipping(true);
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

		if (baseValue instanceof EnumValue) {
			final List<EnumValue.Item> labels = ((EnumValue) baseValue).getEnumItems();
			if (labels.size() > 2) {
				int j = 1;
				for (int i = labels.size() - 1; i > -1; i--) {
					yLabels.append(String.format("%d. %s\n", j++, context.getString(labels.get(i).getStringResource())));
				}
			} else {
				yAxis.setShowOnlyMinMax(true);
			}

			yAxis.setValueFormatter(enumValueFormatter);
			yAxis.setLabelCount(labels.size(), true);
			yAxis.setAxisMinValue(0);
			yAxis.setAxisMaxValue(labels.size() - 1);
			chart.setDoubleTapToZoomEnabled(false);
			chart.setScaleYEnabled(false);
		} else {
			ChartMarkerView markerView = new ChartMarkerView(context, R.layout.util_chart_helper_markerview, chart, dataUnit);
			chart.setMarkerView(markerView);
		}
	}

	/**
	 * Prepare data set for chart
	 *
	 * @param dataset  data set to prepare
	 * @param barChart bar chart flag
	 * @param filled   fill dataset flag
	 * @param color    line color
	 */
	public static void prepareDataSet(DataSet dataset, boolean barChart, boolean filled, @ColorInt int color, @ColorInt @Nullable Integer highlightColor) {
		int fillColor = Utils.setColorAlpha(color, 125);
		if (!barChart) {
			((LineDataSet) dataset).setDrawCircles(false);

			if (filled) {
				((LineDataSet) dataset).setDrawFilled(true);
				((LineDataSet) dataset).setFillColor(fillColor);
				((LineDataSet) dataset).setFillFormatter(new CustomFillFormatter());
				if (highlightColor != null) {
					((LineDataSet) dataset).setDrawHorizontalHighlightIndicator(false);
					((LineDataSet) dataset).setHighLightColor(highlightColor);
					((LineDataSet) dataset).setHighlightLineWidth(1f);
				} else {
					((LineDataSet) dataset).setDrawHighlightIndicators(false);
				}
			} else if (highlightColor == null){
				((LineDataSet) dataset).setDrawHighlightIndicators(false);
			}
		}
		dataset.setColor(color);
		dataset.setDrawValues(false);
	}

	/**
	 * Prepare ValueFormatter for bar and line chart
	 *
	 * @param baseValue
	 * @param context    Context
	 * @param controller Controller instance
	 * @return specific valueFormatter
	 */
	public static YAxisValueFormatter getValueFormatterInstance(final BaseValue baseValue, final Context context, Controller controller) {
		final UnitsHelper unitsHelper = new UnitsHelper(controller.getUserSettings(), context);
		if (baseValue instanceof EnumValue) {
			final List<EnumValue.Item> yLabels = ((EnumValue) baseValue).getEnumItems();
			if (yLabels.size() > 2) {
				return new YAxisValueFormatter() {
					@Override
					public String getFormattedValue(float value, YAxis yAxis) {
						return String.format("%.0f.", yLabels.size() - value);
					}
				};
			}
			return new YAxisValueFormatter() {
				@Override
				public String getFormattedValue(float value, YAxis yAxis) {
					return context.getString(yLabels.get((int) value).getStringResource());
				}
			};
		}
		return new YAxisValueFormatter() {
			@Override
			public String getFormattedValue(float value, YAxis yAxis) {
				return value + unitsHelper.getStringUnit(baseValue);
			}
		};
	}

	/**
	 * Custom fill formatter which allow fill chart from bottom
	 */
	private static class CustomFillFormatter implements FillFormatter {

		@Override
		public float getFillLinePosition(LineDataSet dataSet, LineDataProvider dataProvider) {
			return dataProvider.getAxis(YAxis.AxisDependency.LEFT).mAxisMinimum;
		}
	}
}
