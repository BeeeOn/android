package com.rehivetech.beeeon.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.activity.spinnerItem.DeviceSpinnerItem;
import com.rehivetech.beeeon.activity.spinnerItem.GeofenceSpinnerItem;
import com.rehivetech.beeeon.activity.spinnerItem.SpinnerItem;
import com.rehivetech.beeeon.arrayadapter.DeviceArrayAdapter;
import com.rehivetech.beeeon.arrayadapter.SpinnerMultiAdapter;
import com.rehivetech.beeeon.asynctask.CallbackTask;
import com.rehivetech.beeeon.asynctask.ReloadAdapterDataTask;
import com.rehivetech.beeeon.asynctask.RemoveWatchDogTask;
import com.rehivetech.beeeon.asynctask.SaveWatchDogTask;
import com.rehivetech.beeeon.base.BaseApplicationActivity;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.geofence.SimpleGeofence;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Facility;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.household.watchdog.WatchDog;
import com.rehivetech.beeeon.household.watchdog.WatchDogBaseType;
import com.rehivetech.beeeon.household.watchdog.WatchDogGeofenceType;
import com.rehivetech.beeeon.household.watchdog.WatchDogSensorType;
import com.rehivetech.beeeon.pair.DelWatchDogPair;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.UnitsHelper;
import com.rehivetech.beeeon.util.Utils;

import java.util.ArrayList;
import java.util.List;

// TODO probably create some interface for watchdogActionTypes so that there does not need to be switch everywhere for that
// TODO handle on-off sensor values

/**
 * Activity for creation and editation of watchdog rule
 * @author mlyko
 */
public class WatchDogEditRuleActivity extends BaseApplicationActivity {
    private static final String TAG = WatchDogEditRuleActivity.class.getSimpleName();

    public static final String EXTRA_ADAPTER_ID = "adapter_id";
    public static final String EXTRA_RULE_ID = "rule_id";
    public static final String EXTRA_IS_NEW ="rule_is_new";

    // helper variables for getting devices from list
    private static final int DEVICES_ALL = 0;
    private static final int DEVICES_ACTORS = 1;
    private static final int DEVICES_SENSORS = 2;

    // extras
    private String mActiveAdapterId;
    private String mActiveRuleId;

    private Controller mController;
    private Toolbar mToolbar;
    private Menu mOptionsMenu;
    private ProgressDialog mProgress;
    private Adapter mAdapter;
    private UnitsHelper mUnitsHelper;
    private SharedPreferences mPrefs;

    private List<Location> mLocations;
    private List<Facility> mFacilities;
    private List<SimpleGeofence> mGeofences;

    // TODO??
    private List<Device> _sensors;
    private List<Device> _actors;

    private boolean mIsValueLess = false;        // value for View FAB
    private boolean mIsNew = false;
    private WatchDog mWatchDog;
    private String mWatchDogAction;
    private WatchDogBaseType mWatchDogOperator;

    // async tasks
    private ReloadAdapterDataTask mReloadFacilitiesTask;
    private SaveWatchDogTask mSaveWatchDogTask;
    private RemoveWatchDogTask mRemoveWatchDogTask;

    // GUI elements
    private RadioGroup mActionType;
    private EditText mRuleName;
    private TextView mRuleTresholdUnit;
    private SwitchCompat mRuleEnabled;
    private Spinner mIfItemSpinner;
    private FloatingActionButton mOperatorButton;
    private EditText mRuleTreshold;

    private EditText mNotificationText;
    private Spinner mActorSpinner;

    SpinnerMultiAdapter mSpinnerMultiAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watchdog_edit_rule);
        Log.d(TAG, "onCreate()");

        // prepare toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            mToolbar.setTitle(R.string.watchdog_rule);
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            // x instead of <- indicating that no changes will be saved upon click
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_cancel);
        }

        // Prepare progress dialog
        mProgress = new ProgressDialog(this);
        mProgress.setMessage(getString(R.string.progress_saving_data));
        mProgress.setCancelable(false);
        mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            mActiveAdapterId = bundle.getString(EXTRA_ADAPTER_ID);
            mActiveRuleId = bundle.getString(EXTRA_RULE_ID);
            mIsNew = false;
        }
        else{
            bundle = savedInstanceState;
            if (bundle != null) {
                mActiveAdapterId = bundle.getString(EXTRA_ADAPTER_ID);
                mActiveRuleId = bundle.getString(EXTRA_RULE_ID);
                mIsNew = bundle.getBoolean(EXTRA_IS_NEW);
            }
            else{
                mIsNew = true;
            }
        }

        // if we want detail of some rule, we need id and adapter
        if (!mIsNew && (mActiveAdapterId == null || mActiveRuleId == null)) {
            Toast.makeText(this, R.string.toast_wrong_or_no_watchdog_rule, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // get controllern
        mController = Controller.getInstance(this);
        // get adapter
        mAdapter = mActiveAdapterId == null ? mController.getActiveAdapter() : mController.getAdaptersModel().getAdapter(mActiveAdapterId);

		if(mAdapter == null ) {
			//TODO: neocekova chyba
			return;
		}

        // UserSettings can be null when user is not logged in!
        mPrefs = mController.getUserSettings();
        mUnitsHelper = (mPrefs == null) ? null : new UnitsHelper(mPrefs, this);

        // get all locations for spinners
        mLocations = mController.getLocationsModel().getLocationsByAdapter(mAdapter.getId());
        // get all geofence areas
		String userId = mController.getActualUser().getId();
        mGeofences = mController.getGeofenceModel().getAllGeofences(userId);

        // facilities get by cycling through all locations
        mFacilities = new ArrayList<Facility>();
        for(Location loc : mLocations){
            List<Facility> tempFac = mController.getFacilitiesModel().getFacilitiesByLocation(mAdapter.getId(), loc.getId());
            mFacilities.addAll(tempFac);
        }

        // get watchdog rule
        if(!mIsNew) {
            mWatchDog = mController.getWatchDogsModel().getWatchDog(mAdapter.getId(), mActiveRuleId);
            if(mWatchDog == null){
                Toast.makeText(this, R.string.toast_something_wrong, Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }
        else{
            mWatchDog = new WatchDog(WatchDog.TYPE_SENSOR);
            mWatchDog.setAdapterId(mAdapter.getId());
        }

        mWatchDogOperator = mWatchDog.getOperatorType();

        // needs to set units helper if want to use it
        mWatchDogOperator.setUnitsHelper(mUnitsHelper);

        // hide keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        initLayout();
        setValues();
    }

    /**
     * Initializes GUI elements to work
     */
    private void initLayout() {
        // init gui elements
        mRuleName = (EditText) findViewById(R.id.watchdog_edit_name);
        mRuleEnabled = (SwitchCompat) findViewById(R.id.watchdog_edit_switch);
        mIfItemSpinner = (Spinner) findViewById(R.id.watchdog_edit_if_item_spinner);
        mOperatorButton = (FloatingActionButton) findViewById(R.id.watchdog_edit_operator);
        mRuleTreshold = (EditText) findViewById(R.id.watchdog_edit_treshold);
        mRuleTresholdUnit = (TextView) findViewById(R.id.watchdog_edit_treshold_unit);
        mActionType = (RadioGroup) findViewById(R.id.watchdog_edit_action_radiogroup);

        // "Then" elements
        mNotificationText = (EditText) findViewById(R.id.watchdog_edit_notification_text);
        mActorSpinner = (Spinner) findViewById(R.id.watchdog_edit_actor_spinner);

        // ----- prepare list of available device & geofences
        mSpinnerMultiAdapter = new SpinnerMultiAdapter(this);
        // devices
        mSpinnerMultiAdapter.addHeader(getString(R.string.devices));
        for(Device dev : getDevicesArray(DEVICES_SENSORS)){
            Location loc = Utils.getFromList(dev.getFacility().getLocationId(), mLocations);
            mSpinnerMultiAdapter.addItem(new DeviceSpinnerItem(dev, loc, dev.getId(), this));
        }

        // geofence areas
        mSpinnerMultiAdapter.addHeader(getString(R.string.title_activity_map_geofence));
        for(SimpleGeofence geo : mGeofences){
            mSpinnerMultiAdapter.addItem(new GeofenceSpinnerItem(geo, geo.getId(), this));
        }

        mIfItemSpinner.setAdapter(mSpinnerMultiAdapter);
        mIfItemSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SpinnerItem selected = mSpinnerMultiAdapter.getItem(position);

                switch (selected.getType()) {
                    case DEVICE:
                        mWatchDogOperator = new WatchDogSensorType(mWatchDogOperator.getIndex());
                        mWatchDog.setType(WatchDog.TYPE_SENSOR);
                        mWatchDog.setGeoRegionId("");
                        break;

                    case GEOFENCE:
                        mWatchDogOperator = new WatchDogGeofenceType(mWatchDogOperator.getIndex());
                        mWatchDog.setType(WatchDog.TYPE_GEOFENCE);
                        break;
                }
                // we need to refresh UnitHelper cause setOperator destroys it
                mWatchDogOperator.setUnitsHelper(mUnitsHelper);
                // setup gui based on type
                mWatchDogOperator.setupGUI(selected, mOperatorButton, mRuleTreshold, mRuleTresholdUnit);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // setup gui based on type in default position
        int defaultPos = mSpinnerMultiAdapter.getRealPosition(0);
        mIfItemSpinner.setSelection(defaultPos);

        // changing specified layout when checked
        mActionType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RelativeLayout NotifLayout = (RelativeLayout) findViewById(R.id.watchdog_detail_notification);
                RelativeLayout ActionLayout = (RelativeLayout) findViewById(R.id.watchdog_edit_actor_layout);

                switch (checkedId) {
                    case R.id.watchdog_edit_notification:
                        NotifLayout.setVisibility(View.VISIBLE);
                        ActionLayout.setVisibility(View.GONE);
                        mWatchDogAction = WatchDog.ACTION_NOTIFICATION;
                        break;

                    case R.id.watchdog_edit_actor:
                        NotifLayout.setVisibility(View.GONE);
                        ActionLayout.setVisibility(View.VISIBLE);
                        mWatchDogAction = WatchDog.ACTION_ACTOR;
                        break;
                }
            }
        });

        // ------- choose action type if not loaded
        if(mIsNew){
            // first checked
            RadioButton radioNotification = (RadioButton) findViewById(R.id.watchdog_edit_notification);
            radioNotification.setChecked(true);
        }

        // ------- choose actor
        final DeviceArrayAdapter actorAdapter = new DeviceArrayAdapter(this, R.layout.custom_spinner2_item, getDevicesArray(DEVICES_ACTORS), mLocations);
        actorAdapter.setLayoutInflater(getLayoutInflater());
        actorAdapter.setDropDownViewResource(R.layout.custom_spinner2_dropdown_item);
        mActorSpinner.setAdapter(actorAdapter);
    }

    /**
     * Fills gui elements with values
     */
    private void setValues(){
        // if this is new watchdog, we don't set anything
        if(mWatchDog.getId() == null) return;

        mRuleName.setText(mWatchDog.getName());
        mRuleEnabled.setChecked(mWatchDog.isEnabled());
        mRuleTreshold.setText(mWatchDog.getParams().get(WatchDog.PAR_TRESHOLD));

        // set spinner in the "IF" section
        if(mWatchDog.getGeoRegionId() != null && !mWatchDog.getGeoRegionId().isEmpty()){
            int index = Utils.getObjectIndexFromList(mWatchDog.getGeoRegionId(), mGeofences);
            if(index > -1) mIfItemSpinner.setSelection(mSpinnerMultiAdapter.getRealPosition(index, SpinnerItem.SpinnerItemType.GEOFENCE));
        }
        else if(mWatchDog.getDevices().size() > 0) {
            int index = Utils.getObjectIndexFromList(mWatchDog.getDevices().get(0), getDevicesArray(DEVICES_SENSORS));
            if(index > -1) mIfItemSpinner.setSelection(mSpinnerMultiAdapter.getRealPosition(index, SpinnerItem.SpinnerItemType.DEVICE));
        }

        // get parameter action value
        String par_action_value = mWatchDog.getParam(WatchDog.PAR_ACTION_VALUE);
        // based on action type set different part of GUI
        switch(mWatchDog.getAction()){
            case WatchDog.ACTION_NOTIFICATION:
                mActionType.check(R.id.watchdog_edit_notification);
                if(par_action_value != null) mNotificationText.setText(par_action_value);
                break;

            case WatchDog.ACTION_ACTOR:
                mActionType.check(R.id.watchdog_edit_actor);
                int actorIndex = par_action_value == null ? -1 : Utils.getObjectIndexFromList(par_action_value, getDevicesArray(DEVICES_ACTORS));
                if(actorIndex > -1) mActorSpinner.setSelection(actorIndex);
                break;
        }

        // setup operators icon
        String par_operator = mWatchDog.getParam(WatchDog.PAR_OPERATOR);
        if(par_operator != null){
            mWatchDogOperator.setByType(par_operator);
            mOperatorButton.setImageResource(mWatchDogOperator.getIconResource());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(EXTRA_ADAPTER_ID, mActiveAdapterId);
        savedInstanceState.putString(EXTRA_RULE_ID, mActiveRuleId);
        savedInstanceState.putBoolean(EXTRA_IS_NEW, mIsNew);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Hides delete button when we have new watchdog
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.watchdog_edit_menu, menu);
        mOptionsMenu = menu;

        onUpdateOptionsMenu();
        return true;
    }

    /**
     * Called when want to show delete icon
     */
    private void onUpdateOptionsMenu(){
        if(mOptionsMenu == null) return;

        MenuItem deleteActionButton = mOptionsMenu.findItem(R.id.wat_menu_delete);
        deleteActionButton.setVisible(mIsNew ? false : true);
    }

    /**
     * Clicked on actionbar button (delete / save)
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.wat_menu_save:
                doSaveWatchDogTask();
                break;
            case R.id.wat_menu_delete:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder
                    .setMessage(R.string.rule_delete_dialog)
                    .setPositiveButton(R.string.rule_menu_del, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            doRemoveWatchDogTask();
                        }
                    })
                    .setNegativeButton(R.string.notification_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // ---------- ASYNC TASKS ---------- //

    /**
     * Async task for saving watchdog
     */
    private void doSaveWatchDogTask(){
        if(!validateInput(mRuleName, getString(R.string.toast_field_must_be_filled))){
            return;
        }

        ArrayList<String> devsIds = new ArrayList<>();
        ArrayList<String> newParams = new ArrayList<>();

        String tresholdValue = "";

        SpinnerItem selected = mSpinnerMultiAdapter.getItem(mIfItemSpinner.getSelectedItemPosition());
        switch(selected.getType()){
            case DEVICE:
                if(!validateInput(mRuleTreshold, getString(R.string.toast_field_must_be_filled))) return;

                Device selectedDevice = (Device) selected.getObject();
                devsIds.add(selectedDevice.getId());
                newParams.add(selectedDevice.getId());

                tresholdValue = mRuleTreshold.getText().toString();
            break;

            case GEOFENCE:
                SimpleGeofence selectedGeofence = (SimpleGeofence) selected.getObject();
                mWatchDog.setGeoRegionId(selectedGeofence.getId());
                newParams.add(selectedGeofence.getId());
            break;

            default:
                Toast.makeText(this, getString(R.string.toast_not_implemented), Toast.LENGTH_LONG).show();
                return;
        }

        // operator
        newParams.add(mWatchDogOperator.getCode());
        // treshold
        newParams.add(tresholdValue);
        // action type
        newParams.add(mWatchDogAction);
        switch(mWatchDogAction){
            case WatchDog.ACTION_NOTIFICATION:
                if(!validateInput(mNotificationText, getString(R.string.toast_field_must_be_filled))) return;

                newParams.add(mNotificationText.getText().toString());
                break;

            case WatchDog.ACTION_ACTOR:
                if(mActorSpinner.getSelectedItem() == null){
                    Toast.makeText(this, getString(R.string.actor_required), Toast.LENGTH_LONG).show();
                    return;
                }

                Device selectedActor = getDevicesArray(DEVICES_ACTORS).get(mActorSpinner.getSelectedItemPosition());
                newParams.add(selectedActor.getId());
                break;
        }

        mWatchDog.setOperatorType(mWatchDogOperator);
        mWatchDog.setParams(newParams);
        mWatchDog.setDevices(devsIds);

        mWatchDog.setName(mRuleName.getText().toString());
        mWatchDog.setEnabled(mRuleEnabled.isChecked());

        mProgress.show();

        // TODO check if data were changed ???
        mSaveWatchDogTask = new SaveWatchDogTask(this);
        mSaveWatchDogTask.setListener(new CallbackTask.CallbackTaskListener() {
            @Override
            public void onExecute(boolean success) {
                if (mProgress != null) mProgress.dismiss();
                Toast.makeText(WatchDogEditRuleActivity.this, getResources().getString(success ? R.string.toast_success_save_data : R.string.toast_fail_save_data), Toast.LENGTH_LONG).show();

                // when new rule, close after done
                if(success && mIsNew) finish();
            }
        });

        mSaveWatchDogTask.execute(mWatchDog);
    }

    /**
     * Async task for deleting watchDog
     */
    private void doRemoveWatchDogTask() {
        mRemoveWatchDogTask = new RemoveWatchDogTask(this, false);
        DelWatchDogPair pair = new DelWatchDogPair(mWatchDog.getId(), mWatchDog.getAdapterId());

        mRemoveWatchDogTask.setListener(new CallbackTask.CallbackTaskListener() {
            @Override
            public void onExecute(boolean success) {
                Toast.makeText(WatchDogEditRuleActivity.this, getResources().getString(success ? R.string.toast_delete_success : R.string.toast_delete_fail), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        mRemoveWatchDogTask.execute(pair);
    }

    // ---------- HELPER FUNCTIONS ---------- //

    /**
     * Helper function for getting type of devices from list of facilities
     * @param type DEVICES_ALL | DEVICES_ACTORS | DEVICES_SENSORS
     * @return
     */
    private List<Device> getDevicesArray(int type){
        if(type == DEVICES_ACTORS && _actors != null) {
            return _actors;
        }
        else if(type == DEVICES_SENSORS && _sensors != null) {
            return _sensors;
        }

        List<Device> devices = new ArrayList<Device>();

        for(Facility facility : mFacilities)
            for (Device device : facility.getDevices()) {
                if (type == DEVICES_ACTORS && !device.getType().isActor()) {
                    continue;
                }
                else if(type == DEVICES_SENSORS && device.getType().isActor()) {
                    continue;
                }

                devices.add(device);
            }

        if(type == DEVICES_ACTORS) {
            _actors = devices;
        }
        else if(type == DEVICES_SENSORS) {
            _sensors = devices;
        }

        return devices;
    }

    /**
     * Helper function for validating EditText
     * @param eText
     * @param msg
     * @return
     */
    private static boolean validateInput(EditText eText, String msg) {
        if (eText.getText().toString().trim().length() == 0) {
            eText.setError(msg);
            return false;
        }
        return true;
    }

    @Override
    protected void onAppResume() {

    }

    @Override
    protected void onAppPause() {

    }
}

