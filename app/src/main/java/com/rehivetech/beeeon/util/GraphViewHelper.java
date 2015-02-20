package com.rehivetech.beeeon.util;

import java.util.List;

import org.joda.time.format.DateTimeFormatter;

import android.content.Context;
import android.view.View;

import com.jjoe64.graphview.BarGraphView;
import com.jjoe64.graphview.CustomLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LineGraphView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.adapter.device.Device;
import com.rehivetech.beeeon.adapter.device.values.BaseEnumValue;
import com.rehivetech.beeeon.adapter.device.values.BaseEnumValue.Item;

final public class GraphViewHelper {

	/**
	 * Private constructor to avoid instantiation.
	 */
	private GraphViewHelper() {
	};
	
	public static GraphView prepareGraphView(final Context context, final String title, final Device device, final DateTimeFormatter fmt, final UnitsHelper unitsHelper) {
		boolean isEnumValue = device.getValue() instanceof BaseEnumValue;
		
		GraphView graphView = isEnumValue ? new BarGraphView(context, title) : new LineGraphView(context, title);
		
		graphView.getGraphViewStyle().setTextSize(context.getResources().getDimension(R.dimen.textsizesmaller));
		graphView.getGraphViewStyle().setVerticalLabelsColor(context.getResources().getColor(R.color.iha_text_hint));
		graphView.getGraphViewStyle().setHorizontalLabelsColor(context.getResources().getColor(R.color.iha_text_hint));
		// mGraphView.getGraphViewStyle().setVerticalLabelsWidth(60);
		// mGraphView.getGraphViewStyle().setNumHorizontalLabels(2);
		// graphView.setBackgroundColor(mContext.getResources().getColor(R.color.alpha_blue));// getResources().getColor(R.color.log_blue2));

		graphView.setScalable(true);
		graphView.setScrollable(true);

		graphView.setVisibility(View.VISIBLE);
		// graphView.setAlpha(128);

		if (isEnumValue) {
			// Use special Y-axis for BaseEnumValue type
			BaseEnumValue value = (BaseEnumValue) device.getValue();
			List<Item> enumItems = value.getEnumItems();
			String[] verlabels = new String[enumItems.size()];
			
			int i = enumItems.size();
			for (Item item : enumItems) {
				verlabels[--i] = context.getString(item.getStringResource());
			}

			//graphView.getGraphViewStyle().setNumVerticalLabels(enumItems.size() + 1);
			graphView.setVerticalLabels(verlabels);
			
			graphView.setManualYAxis(true);
			graphView.setManualYAxisBounds(enumItems.size() - 1, 0);
		}
		
		graphView.setCustomLabelFormatter(new CustomLabelFormatter() {
			final String unit = unitsHelper.getStringUnit(device.getValue());

			@Override
			public String formatLabel(double value, boolean isValueX) {
				if (isValueX) {
					return fmt.print((long) value);
				}

				return String.format("%s %s", unitsHelper.getStringValue(device.getValue(), value), unit);
			}
		});
		
		return graphView;
	}

}
