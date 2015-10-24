package com.rehivetech.beeeon.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.view.MotionEvent;

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
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.rehivetech.beeeon.R;
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
	@SuppressLint("PrivateResource")
	public static void prepareChart(final BarLineChartBase chart, final Context context, BaseValue baseValue,
									StringBuffer yLabels, MarkerView markerView) {
		YAxisValueFormatter yAxisValueFormatter = getValueFormatterInstance(baseValue, context);

		chart.getLegend().setEnabled(false);
		chart.setNoDataText(context.getString(R.string.chart_helper_chart_no_data));

		//TextView to get text color and typeface from textAppearance
		AppCompatTextView tempText = new AppCompatTextView(context);
		tempText.setTextAppearance(context, R.style.TextAppearance_AppCompat_Caption);

		//set paint when no chart data is avaiable
		Paint paint = chart.getPaint(Chart.PAINT_INFO);
		paint.setColor(tempText.getCurrentTextColor());
		paint.setTypeface(tempText.getTypeface());
		paint.setTextSize(tempText.getTextSize());

		chart.setDrawBorders(true);
		chart.setBorderColor(ContextCompat.getColor(context, R.color.gray));

		chart.setDescription("");

		chart.setGridBackgroundColor(ContextCompat.getColor(context, R.color.white));
		//set bottom X axis style
		XAxis xAxis = chart.getXAxis();
		xAxis.setAvoidFirstLastClipping(true);
		xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
		xAxis.setAxisLineColor(ContextCompat.getColor(context, R.color.beeeon_secondary_text));
		xAxis.setTextSize(Utils.convertPixelsToDp(tempText.getTextSize()));
		xAxis.setTypeface(tempText.getTypeface());
		xAxis.setTextColor(tempText.getCurrentTextColor());

		//set left Y axis style
		YAxis yAxis = chart.getAxisLeft();
		yAxis.setAxisLineColor(ContextCompat.getColor(context, R.color.beeeon_secondary_text));
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

		//set max visible values count by screen size
		chart.setMaxVisibleValueCount(context.getResources().getInteger(R.integer.graph_values_count));

		final ViewPortHandler viewPortHandler = chart.getViewPortHandler();
		final int chartNumOfCircles = context.getResources().getInteger(R.integer.graph_number_circles);

		chart.setOnChartGestureListener(new OnChartGestureListener() {
			@Override
			public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

			}

			@Override
			public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

			}

			@Override
			public void onChartLongPressed(MotionEvent me) {

			}

			@Override
			public void onChartDoubleTapped(MotionEvent me) {

			}

			@Override
			public void onChartSingleTapped(MotionEvent me) {

			}

			@Override
			public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

			}

			@Override
			public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
				int yValuesCount = chart.getData().getYValCount();
				DataSet dataSet = (DataSet) chart.getData().getDataSets().get(0);

				if (yValuesCount / viewPortHandler.getScaleX() < chartNumOfCircles) {

					if (dataSet instanceof LineDataSet && !((LineDataSet) dataSet).isDrawCirclesEnabled()) {
						((LineDataSet) chart.getData().getDataSets().get(0)).setDrawCircles(true);
					}
				} else {

					if (dataSet instanceof LineDataSet && ((LineDataSet) dataSet).isDrawCirclesEnabled()) {
						((LineDataSet) chart.getData().getDataSets().get(0)).setDrawCircles(false);
					}
				}
			}

			@Override
			public void onChartTranslate(MotionEvent me, float dX, float dY) {

			}
		});
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
	@SuppressLint("PrivateResource")
	public static void prepareDataSet(Context context, DataSet dataset, boolean barChart, boolean filled,
									  @ColorInt int color, @ColorInt int highlightColor) {
		int fillColor = Utils.setColorAlpha(color, 125);

		dataset.setDrawValues(false);

		if (!barChart) {
			((LineDataSet) dataset).setDrawCircles(false);
			((LineDataSet) dataset).setCircleColor(color);
			((LineDataSet) dataset).setCircleSize(Utils.convertDpToPixel(2));

			dataset.setDrawValues(true);

			AppCompatTextView tempText = new AppCompatTextView(context);
			tempText.setTextAppearance(context, R.style.TextAppearance_AppCompat_Caption);

			dataset.setValueTextColor(tempText.getCurrentTextColor());
			dataset.setValueTextSize(Utils.convertPixelsToDp(tempText.getTextSize()));
			dataset.setValueTypeface(tempText.getTypeface());

			if (filled) {
				((LineDataSet) dataset).setDrawFilled(true);
				((LineDataSet) dataset).setFillColor(fillColor);
				((LineDataSet) dataset).setFillFormatter(new CustomFillFormatter());
				((LineDataSet) dataset).setHighLightColor(highlightColor);
			} else {
				((LineDataSet) dataset).setHighLightColor(highlightColor);
			}
		}
		dataset.setColor(color);
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

				if (value % 1 == 0) {
					return String.format("%.0f", value);
				}
				return String.format("%.1f", value);
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
