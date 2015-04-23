package com.rehivetech.beeeon.widget.data;

import android.content.Context;
import android.graphics.Bitmap;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.BaseSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.asynctask.GetDeviceLogTask;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.DeviceLog;
import com.rehivetech.beeeon.household.device.Facility;
import com.rehivetech.beeeon.household.device.RefreshInterval;
import com.rehivetech.beeeon.household.device.values.BaseEnumValue;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.pair.LogDataPair;
import com.rehivetech.beeeon.util.GraphViewHelper;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;
import com.rehivetech.beeeon.widget.persistence.WidgetLocationPersistence;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

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

    public WidgetLocationPersistence widgetLocation;

    public WidgetGraphData(int widgetId, Context context, UnitsHelper unitsHelper, TimeHelper timeHelper){
        super(widgetId, context, unitsHelper, timeHelper);
        mGraphWidth = (int) mContext.getResources().getDimension(R.dimen.widget_graph_width);
        mGraphHeight = (int) mContext.getResources().getDimension(R.dimen.widget_graph_height);

        mGraph = new GraphView(mContext);
        widgetLocation = new WidgetLocationPersistence(mContext, mWidgetId, 0, R.id.location_container, mUnitsHelper, mTimeHelper);
    }

    @Override
    public String getClassName() {
        return WidgetGraphData.class.getName();
    }

    @Override
    public void init() {
        if(widgetDevice.getId().isEmpty()){
            Log.i(TAG, "Could not retrieve device from widget " + String.valueOf(mWidgetId));
            return;
        }

        String[] ids = widgetDevice.getId().split(Device.ID_SEPARATOR, 2);
        Facility facility = new Facility();
        facility.setAdapterId(adapterId);
        facility.setAddress(ids[0]);
        facility.setLastUpdate(new DateTime(widgetDevice.lastUpdateTime, DateTimeZone.UTC));
        facility.setRefresh(RefreshInterval.fromInterval(widgetDevice.refresh));

        Device dev = Device.createFromDeviceTypeId(ids[1]);
        facility.addDevice(dev);

        mFacilities.clear();
        mFacilities.add(facility);

        initGraph(dev);
    }

    @Override
    public void load(){
        super.load();
        widgetLocation.load();
    }

    @Override
    protected void save() {
        super.save();
        widgetLocation.save();
    }

    @Override
    public void delete(Context context) {
        super.delete(context);
        widgetLocation.delete();
    }

    private void initGraph(Device device) {
        if(mTimeHelper == null || mUnitsHelper == null) return;

        Log.d(TAG, "prepareWidgetGraphView");

        Adapter adapter = mController.getAdaptersModel().getAdapter(adapterId);
        final DateTimeFormatter fmt = mTimeHelper.getFormatter(mGraphDateTimeFormat, adapter);
        GraphViewHelper.prepareWidgetGraphView(mGraph, mContext, mFacilities.get(0).getDevices().get(0), fmt, mUnitsHelper);

        // clears series if reinitializes
        if(mGraph.getSeries() != null && mGraph.getSeries().size() > 0){
            mGraph.removeAllSeries();
        }

        if (device.getValue() instanceof BaseEnumValue) {
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
    public void initLayout() {
        super.initLayout();
        widgetLocation.initValueView(mRemoteViews);
    }

    @Override
    protected boolean updateData() {
        Device device = mController.getFacilitiesModel().getDevice(adapterId, widgetDevice.getId());
        if(device == null){
            Log.v(TAG, String.format("Updating widget (%d) with cached data", getWidgetId()));
            return false;
        }

        Adapter adapter = mController.getAdaptersModel().getAdapter(adapterId);
        widgetDevice.change(device, adapter);


        Location location = mController.getLocationsModel().getLocation(adapterId, device.getFacility().getLocationId());
        if(location != null){
            widgetLocation.change(location, adapter);
        }

        widgetLastUpdate = getTimeNow();
        adapterId = adapter.getId();

        doLoadGraphData(device);

        this.save();
        Log.v(TAG, String.format("Updating widget (%d) with fresh data", getWidgetId()));
        return true;
    }

    @Override
    protected void updateLayout() {
        super.updateLayout();

        widgetLocation.updateValueView(false);
        Log.d(TAG, String.format("Graph: %d %d, is NUll = %b", mGraphWidth, mGraphHeight, mGraphBitmap == null));
        if(mGraphBitmap != null) mRemoteViews.setImageViewBitmap(R.id.widget_graph, mGraphBitmap);
    }

    private void doLoadGraphData(Device device) {
        DateTime end = DateTime.now(DateTimeZone.UTC);
        DateTime start = end.minusWeeks(1);
        DateTimeFormatter fmt = DateTimeFormat.forPattern(LOG_DATE_TIME_FORMAT).withZoneUTC();
        Log.d(TAG, String.format("Loading graph data from %s to %s.", fmt.print(start), fmt.print(end)));

        mGetDeviceLogTask = new GetDeviceLogTask(mContext);
        LogDataPair pair = new LogDataPair(device, new Interval(start, end), DeviceLog.DataType.AVERAGE, (device.getValue() instanceof BaseEnumValue)? DeviceLog.DataInterval.RAW: DeviceLog.DataInterval.HOUR);

        mGetDeviceLogTask.setListener(new GetDeviceLogTask.CallbackLogTaskListener() {
            @Override
            public void onExecute(DeviceLog result) {
                fillGraph(result);
                mGraphBitmap = mGraph.drawBitmap(mGraphWidth, mGraphHeight);
            }
        });
        mGetDeviceLogTask.execute(new LogDataPair[]{pair});
    }

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

}
