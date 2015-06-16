package com.rehivetech.beeeon.widget.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.BaseSeries;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.point.DataPoint;
import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.ModuleLog;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.household.device.values.BaseEnumValue;
import com.rehivetech.beeeon.household.device.values.BaseValue;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.util.GraphViewHelper;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;
import com.rehivetech.beeeon.util.Utils;
import com.rehivetech.beeeon.widget.persistence.WidgetLocationPersistence;
import com.rehivetech.beeeon.widget.persistence.WidgetLogDataPersistence;
import com.rehivetech.beeeon.widget.persistence.WidgetModulePersistence;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;


public class WidgetGraphData extends WidgetModuleData {
	private static final String TAG = WidgetGraphData.class.getSimpleName();

	private static final String GRAPH_FILE_NAME = "widget_%d_graph.png";

	private GraphView mGraph;
	private BaseSeries mGraphSeries;
	private Bitmap mGraphBitmap;

	private int mGraphWidth;
	private int mGraphHeight;

	public WidgetLogDataPersistence widgetLogData;
	public WidgetLocationPersistence widgetLocation;

	private ModuleLog.DataPair mLogDataPair;

	/**
	 * Constructing object holding information about widget (instantiating in config activity and then in service)
	 *
	 * @param widgetId
	 * @param context
	 * @param unitsHelper
	 * @param timeHelper
	 */
	public WidgetGraphData(int widgetId, Context context, UnitsHelper unitsHelper, TimeHelper timeHelper) {
		super(widgetId, context, unitsHelper, timeHelper);

		mGraphWidth = (int) mContext.getResources().getDimension(R.dimen.widget_graph_width);
		mGraphHeight = (int) mContext.getResources().getDimension(R.dimen.widget_graph_height);

		mGraph = new GraphView(mContext);
		widgetLocation = new WidgetLocationPersistence(mContext, mWidgetId, 0, R.id.location_container, mUnitsHelper, mTimeHelper, settings);
		widgetLogData = new WidgetLogDataPersistence(mContext, mWidgetId);
	}

	// ----------------------------------------------------------- //
	// ---------------- MANIPULATING PERSISTENCE ----------------- //
	// ----------------------------------------------------------- //

	@Override
	public void load() {
		super.load();
		widgetLocation.load();
		widgetLogData.load();
	}

	@Override
	public void init() {
		Log.d(TAG, "init()");
		// NOTE: we don't override base method here cause it would need to do similar work twice

		mDevices.clear();
		for (WidgetModulePersistence dev : widgetModules) {
			if (dev.getId().isEmpty()) {
				Log.i(TAG, "Could not retrieve module from widget " + String.valueOf(mWidgetId));
				continue;
			}

			String[] ids = dev.getId().split(Module.ID_SEPARATOR, 2);
			Device device = new Device();
			device.setGateId(widgetGateId);
			device.setAddress(ids[0]);
			device.setLastUpdate(new DateTime(dev.lastUpdateTime, DateTimeZone.UTC));
			device.setRefresh(RefreshInterval.fromInterval(dev.refresh));

			Module module = Module.createFromModuleTypeId(ids[1]);
			device.addModule(module);

			mDevices.add(device);
			initGraph(module.getValue());
			createLogDataPair();
			break;          // only one module possible
		}

		// if initialized and no graph bitmap - tries to load it
		if (mGraphBitmap == null) {
			Log.v(TAG, "Graph bitmap from internal memory");
			mGraphBitmap = loadBitmapFromStorage();
		}
	}

	@Override
	public void save() {
		super.save();
		widgetLocation.save();
		widgetLogData.save();

		if (mGraphBitmap != null) saveBitmapToInternalSorage(mGraphBitmap);
	}

	@Override
	public void delete() {
		super.delete();
		File f = getGraphImageFile();
		boolean deleted = f.delete();
	}

	// ----------------------------------------------------------- //
	// ------------------------ RENDERING ------------------------ //
	// ----------------------------------------------------------- //

	/**
	 * Initialize graph and series which will be used base ond module type
	 *
	 * @param baseValue
	 */
	private void initGraph(BaseValue baseValue) {
		if (mTimeHelper == null || mUnitsHelper == null) return;
		Log.d(TAG, "prepareWidgetGraphView");

		Gate gate = Controller.getInstance(mContext).getGatesModel().getGate(widgetGateId);
		String graphDateTimeFormat = "dd.MM. kk:mm";
		final DateTimeFormatter fmt = mTimeHelper.getFormatter(graphDateTimeFormat, gate);
		GraphViewHelper.prepareWidgetGraphView(mGraph, mContext, baseValue, fmt, mUnitsHelper);

		// clears series if reinitializes
		if (mGraph.getSeries() != null && mGraph.getSeries().size() > 0) {
			Log.v(TAG, "removeAllSeries");
			mGraph.removeAllSeries();
		}

		if (baseValue instanceof BaseEnumValue) {
			Log.v(TAG, "baseEnumValue");
			mGraphSeries = new BarGraphSeries<>(new DataPoint[]{new DataPoint(0, 0), new DataPoint(1, 1)});
			((BarGraphSeries) mGraphSeries).setSpacing(30);
		} else {
			Log.v(TAG, "other type of value");
			mGraphSeries = new LineGraphSeries<>(new DataPoint[]{new DataPoint(0, 0), new DataPoint(1, 1)});
			((LineGraphSeries) mGraphSeries).setBackgroundColor(mContext.getResources().getColor(R.color.alpha_blue));
			((LineGraphSeries) mGraphSeries).setDrawBackground(true);
			((LineGraphSeries) mGraphSeries).setThickness(2);
		}

		mGraph.addSeries(mGraphSeries);
	}

	@Override
	protected void renderLayout() {
		super.renderLayout();

		widgetLocation.renderView(mBuilder);

		Log.d(TAG, String.format("Graph: %d %d, is NUll = %b", mGraphWidth, mGraphHeight, mGraphBitmap == null));
		if (mGraphBitmap != null) mBuilder.setImage(R.id.widget_graph, mGraphBitmap);
	}

	/**
	 * Creates new log pair from data saved last time when was updated
	 */
	private void createLogDataPair() {
		Device fac = (Device) mDevices.get(0);
		if (fac == null) return;

		mLogDataPair = new ModuleLog.DataPair(
				fac.getModules().get(0),
				new Interval(widgetLogData.intervalStart, DateTime.now(DateTimeZone.UTC).getMillis()),
				Utils.getEnumFromId(ModuleLog.DataType.class, widgetLogData.type),
				ModuleLog.DataInterval.fromSeconds(widgetLogData.gap));
	}

	/**
	 * Fills graph with received data
	 *
	 * @param log
	 */
	private void fillGraph(ModuleLog log) {
		if (mGraph == null) return;
		Log.d(TAG, "fillGraph");

		SortedMap<Long, Float> values = log.getValues();
		int size = values.size();
		DataPoint[] data = new DataPoint[size];

		Log.d(TAG, String.format("Filling graph with %d values. Min: %.1f, Max: %.1f", size, log.getMinimum(), log.getMaximum()));

		int i = 0;
		for (Map.Entry<Long, Float> entry : values.entrySet()) {
			Long dateMillis = entry.getKey();
			float value = Float.isNaN(entry.getValue()) ? log.getMinimum() : entry.getValue();

			data[i++] = new DataPoint(dateMillis, value);

			// This shouldn't happen, only when some other thread changes this values object - can it happen?
			if (i >= size)
				break;
		}

		Log.d(TAG, "Filling graph finished");

		mGraphSeries.resetData(data);
		mGraph.getViewport().setXAxisBoundsManual(true);
		if (values.size() > 100 && mGraphSeries instanceof BarGraphSeries) {
			mGraph.getViewport().setMaxX(mGraphSeries.getHighestValueX());
			mGraph.getViewport().setMinX(mGraphSeries.getHighestValueX() - TimeUnit.HOURS.toMillis(1));
		}
	}

	// ----------------------------------------------------------- //
	// ---------------------- FAKE HANDLERS ---------------------- //
	// ----------------------------------------------------------- //

	@Override
	public boolean handleUpdateData() {
		int updated = 0;
		Controller controller = Controller.getInstance(mContext);
		Gate gate = controller.getGatesModel().getGate(widgetGateId);
		if (gate == null) return false;

		for (WidgetModulePersistence dev : widgetModules) {
			Module module = controller.getDevicesModel().getModule(widgetGateId, dev.getId());
			if (module == null) continue;

			Location location = controller.getLocationsModel().getLocation(dev.gateId, module.getDevice().getLocationId());
			if (location != null) {
				widgetLocation.configure(location, gate);
			}

			dev.configure(module, gate);
			updated++;
		}

		if (updated > 0) {
			// update last update to "now"
			widgetLastUpdate = getTimeNow();
			widgetGateId = gate.getId();

			ModuleLog log = controller.getModuleLogsModel().getModuleLog(mLogDataPair);
			if (log != null) {
				fillGraph(log);
				mGraphBitmap = mGraph.drawBitmap(mGraphWidth, mGraphHeight);
				widgetLogData.intervalStart = DateTime.now(DateTimeZone.UTC).minusWeeks(1).getMillis();
			}

			createLogDataPair();
			// Save fresh data
			this.save();
		}

		return updated > 0;
	}

	@Override
	public void handleResize(int minWidth, int minHeight) {
		// NO Operation -> just to override devices resizing
		// TODO make graph bigger based on minWidth ?
	}

	// ----------------------------------------------------------- //
	// ------------------------- GETTERS ------------------------- //
	// ----------------------------------------------------------- //
	@Override
	public List<Object> getObjectsToReload() {
		List<Object> resultObj = new ArrayList<>();

		// first add parent objects (devices)
		resultObj.addAll(super.getObjectsToReload());

		// then from this widget
		resultObj.add(mLogDataPair);

		return resultObj;
	}

	@Override
	public String getClassName() {
		return WidgetGraphData.class.getName();
	}

	// ----------------------------------------------------------- //
	// ---------------------- BITMAP CACHING --------------------- //
	// ----------------------------------------------------------- //

	/**
	 * Get file of the widget cached graph image
	 *
	 * @return
	 */
	private File getGraphImageFile() {
		// path to /data/data/yourapp/app_data/imageDir (creates if not exist)
		File directory = mContext.getDir(Constants.PERSISTENCE_APP_IMAGE_DIR, Context.MODE_PRIVATE);
		return new File(directory, String.format(GRAPH_FILE_NAME, mWidgetId));
	}

	/**
	 * Saves bitmap into internal storage
	 *
	 * @param bitmapImage bitmap to save
	 */
	private void saveBitmapToInternalSorage(Bitmap bitmapImage) {
		try {
			File f = getGraphImageFile();
			FileOutputStream fos = new FileOutputStream(f);

			// Use the compress method on the BitMap object to write image to the OutputStream
			bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Load bitmap from internal storage and creates bitmap from it
	 *
	 * @return decoded bitmap
	 */
	private Bitmap loadBitmapFromStorage() {
		try {
			File f = getGraphImageFile();
			return BitmapFactory.decodeStream(new FileInputStream(f));
		} catch (FileNotFoundException e) {
			// NOTE: If not found we just want to return null
		}
		return null;
	}
}
