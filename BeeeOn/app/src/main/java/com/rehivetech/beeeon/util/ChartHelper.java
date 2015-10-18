package com.rehivetech.beeeon.util;

import android.content.Context;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.FillFormatter;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.interfaces.LineDataProvider;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
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
	 * @param baseValue  module baseValue
	 * @param yLabels    StringBuffer to save long x labels in bar chart
	 * @param markerView chart markerView instance
	 */
	public static void prepareChart(final BarLineChartBase chart, final Context context, BaseValue baseValue,
									StringBuffer yLabels, MarkerView markerView, String valuesUnit) {
		YAxisValueFormatter yAxisValueFormatter = getValueFormatterInstance(baseValue, context);

		chart.getLegend().setEnabled(false);
		chart.setNoDataText(context.getString(R.string.chart_helper_chart_no_data));

		//TextView to get text color and typeface from textAppearance
		TextView tempText = new TextView(context);
		tempText.setTextAppearance(context, R.style.TextAppearance_AppCompat_Caption);

		//set paint when no chart data is avaiable
		Paint paint = chart.getPaint(Chart.PAINT_INFO);
		paint.setColor(tempText.getCurrentTextColor());
		paint.setTypeface(tempText.getTypeface());
		paint.setTextSize(tempText.getTextSize());

		chart.setDrawBorders(true);
		chart.setBorderColor(context.getResources().getColor(R.color.gray));

		//set chart description as values unit
		chart.setDescription(valuesUnit);
		chart.setDescriptionColor(tempText.getCurrentTextColor());
		tempText.setTextAppearance(context, R.style.TextAppearance_AppCompat_Subhead);
		chart.setDescriptionTextSize(tempText.getTextSize());
		chart.setDescriptionTypeface(tempText.getTypeface());



		chart.setGridBackgroundColor(context.getResources().getColor(R.color.white));
		//set bottom X axis style
		XAxis xAxis = chart.getXAxis();
		xAxis.setAvoidFirstLastClipping(true);
		xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
		xAxis.setAxisLineColor(context.getResources().getColor(R.color.beeeon_secondary_text));
		tempText.setTextAppearance(context, R.style.TextAppearance_AppCompat_Caption);
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
		yAxis.setValueFormatter(yAxisValueFormatter);

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

			yAxis.setLabelCount(labels.size(), true);
			yAxis.setAxisMinValue(0);
			yAxis.setAxisMaxValue(labels.size() - 1);
			chart.setDoubleTapToZoomEnabled(false);
			chart.setScaleYEnabled(false);
		} else {
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
	 * @param highlightColor color for highlight lines
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
				} else {
					((LineDataSet) dataset).setDrawHighlightIndicators(false);
				}
			} else if (highlightColor == null){
				((LineDataSet) dataset).setDrawHighlightIndicators(false);
			} else {
				((LineDataSet) dataset).setHighLightColor(highlightColor);
			}
		}
		dataset.setColor(color);
		dataset.setDrawValues(false);
	}

	/**
	 * Prepare ValueFormatter for bar and line chart
	 *
	 * @param baseValue module baseValue
	 * @param context    Context
	 * @return specific valueFormatter
	 */
	public static YAxisValueFormatter getValueFormatterInstance(final BaseValue baseValue, final Context context) {
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
				if (value == 0)
					value = 0;
				return String.format("%.1f", value);
			}
		};
	}

	/**
	 * Set description style in chart
	 * @param chart chart instance
	 */
	public static void setChartDescription(Chart chart, boolean customView) {
		ViewPortHandler viewPortHandler = chart.getViewPortHandler();
		float posX = chart.getWidth() - viewPortHandler.offsetRight() - 10f;
		float posY = chart.getTop() + viewPortHandler.offsetTop() + 10f;
		if (customView) {
			posY += 20f;
		}
		chart.setDescriptionPosition(posX, posY);
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
