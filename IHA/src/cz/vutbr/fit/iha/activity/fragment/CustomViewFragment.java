package cz.vutbr.fit.iha.activity.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.jjoe64.graphview.CustomLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;

import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.activity.MainActivity;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.adapter.device.Device;
import cz.vutbr.fit.iha.adapter.device.DeviceLog;
import cz.vutbr.fit.iha.adapter.device.DeviceLog.DataInterval;
import cz.vutbr.fit.iha.adapter.device.DeviceLog.DataType;
import cz.vutbr.fit.iha.adapter.device.DeviceType;
import cz.vutbr.fit.iha.adapter.device.Facility;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.pair.LogDataPair;
import cz.vutbr.fit.iha.util.Log;
import cz.vutbr.fit.iha.util.UnitsHelper;

public class CustomViewFragment extends SherlockFragment {
	
	private MainActivity mActivity;
	
	private SparseArray<List<Device>> mDevices = new SparseArray<List<Device>>();
	private SparseArray<List<DeviceLog>> mLogs = new SparseArray<List<DeviceLog>>();
	private SparseArray<GraphView> mGraphs = new SparseArray<GraphView>();
	
	private String mGraphDateTimeFormat = "dd.MM. kk:mm";
	
	private LinearLayout mLayout;
	
	private static final String TAG = CustomViewFragment.class.getSimpleName();
	
	private Context mContext;
	private Controller mController;
	
	
	public CustomViewFragment(MainActivity context) {
		mActivity = context;
		mController = Controller.getInstance(mActivity.getApplicationContext());
	}
	public CustomViewFragment() {}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.graphofsensors, container, false);
		
		if (mContext == null) {
			mContext = getActivity().getApplicationContext();
		}
		
		mLayout = (LinearLayout) view.findViewById(R.id.container);

		prepareDevices();
		
		return view;
	}

	
	private void addGraph(final Device device) {
		LayoutInflater inflater = getLayoutInflater(null);
		
		//LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View row = inflater.inflate(R.layout.custom_graph_item, mLayout, false);

		LinearLayout graphLayout = (LinearLayout)row.findViewById(R.id.graph_layout);		
		
		TextView tv = (TextView)row.findViewById(R.id.graph_label);
		tv.setText(mContext.getString(device.getType().getStringResource()));
		
		LineGraphView graphView = new LineGraphView(mContext, ""); // empty heading

		graphView.getGraphViewStyle().setTextSize(mContext.getResources().getDimension(R.dimen.textsizesmaller));
		graphView.getGraphViewStyle().setVerticalLabelsColor(mContext.getResources().getColor(R.color.iha_text_hint));
		graphView.getGraphViewStyle().setHorizontalLabelsColor(mContext.getResources().getColor(R.color.iha_text_hint));
		// mGraphView.getGraphViewStyle().setVerticalLabelsWidth(60);
		// mGraphView.getGraphViewStyle().setNumHorizontalLabels(2);
		//graphView.setBackgroundColor(mContext.getResources().getColor(R.color.alpha_blue));// getResources().getColor(R.color.log_blue2));

		graphView.setShowLegend(true);
		
		graphView.setDrawBackground(false);
		graphView.setVisibility(View.VISIBLE);
		// graphView.setAlpha(128);

		graphLayout.addView(graphView);
		mGraphs.put(device.getType().getTypeId(), graphView);
		
		mLayout.addView(row);
		
		final UnitsHelper unitsHelper = new UnitsHelper(mController.getUserSettings(), mContext);
		//final TimeHelper timeHelper = new TimeHelper(mController.getUserSettings());
		final DateTimeFormatter fmt = DateTimeFormat.forPattern(mGraphDateTimeFormat); // FIXME for correct timezone data..
		
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

		/*graphLayout.setOnTouchListener(new OnTouchListener() {

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
		});*/

	}
	
	private void fillGraph(DeviceLog log, Device device) {

		GraphView graphView = mGraphs.get(device.getType().getTypeId());
		if (graphView == null) {
			return;
		}
	
		Random random = new Random();
		
		//for (DeviceLog log : logs) {
	
			GraphViewSeriesStyle seriesStyle = new GraphViewSeriesStyle(getResources().getColor((random.nextBoolean())?R.color.iha_primary_cyan:R.color.iha_secundary_pink), 4);
			
			//GraphViewSeriesStyle seriesStyleBlue = new GraphViewSeriesStyle(mContext.getResources().getColor(R.color.iha_primary_cyan), 2);
			// GraphViewSeriesStyle seriesStyleGray = new GraphViewSeriesStyle(getResources().getColor(R.color.light_gray),2);

			GraphViewSeries graphSeries = new GraphViewSeries(device.getName(), seriesStyle, new GraphViewData[] { new GraphView.GraphViewData(0, 0), });

			graphView.addSeries(graphSeries);
			
			int size = log.getValues().size();
			// Log.d(TAG, String.format("Filling graph with %d values. Min: %.1f, Max: %.1f", size, log.getMinimum(), log.getMaximum()));

			int begin;
			GraphView.GraphViewData[] data;

			// Limit amount of showed values
			/*if (size > MAX_GRAPH_DATA_COUNT) {
				data = new GraphView.GraphViewData[MAX_GRAPH_DATA_COUNT];
				begin = (size - MAX_GRAPH_DATA_COUNT);
			} else {*/
				data = new GraphView.GraphViewData[size];
				begin = 0;
			//}

			for (int i = begin; i < size; i++) {
				DeviceLog.DataRow row = log.getValues().get(i);
				
				float value = Float.isNaN(row.value) ? log.getMinimum() : row.value;
				data[i - begin] = new GraphView.GraphViewData(row.dateMillis, value);
				//Log.v(TAG, String.format("Graph value: date(msec): %s, Value: %.1f", fmt.print(row.dateMillis), row.value));
			}

			// Set maximum as +10% more than deviation
			//graphView.setManualYAxisBounds(log.getMaximum() + log.getDeviation() * 0.1, log.getMinimum());
			// mGraphView.setViewPort(0, 7);
			graphSeries.resetData(data);
			//mGraphInfo.setText(getView().getResources().getString(R.string.sen_detail_graph_info));
		//}
		
		//graphView.setManualYAxisBounds(1.0, 0.0);
		
		//graphView.setViewPort(0, 7);
	}
	
	private void prepareDevices() {
		for (Adapter adapter : mController.getAdapters()) {
			Log.d(TAG, String.format("Preparing adapter %s", adapter.getId()));
			
			for (Facility facility: mController.getFacilitiesByAdapter(adapter.getId())) {
				Log.d(TAG, String.format("Preparing facility with %d device", facility.getDevices().size()));
				
				for (Device device : facility.getDevices()) {
					Log.d(TAG, String.format("Preparing device %s (type %d)", device.getName(), device.getType().getTypeId()));
					
					List<Device> devices = mDevices.get(device.getType().getTypeId());
					if (devices == null) {
						devices = new ArrayList<Device>();
						mDevices.put(device.getType().getTypeId(), devices);
						
						DeviceType type = device.getType();
						addGraph(device);
					}
					
					devices.add(device);
					
					GetDeviceLogTask getDeviceLogTask = new GetDeviceLogTask();
					getDeviceLogTask.execute(device);
				}
			}
		}
	}
	
	/*@SuppressWarnings("unchecked")
	private void prepareLogs() {
		for (int i=0; i<mDevices.size(); i++) {
			GetDeviceLogTask getDeviceLogTask = new GetDeviceLogTask();
			getDeviceLogTask.execute(mDevices.valueAt(i));
		}
	}*/
	
	private class GetDeviceLogTask extends AsyncTask<Device, Void, DeviceLog> {
		
		private int mTypeId = 0;
		private Device mDevice;
		
		@Override
		protected DeviceLog doInBackground(Device... devices) {
			mDevice = devices[0]; // expects only one device at a time is sent there
			mTypeId = mDevice.getType().getTypeId();
			
			DateTime end = DateTime.now(DateTimeZone.UTC);
			DateTime start = end.minusDays(3);//end.minusWeeks(1);
			
			LogDataPair pair = new LogDataPair( //
					mDevice, // device
					new Interval(start, end), // interval from-to
					DataType.AVERAGE, // type
					DataInterval.HOUR); // interval

			return mController.getDeviceLog(pair.device, pair);	
		}

		@Override
		protected void onPostExecute(DeviceLog log) {
			fillGraph(log, mDevice);
		}

	}

}
