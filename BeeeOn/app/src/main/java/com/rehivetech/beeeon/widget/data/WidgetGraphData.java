package com.rehivetech.beeeon.widget.data;

import android.content.Context;
import android.graphics.Bitmap;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.BaseSeries;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.point.DataPoint;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.asynctask.GetDeviceLogTask;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.DeviceLog;
import com.rehivetech.beeeon.household.device.Facility;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.household.device.values.BaseEnumValue;
import com.rehivetech.beeeon.household.device.values.BaseValue;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.pair.LogDataPair;
import com.rehivetech.beeeon.util.GraphViewHelper;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;
import com.rehivetech.beeeon.util.Utils;
import com.rehivetech.beeeon.widget.persistence.WidgetDevicePersistence;
import com.rehivetech.beeeon.widget.persistence.WidgetLocationPersistence;
import com.rehivetech.beeeon.widget.persistence.WidgetLogDataPersistence;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;


public class WidgetGraphData extends WidgetDeviceData {
    private static final String TAG = WidgetGraphData.class.getSimpleName();

    private String mGraphDateTimeFormat = "dd.MM. kk:mm";
    private static final String LOG_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private GetDeviceLogTask mGetDeviceLogTask;

    private GraphView mGraph;
    private BaseSeries mGraphSeries;
    private Bitmap mGraphBitmap;

    private int mGraphWidth;
    private int mGraphHeight;

    public WidgetLogDataPersistence widgetLogData;

    private LogDataPair mLogDataPair;

    public WidgetLocationPersistence widgetLocation;

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

        mFacilities.clear();
        for(WidgetDevicePersistence dev : widgetDevices){
            if(dev.getId().isEmpty()){
                Log.i(TAG, "Could not retrieve device from widget " + String.valueOf(mWidgetId));
                continue;
            }

            String[] ids = dev.getId().split(Device.ID_SEPARATOR, 2);
            Facility facility = new Facility();
            facility.setAdapterId(widgetAdapterId);
            facility.setAddress(ids[0]);
            facility.setLastUpdate(new DateTime(dev.lastUpdateTime, DateTimeZone.UTC));
            facility.setRefresh(RefreshInterval.fromInterval(dev.refresh));

            Device device = Device.createFromDeviceTypeId(ids[1]);
            facility.addDevice(device);

            mFacilities.add(facility);
            initGraph(device.getValue());
            createLogDataPair();
            break;          // only one device possible
        }
    }

    @Override
    public void save() {
        super.save();
        widgetLocation.save();
        widgetLogData.save();
    }

    // ----------------------------------------------------------- //
    // ------------------------ RENDERING ------------------------ //
    // ----------------------------------------------------------- //
    /**
     * Initialize graph and series which will be used base ond device type
     * @param baseValue
     */
    private void initGraph(BaseValue baseValue) {
        if(mTimeHelper == null || mUnitsHelper == null) return;
        Log.d(TAG, "prepareWidgetGraphView");

        Adapter adapter = mController.getAdaptersModel().getAdapter(widgetAdapterId);
        final DateTimeFormatter fmt = mTimeHelper.getFormatter(mGraphDateTimeFormat, adapter);
        GraphViewHelper.prepareWidgetGraphView(mGraph, mContext, baseValue, fmt, mUnitsHelper);

        // clears series if reinitializes
        if(mGraph.getSeries() != null && mGraph.getSeries().size() > 0){
            mGraph.removeAllSeries();
        }

        if (baseValue instanceof BaseEnumValue) {
            mGraphSeries = new BarGraphSeries<>(new DataPoint[]{new DataPoint(0, 0), new DataPoint(1,1)});
            ((BarGraphSeries) mGraphSeries).setSpacing(30);
        } else {
            mGraphSeries =  new LineGraphSeries<>(new DataPoint[]{new DataPoint(0, 0), new DataPoint(1,1)});
            ((LineGraphSeries) mGraphSeries).setBackgroundColor(mContext.getResources().getColor(R.color.alpha_blue));
            ((LineGraphSeries) mGraphSeries).setDrawBackground(true);
            ((LineGraphSeries) mGraphSeries).setThickness(2);
        }

        mGraph.addSeries(mGraphSeries);
    }

    @Override
    protected void renderLayout() {
        super.renderLayout();

        widgetLocation.initView();
        widgetLocation.renderView(mBuilder);

        Log.d(TAG, String.format("Graph: %d %d, is NUll = %b", mGraphWidth, mGraphHeight, mGraphBitmap == null));
        if(mGraphBitmap != null) mBuilder.setImage(R.id.widget_graph, mGraphBitmap);
    }

    /**
     * Creates new log pair from data saved last time when was updated
     */
    private void createLogDataPair() {
        Facility fac = (Facility) mFacilities.get(0);
        if(fac == null) return;

        mLogDataPair = new LogDataPair(
            fac.getDevices().get(0),
            new Interval(widgetLogData.intervalStart, DateTime.now(DateTimeZone.UTC).getMillis()),
                    Utils.getEnumFromId(DeviceLog.DataType.class, widgetLogData.type),
                    DeviceLog.DataInterval.fromSeconds(widgetLogData.gap));
    }

    /**
     * Fills graph with received data
     * @param log
     */
    private void fillGraph(DeviceLog log) {
        if(mGraph == null) return;
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
        Adapter adapter = mController.getAdaptersModel().getAdapter(widgetAdapterId);
        if(adapter == null) return false;

        for(WidgetDevicePersistence dev : widgetDevices) {
            Device device = mController.getFacilitiesModel().getDevice(widgetAdapterId, dev.getId());
            if(device == null) continue;

            Location location = mController.getLocationsModel().getLocation(dev.adapterId, device.getFacility().getLocationId());
            if(location != null){
                widgetLocation.configure(location, adapter);
            }

            dev.configure(device, adapter);
            updated++;
        }

        if(updated > 0) {
            // update last update to "now"
            widgetLastUpdate = getTimeNow();
            widgetAdapterId = adapter.getId();

            DeviceLog log = mController.getDeviceLogsModel().getDeviceLog(mLogDataPair);
            if(log != null) {
                mGraphBitmap = mGraph.drawBitmap(mGraphWidth, mGraphHeight);
                fillGraph(log);
                widgetLogData.intervalStart = DateTime.now(DateTimeZone.UTC).minusWeeks(1).getMillis();
            }

            createLogDataPair();
            // Save fresh data
            this.save();
            Log.v(TAG, String.format("Updating widget (%d) with fresh data", getWidgetId()));
        }
        else {
            // TODO show some kind of icon
            Log.v(TAG, String.format("Updating widget (%d) with cached data", getWidgetId()));
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

        // first add parent objects (facilities)
        resultObj.addAll(super.getObjectsToReload());

        // then from this widget
        resultObj.add(mLogDataPair);

        return resultObj;
    }

    @Override
    public String getClassName() {
        return WidgetGraphData.class.getName();
    }
}
