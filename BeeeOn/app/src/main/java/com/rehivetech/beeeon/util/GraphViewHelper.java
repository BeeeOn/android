package com.rehivetech.beeeon.util;

import android.content.Context;
import android.view.View;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.values.BaseEnumValue;

import org.joda.time.format.DateTimeFormatter;

import java.util.List;

final public class GraphViewHelper {

	/**
	 * Private constructor to avoid instantiation.
	 */
	private GraphViewHelper() {
	};
	
	public static void prepareGraphView(final GraphView graphView, final Context context, final Device device, final DateTimeFormatter fmt, final UnitsHelper unitsHelper) {
		boolean isEnumValue = device.getValue() instanceof BaseEnumValue;
		
		graphView.setTitleTextSize(context.getResources().getDimension(R.dimen.textsizesmaller));
		graphView.getGridLabelRenderer().setTextSize(context.getResources().getDimension(R.dimen.textsizesmaller));
		graphView.getGridLabelRenderer().setVerticalLabelsColor(context.getResources().getColor(R.color.beeeon_text_hint));
		graphView.getGridLabelRenderer().setHorizontalLabelsColor(context.getResources().getColor(R.color.beeeon_text_hint));
		graphView.getGridLabelRenderer().setGridColor(context.getResources().getColor(R.color.beeeon_text_hint));
		graphView.getPointer().setColor(context.getResources().getColor(R.color.beeeon_secundary_pink));
		graphView.getPointer().setTextColor(context.getResources().getColor(R.color.white));

		graphView.setLoading(true);
		graphView.setDrawPointer(true);

		graphView.getViewport().setScalable(true);
		graphView.getViewport().setScrollable(true);
		graphView.setVisibility(View.VISIBLE);

		if (isEnumValue) {
			graphView.getViewport().setYAxisBoundsManual(true);
			graphView.getViewport().setMaxY(1.1d);
			BaseEnumValue value = (BaseEnumValue) device.getValue();
			List<BaseEnumValue.Item> enumItems = value.getEnumItems();
			String[] verlabels = new String[enumItems.size()];
			int i = 0;
			for (BaseEnumValue.Item item : enumItems) {
				verlabels[i++] = context.getString(item.getStringResource());
			}
			DateAsXAxisLabelFormatter labelFormatter = new DateAsXAxisLabelFormatter(context,"HH:mm","dd.MM.yy");
			StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graphView,labelFormatter);
			staticLabelsFormatter.setVerticalLabels(verlabels);
			graphView.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
		} else {
			final String unit = " "+unitsHelper.getStringUnit(device.getValue());
			graphView.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(context, "HH:mm","dd.MM.yy",unit));
		}

	}


	public static void prepareWidgetGraphView(final GraphView graphView, final Context context, final Device device, final DateTimeFormatter fmt, final UnitsHelper unitsHelper) {
		boolean isEnumValue = device.getValue() instanceof BaseEnumValue;

		graphView.setTitleTextSize(20);
		graphView.getGridLabelRenderer().setTextSize(20);
		graphView.getGridLabelRenderer().setVerticalLabelsColor(context.getResources().getColor(R.color.beeeon_text_hint));
		graphView.getGridLabelRenderer().setHorizontalLabelsColor(context.getResources().getColor(R.color.beeeon_text_hint));
		graphView.getGridLabelRenderer().setGridColor(context.getResources().getColor(R.color.beeeon_text_hint));

		if (isEnumValue) {
			graphView.getViewport().setYAxisBoundsManual(true);
			graphView.getViewport().setMaxY(1.1d);
			BaseEnumValue value = (BaseEnumValue) device.getValue();
			List<BaseEnumValue.Item> enumItems = value.getEnumItems();
			String[] verlabels = new String[enumItems.size()];
			int i = 0;
			for (BaseEnumValue.Item item : enumItems) {
				verlabels[i++] = context.getString(item.getStringResource());
			}
			DateAsXAxisLabelFormatter labelFormatter = new DateAsXAxisLabelFormatter(context,"HH:mm","dd.MM.yy");
			StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graphView,labelFormatter);
			staticLabelsFormatter.setVerticalLabels(verlabels);
			graphView.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
		} else {
			final String unit = " "+unitsHelper.getStringUnit(device.getValue());
			graphView.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(context, "HH:mm","dd.MM.yy",unit));
		}
	}

}
