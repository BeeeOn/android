package com.rehivetech.beeeon.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.MotionEvent;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.FillFormatter;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.activity.BaseApplicationActivity;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.ModuleLog;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.household.device.values.BaseValue;
import com.rehivetech.beeeon.household.device.values.EnumValue;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.task.GetModuleLogTask;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormatter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

final public class ChartHelper {
	private static final String TAG = ChartHelper.class.getSimpleName();

	public static final String GRAPH_DATE_TIME_FORMAT = "dd.MM. HH:mm";

	@IntDef(value = {RANGE_HOUR, RANGE_DAY, RANGE_WEEK, RANGE_MONTH})
	@Retention(RetentionPolicy.CLASS)

	public @interface DataRange {
		int[] values() default {};
	}

	public static final int RANGE_HOUR = 60 * 60;
	public static final int RANGE_DAY = RANGE_HOUR * 24;
	public static final int RANGE_WEEK = RANGE_DAY * 7;
	public static final int RANGE_MONTH = RANGE_WEEK * 4;


	public static int[] ALL_RANGES = {RANGE_HOUR, RANGE_DAY, RANGE_WEEK, RANGE_MONTH};

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
	public static void prepareChart(final BarLineChartBase chart, final Context context, BaseValue baseValue, StringBuffer yLabels,
									@Nullable MarkerView markerView, boolean drawBorders) {

		chart.getLegend().setEnabled(false);
		chart.setNoDataText(context.getString(R.string.chart_helper_chart_no_data));

		//TextView to get text color and typeface from textAppearance
		AppCompatTextView tempText = new AppCompatTextView(context);
		tempText.setTextAppearance(context, R.style.TextAppearance_AppCompat_Body1);

		//set paint when no chart data is avaiable
		Paint paint = chart.getPaint(Chart.PAINT_INFO);
		paint.setColor(tempText.getCurrentTextColor());
		paint.setTypeface(tempText.getTypeface());
		paint.setTextSize(tempText.getTextSize());

		chart.setDrawBorders(drawBorders);
		chart.setBorderColor(ContextCompat.getColor(context, R.color.gray));

		chart.setDescription("");

		chart.setGridBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));

		if (baseValue instanceof EnumValue) {
			final List<EnumValue.Item> labels = ((EnumValue) baseValue).getEnumItems();
			if (labels.size() > 2) {
				int j = 1;
				for (int i = labels.size() - 1; i > -1; i--) {
					yLabels.append(String.format("%d. %s\n", j++, context.getString(labels.get(i).getStringResource())));
				}
			}

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
			@SuppressWarnings("unchecked")
			public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
				int yValuesCount = chart.getData().getYValCount();
				List<DataSet> dataSets = (List<DataSet>)chart.getData().getDataSets();


				for (DataSet dataSet : dataSets) {
					setDataSetCircles(dataSet, viewPortHandler, yValuesCount, chartNumOfCircles);
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
	 * @param dataset        data set to prepare
	 * @param barChart       bar chart flag
	 * @param filled         fill dataset flag
	 * @param color          line color
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
			((LineDataSet) dataset).setCircleSize(4f);

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

	public static void setDataSetCircles(DataSet dataSet, ViewPortHandler viewPortHandler, int yValuesCount, int chartNumOfCircles) {
		if (yValuesCount / viewPortHandler.getScaleX() < chartNumOfCircles) {

			if (dataSet instanceof LineDataSet && !((LineDataSet) dataSet).isDrawCirclesEnabled()) {
				((LineDataSet) dataSet).setDrawCircles(true);
			}
		} else {

			if (dataSet instanceof LineDataSet && ((LineDataSet) dataSet).isDrawCirclesEnabled()) {
				((LineDataSet) dataSet).setDrawCircles(false);
			}
		}
	}

	@SuppressLint("PrivateResource")
	public static void prepareXAxis(Context context, XAxis axis, @ColorInt Integer textColor,
									XAxis.XAxisPosition position, boolean drawGridLines) {

		//TextView to get text color and typeface from textAppearance
		AppCompatTextView tempText = new AppCompatTextView(context);
		tempText.setTextAppearance(context, R.style.TextAppearance_AppCompat_Caption);

		axis.setAvoidFirstLastClipping(true);
		axis.setPosition(position);
		axis.setAxisLineColor(ContextCompat.getColor(context, R.color.beeeon_secondary_text));
		axis.setTextSize(Utils.convertPixelsToDp(tempText.getTextSize()));
		axis.setTypeface(tempText.getTypeface());
		axis.setTextColor((textColor != null) ? textColor : tempText.getCurrentTextColor());
		axis.setDrawGridLines(drawGridLines);
	}

	@SuppressLint("PrivateResource")
	public static void prepareYAxis(Context context, BaseValue baseValue, YAxis axis, @ColorInt Integer textColor,
									YAxis.YAxisLabelPosition position, boolean drawGridLines, boolean drawAxisLine) {

		YAxisValueFormatter yAxisValueFormatter = getValueFormatterInstance(baseValue, context);
		//TextView to get text color and typeface from textAppearance
		AppCompatTextView tempText = new AppCompatTextView(context);
		tempText.setTextAppearance(context, R.style.TextAppearance_AppCompat_Caption);

		axis.setAxisLineColor(ContextCompat.getColor(context, R.color.beeeon_secondary_text));
		axis.setStartAtZero(false);
		axis.setTextSize(Utils.convertPixelsToDp(tempText.getTextSize()));
		axis.setTypeface(tempText.getTypeface());
		axis.setTextColor((textColor != null) ? textColor : tempText.getCurrentTextColor());
		axis.setValueFormatter(yAxisValueFormatter);
		axis.setPosition(position);
		axis.setDrawGridLines(drawGridLines);
		axis.setDrawAxisLine(drawAxisLine);

		if (baseValue instanceof EnumValue) {
			final List<EnumValue.Item> labels = ((EnumValue) baseValue).getEnumItems();

			if (labels.size() <= 2) {
				axis.setShowOnlyMinMax(true);
			}

			axis.setLabelCount(labels.size(), true);
			axis.setAxisMinValue(0);
			axis.setAxisMaxValue(labels.size() - 1);
		}
	}


	/**
	 * Prepare ValueFormatter for bar and line chart
	 *
	 * @param baseValue module baseValue
	 * @param context   Context
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
	 * @param activity     instance of activity
	 * @param controller   instance of controller
	 * @param dataSet      instance of chart dataSet
	 * @param gateId       ID of gate
	 * @param deviceId     ID of device
	 * @param moduleId     ID of module
	 * @param range        time range to be displayed
	 * @param dataType     type of data (AVG, MIN, MAX)
	 * @param dataInterval interval of values
	 */
	public static <T extends DataSet>
	void loadChartData(final BaseApplicationActivity activity, final Controller controller, final T dataSet, final String gateId, String deviceId,
					   String moduleId, @DataRange int range, final ModuleLog.DataType dataType,
					   final ModuleLog.DataInterval dataInterval, final ChartLoadListener callback, final DateTimeFormatter formatter) {

		final List<String> xValues = new ArrayList<>();
		final Module module = controller.getDevicesModel().getDevice(gateId, deviceId).getModuleById(moduleId);

		if (module == null) {
			return;
		}

		// FIXME: Better hold these in parent fragment/activity and have it same (and not duplicit on more places) for all of htem
		DateTime end = DateTime.now(DateTimeZone.UTC);
		DateTime start = end.minusSeconds(range);

		GetModuleLogTask getModuleLogTask = new GetModuleLogTask(activity);

		final ModuleLog.DataPair dataPair = new ModuleLog.DataPair(module, new Interval(start, end), dataType, dataInterval);
		getModuleLogTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				ModuleLog moduleLog = Controller.getInstance(activity).getModuleLogsModel().getModuleLog(dataPair);

				fillDataSet(moduleLog, dataSet, xValues, dataPair, formatter);
				callback.onChartLoaded(dataSet, xValues);
			}
		});

		activity.callbackTaskManager.executeTask(getModuleLogTask, dataPair);
	}

	/**
	 * @param moduleLog data to be displayed
	 * @param dataSet chart dataSet
	 * @param xValues chart xValues
	 * @param pair
	 */
	private static <T extends DataSet> void fillDataSet(ModuleLog moduleLog, T dataSet, List<String> xValues, ModuleLog.DataPair pair, DateTimeFormatter formatter) {
		boolean barChart = (dataSet instanceof BarDataSet);
		SortedMap<Long, Float> values = moduleLog.getValues();

		Log.d(TAG, String.format("Filling graph with %d values. Min: %.1f, Max: %.1f, dataType: %s, dataInterval: %s", values.size(), moduleLog.getMinimum(), moduleLog.getMaximum(), moduleLog.getType().toString(), moduleLog.getInterval().toString()));

		dataSet.clear();

		long start = pair.interval.getStartMillis();
		long end = pair.interval.getEndMillis();

		RefreshInterval refresh = pair.module.getDevice().getRefresh();

		// use refresh interval for raw data, or 5 min / 1 hour when device has no refresh
		int refreshMsecs;

		if (refresh == null) {
			long interval = end - start;
			if (interval == RANGE_WEEK * 1000) {
				refreshMsecs = 1000 * 60 * 5;
			} else if (interval == (long) RANGE_MONTH * 1000) {
				refreshMsecs = 1000 * 60 * 60;
			} else {
				refreshMsecs = 1000;
			}
		} else {
			refreshMsecs = refresh.getInterval() * 1000;
		}

		long everyMsecs = pair.gap == ModuleLog.DataInterval.RAW ? refreshMsecs : pair.gap.getSeconds() * 1000;

		Log.d(TAG, String.format("Computing %d values", (end - start) / everyMsecs));

		int i = 0;
		for (Map.Entry<Long, Float> entry : values.entrySet()) {
			long time = entry.getKey();
			float value = entry.getValue();

			// Fill missing data between start (or previous timestamp) and timestamp of given value
			while ((start + everyMsecs) <= time && start < end) {
				xValues.add(formatter.print(start));
				start += everyMsecs;
				i++;
			}

			if (start >= end) {
				break;
			}

			xValues.add(formatter.print(time));

			if (Float.isNaN(value)) {
				continue;
			}

			if (barChart) {
				dataSet.addEntry(new BarEntry(value, i++));
			} else {
				dataSet.addEntry(new Entry(value, i++));
			}

			start += everyMsecs;
		}

		// Fill missing data between last given value and end of interval
		while ((start + everyMsecs) <= end) {
			xValues.add(formatter.print(start));
			start += everyMsecs;
			i++;
		}
	}

	/**
	 * @param interval Data range int interval
	 * @return interval string representation
	 */
	public static
	@StringRes
	int getIntervalString(@ChartHelper.DataRange int interval) {
		switch (interval) {
			case ChartHelper.RANGE_HOUR:
				return R.string.graph_range_hour;
			case ChartHelper.RANGE_DAY:
				return R.string.graph_range_day;
			case ChartHelper.RANGE_WEEK:
				return R.string.graph_range_week;
			case ChartHelper.RANGE_MONTH:
				return R.string.graph_range_month;
		}
		return -1;
	}

	/**
	 * Custom fill formatter which allow fill chart from bottom
	 */
	private static class CustomFillFormatter implements FillFormatter {

		@Override
		public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
			return dataProvider.getAxis(YAxis.AxisDependency.LEFT).mAxisMinimum;
		}
	}


	/**
	 * Callback interface for load chart data
	 */
	public interface ChartLoadListener {
		void onChartLoaded(DataSet dataset, List<String> xValues);
	}
}
