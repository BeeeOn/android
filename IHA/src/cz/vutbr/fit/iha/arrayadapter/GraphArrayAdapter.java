package cz.vutbr.fit.iha.arrayadapter;

import java.util.List;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;

import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.device.DeviceLog;

public class GraphArrayAdapter extends ArrayAdapter<List<DeviceLog>> {

	private static final int MAX_GRAPH_DATA_COUNT = 500;
	
	private static final String TAG = GraphArrayAdapter.class.getSimpleName();
	
	private String mGraphDateTimeFormat = "dd.MM. kk:mm";
	
	private List<List<DeviceLog>> mLogs;
	private int mLayoutResource;
	//private int mDropDownLayoutResource;

	private LayoutInflater mInflater;

	public GraphArrayAdapter(Context context, int resource, List<List<DeviceLog>> objects) {
		super(context, resource, objects);
		mLayoutResource = resource;
		mLogs = objects;
	}

	public void setLayoutInflater(LayoutInflater inflater) {
		mInflater = inflater;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// LayoutInflater inflater = getLayoutInflater();
		View row = mInflater.inflate(mLayoutResource, parent, false);

		TextView label = (TextView) row.findViewById(R.id.custom_spinner_label);
		label.setText(String.valueOf(position)); // TODO: Name of type of devices
		
		LinearLayout graphLayout = (LinearLayout) row.findViewById(R.id.custom_spinner_graph);
		List<DeviceLog> logs = mLogs.get(position);
		addGraph(graphLayout, logs);

		return row;
	}
	
	
	private void addGraph(LinearLayout graphLayout, List<DeviceLog> logs) {
		GraphView graphView = new LineGraphView(getContext(), ""); // empty heading

		graphView.getGraphViewStyle().setTextSize(getContext().getResources().getDimension(R.dimen.textsizesmaller));
		graphView.getGraphViewStyle().setVerticalLabelsColor(getContext().getResources().getColor(R.color.iha_text_hint));
		graphView.getGraphViewStyle().setHorizontalLabelsColor(getContext().getResources().getColor(R.color.iha_text_hint));
		// mGraphView.getGraphViewStyle().setVerticalLabelsWidth(60);
		// mGraphView.getGraphViewStyle().setNumHorizontalLabels(2);
		graphView.setBackgroundColor(getContext().getResources().getColor(R.color.alpha_blue));// getResources().getColor(R.color.log_blue2));

		((LineGraphView) graphView).setDrawBackground(true);
		// graphView.setAlpha(128);

		
		int seriesId = 0;
		for (DeviceLog log : logs) {
		
			GraphViewSeriesStyle seriesStyleBlue = new GraphViewSeriesStyle(getContext().getResources().getColor(R.color.iha_primary_cyan), 2);
			// GraphViewSeriesStyle seriesStyleGray = new GraphViewSeriesStyle(getResources().getColor(R.color.light_gray),2);
	
			GraphViewSeries graphSeries = new GraphViewSeries("Graph"/* + String.valueOf(seriesId++)*/, seriesStyleBlue, new GraphViewData[] { new GraphView.GraphViewData(0, 0), });
			graphView.addSeries(graphSeries);
			
			
			// NOTE: This formatter is only for Log, correct timezone from app setting doesn't matter here
			final DateTimeFormatter fmt = DateTimeFormat.forPattern(mGraphDateTimeFormat); 

			int size = log.getValues().size();
			// Log.d(TAG, String.format("Filling graph with %d values. Min: %.1f, Max: %.1f", size, log.getMinimum(), log.getMaximum()));

			int begin;
			GraphView.GraphViewData[] data;

			// Limit amount of showed values
			if (size > MAX_GRAPH_DATA_COUNT) {
				data = new GraphView.GraphViewData[MAX_GRAPH_DATA_COUNT];
				begin = (size - MAX_GRAPH_DATA_COUNT);
			} else {
				data = new GraphView.GraphViewData[size];
				begin = 0;
			}

			for (int i = begin; i < size; i++) {
				DeviceLog.DataRow row = log.getValues().get(i);
				
				float value = Float.isNaN(row.value) ? log.getMinimum() : row.value;
				data[i - begin] = new GraphView.GraphViewData(row.dateMillis, value);
				//Log.v(TAG, String.format("Graph value: date(msec): %s, Value: %.1f", fmt.print(row.dateMillis), row.value));
			}

			// Set maximum as +10% more than deviation
			graphView.setManualYAxisBounds(log.getMaximum() + log.getDeviation() * 0.1, log.getMinimum());
			// mGraphView.setViewPort(0, 7);
			graphSeries.resetData(data);
			//mGraphInfo.setText(getView().getResources().getString(R.string.sen_detail_graph_info));
			
			break;
		}
		
		graphView.setManualYAxisBounds(1.0, 0.0);
		
/*		graphView.setCustomLabelFormatter(new CustomLabelFormatter() {
			final String unit = unitsHelper.getStringUnit(mDevice.getValue());
			
			@Override
			public String formatLabel(double value, boolean isValueX) {
				if (isValueX) {
					return fmt.print((long) value);
				}

				return String.format("%s %s", unitsHelper.getStringValue(mDevice.getValue(), value), unit);
			}
		});

		graphLayout.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// Disable graph if in edit Mode
				if (mEditMode != EDIT_NONE)
					return false;

				if (mWasTapGraph)
					return true;

				mWasTapLayout = false;
				mWasTapGraph = true;

				Log.d(TAG, "onTouch layout");
				graphView.setScrollable(true);
				graphView.setScalable(true);
				mActivity.setEnableSwipe(false);
				mGraphInfo.setVisibility(View.GONE);
				onTouch(v, event);
				return true;
			}
		}); */
		
		graphLayout.addView(graphView);
	}

}
