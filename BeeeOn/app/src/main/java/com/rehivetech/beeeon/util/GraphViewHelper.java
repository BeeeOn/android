package com.rehivetech.beeeon.util;

import android.content.Context;
import android.view.View;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.values.BaseEnumValue;
import com.rehivetech.beeeon.household.device.values.BaseValue;

import org.joda.time.format.DateTimeFormatter;

import java.util.List;

final public class GraphViewHelper {

	/**
	 * Private constructor to avoid instantiation.
	 */
	private GraphViewHelper() {
	};
	
	public static void prepareGraphView(final GraphView graphView, final Context context, final Module module, final DateTimeFormatter fmt, final UnitsHelper unitsHelper) {
		boolean isEnumValue = module.getValue() instanceof BaseEnumValue;
		
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
			BaseEnumValue value = (BaseEnumValue) module.getValue();
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
			final String unit = " "+unitsHelper.getStringUnit(module.getValue());
			graphView.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(context, "HH:mm","dd.MM.yy",unit));
		}

	}


	/**
	 * Preparation of skin for widget graph
	 * @param graphView
	 * @param context
	 * @param baseValue
	 * @param fmt
	 * @param unitsHelper
	 */
	public static void prepareWidgetGraphView(final GraphView graphView, final Context context, final BaseValue baseValue, final DateTimeFormatter fmt, final UnitsHelper unitsHelper) {
		float textSize = context.getResources().getDimension(R.dimen.textsize_caption);

		graphView.getGridLabelRenderer().setTextSize(textSize);
		graphView.getGridLabelRenderer().setVerticalLabelsColor(context.getResources().getColor(R.color.beeeon_text_hint));
		graphView.getGridLabelRenderer().setHorizontalLabelsColor(context.getResources().getColor(R.color.beeeon_text_hint));
		graphView.getGridLabelRenderer().setGridColor(context.getResources().getColor(R.color.beeeon_text_hint));

		boolean isEnumValue = baseValue instanceof BaseEnumValue;
		if (isEnumValue) {
			graphView.getViewport().setYAxisBoundsManual(true);
			graphView.getViewport().setMaxY(1.1d);
			BaseEnumValue value = (BaseEnumValue) baseValue;
			List<BaseEnumValue.Item> enumItems = value.getEnumItems();
			String[] verlabels = new String[enumItems.size()];
			int i = 0;
			for (BaseEnumValue.Item item : enumItems) {
				verlabels[i++] = context.getString(item.getStringResource());
			}
			DateAsXAxisLabelFormatter labelFormatter = new DateAsXAxisLabelFormatter(context,"HH:mm","dd.MM");
			StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graphView,labelFormatter);
			staticLabelsFormatter.setVerticalLabels(verlabels);
			graphView.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
		} else {
			final String unit = " " + unitsHelper.getStringUnit(baseValue);
			graphView.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(context, "HH:mm","dd.MM" ,unit));
		}
	}

}
