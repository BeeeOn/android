package com.rehivetech.beeeon.util;

import android.content.Context;
import android.view.View;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.adapter.device.Device;
import com.rehivetech.beeeon.adapter.device.values.BaseEnumValue;

import org.joda.time.format.DateTimeFormatter;

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
		graphView.setLoading(true);

		graphView.getViewport().setScalable(true);
		graphView.getViewport().setScrollable(true);
		graphView.setVisibility(View.VISIBLE);

		if (isEnumValue) {
			graphView.getGridLabelRenderer().setNumVerticalLabels(2);
		}

		final String unit = " "+unitsHelper.getStringUnit(device.getValue());
		graphView.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(context, "HH:mm","dd.MM.yy",unit));
	}

}
