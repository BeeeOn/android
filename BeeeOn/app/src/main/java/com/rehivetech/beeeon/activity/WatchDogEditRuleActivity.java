package com.rehivetech.beeeon.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.rehivetech.beeeon.adapter.Adapter;
import com.rehivetech.beeeon.adapter.watchdog.WatchDog;
import com.rehivetech.beeeon.adapter.device.Device;
import com.rehivetech.beeeon.adapter.device.Facility;
import com.rehivetech.beeeon.adapter.location.Location;
import com.rehivetech.beeeon.arrayadapter.DeviceArrayAdapter;
import com.rehivetech.beeeon.arrayadapter.SpinnerMultiAdapter;
import com.rehivetech.beeeon.asynctask.CallbackTask;
import com.rehivetech.beeeon.asynctask.ReloadFacilitiesTask;
import com.rehivetech.beeeon.asynctask.RemoveWatchDogTask;
import com.rehivetech.beeeon.asynctask.SaveWatchDogTask;
import com.rehivetech.beeeon.base.BaseApplicationActivity;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.geofence.SimpleGeofence;
import com.rehivetech.beeeon.pair.DelWatchDogPair;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.UnitsHelper;
import com.rehivetech.beeeon.util.Utils;

import java.util.ArrayList;
import java.util.List;

/*
interface WatchDogOperator{
    public enum WatchDogOperatorType{
        SENSOR, GEOFENCE
    }

    public WatchDogOperatorType getType();

    //public void onChanged(SpinnerItem item);

    public View.OnClickListener getOperatorButtonClickListener(boolean opState);
};

class GeofenceOperator implements WatchDogOperator{

    public static final int[] geofenceOperators = {
            R.drawable.ic_in,
            R.drawable.ic_out
    };

    @Override
    public WatchDogOperatorType getType() {
        return WatchDogOperatorType.GEOFENCE;
    }

    @Override
    public View.OnClickListener getOperatorButtonClickListener(final boolean opState) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageButton img = (ImageButton) v;
                img.setImageResource(opState ? R.drawable.ic_out : R.drawable.ic_in);
                //opState = !opState;
            }
    };
}
//*/

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

    // async tasks
    private ReloadFacilitiesTask mReloadFacilitiesTask;
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

        // get controller
        mController = Controller.getInstance(this);
        // get adapter
        mAdapter = mActiveAdapterId == null ? mController.getActiveAdapter() : mController.getAdapter(mActiveAdapterId);

        // UserSettings can be null when user is not logged in!
        mPrefs = mController.getUserSettings();
        mUnitsHelper = (mPrefs == null) ? null : new UnitsHelper(mPrefs, this);

        // get all locations for spinners
        mLocations = mController.getLocations(mAdapter.getId());
        // get all geofence areas
        mGeofences = mController.getAllGeofences();

        // facilities get by cycling through all locations
        mFacilities = new ArrayList<Facility>();
        for(Location loc : mLocations){
            List<Facility> tempFac = mController.getFacilitiesByLocation(mAdapter.getId(), loc.getId());
            mFacilities.addAll(tempFac);
        }

        // get watchdog rule
        if(!mIsNew) {
            mWatchDog = mController.getWatchDog(mAdapter.getId(), mActiveRuleId);
            if(mWatchDog == null){
                Toast.makeText(this, R.string.toast_something_wrong, Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }
        else{
            mWatchDog = new WatchDog(WatchDog.TYPE_SENSOR);
        }

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
        mIfItemSpinner.setSelection(mSpinnerMultiAdapter.getRealPosition(0));

        mIfItemSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SpinnerItem selected = mSpinnerMultiAdapter.getItem(position);

                switch (selected.getType()) {
                    case DEVICE:
                        setupSensorWatchdog(selected);
                        break;

                    case GEOFENCE:
                        setupGeofenceWatchdog(selected);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

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
                        break;

                    case R.id.watchdog_edit_actor:
                        NotifLayout.setVisibility(View.GONE);
                        ActionLayout.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });

        // first checked
        RadioButton radioNotification = (RadioButton) findViewById(R.id.watchdog_edit_notification);
        radioNotification.setChecked(true);

        // ------- choose actor
        final DeviceArrayAdapter actorAdapter = new DeviceArrayAdapter(this, R.layout.custom_spinner2_item, getDevicesArray(DEVICES_ACTORS), mLocations);
        actorAdapter.setLayoutInflater(getLayoutInflater());
        actorAdapter.setDropDownViewResource(R.layout.custom_spinner2_dropdown_item);
        mActorSpinner.setAdapter(actorAdapter);
    }

    private void setupSensorWatchdog(SpinnerItem selected){
        mOperatorButton.setImageResource(mWatchDog.getOperatorType().getIconResource());
        mRuleTreshold.setVisibility(View.VISIBLE);
        mRuleTresholdUnit.setVisibility(View.VISIBLE);
        // senzors can have only numbers
        mRuleTreshold.setInputType(InputType.TYPE_CLASS_NUMBER);

        if(mUnitsHelper != null){
            Device selectedDevice = (Device) selected.getObject();
            mRuleTresholdUnit.setText(mUnitsHelper.getStringUnit(selectedDevice.getValue()));
        }

        mOperatorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageButton img = (ImageButton) v;
                img.setImageResource(mWatchDog.getOperatorType().next().getIconResource());
            }
        });
    }

    private void setupGeofenceWatchdog(SpinnerItem selected){
        mOperatorButton.setImageResource(mWatchDog.getOperatorType().getIconResource());
        mRuleTreshold.setVisibility(View.GONE);
        mRuleTresholdUnit.setVisibility(View.GONE);

        SimpleGeofence selectedGeofence = (SimpleGeofence) selected.getObject();

        mOperatorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageButton img = (ImageButton) v;
                img.setImageResource(mWatchDog.getOperatorType().next().getIconResource());
            }
        });
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

        int index = Utils.getObjectIndexFromList(mWatchDog.getDevices().get(0), getDevicesArray(DEVICES_SENSORS));
        if(index > -1) mIfItemSpinner.setSelection(mSpinnerMultiAdapter.getRealPosition(index));

        // TODO toto je spatne nemuze byt typ

        mActionType.check(R.id.watchdog_edit_notification);
        String par_value = mWatchDog.getParam(WatchDog.PAR_ACTION_VALUE);
        if(par_value != null) mNotificationText.setText(par_value);
        /*
        switch(mWatchDog.getType()){
            case WatchDog.ACTION_NOTIFICATION:
                mActionType.check(R.id.watchdog_edit_notification);
                mNotificationText.setText(mWatchDog.getParams().get(WatchDog.PAR_ACTION_VALUE));
                break;

            case WatchDog.ACTION_ACTOR:
                mActionType.check(R.id.watchdog_edit_actor);
                // TODO - vybrat actor
                break;
        }
        //*/

        // TODO obecne
        mWatchDog.getOperatorType().setByType(mWatchDog.getParams().get(WatchDog.PAR_OPERATOR));
        mOperatorButton.setImageResource(mWatchDog.getOperatorType().getIconResource());
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
     * TODO revise this
     */
    private void doSaveWatchDogTask(){
        if(!validateInput(mRuleName, "This must be filled!") || !validateInput(mRuleTreshold, "This must be filled!")){
            return;
        }

        ArrayList<String> devsIds = new ArrayList<>();
        ArrayList<String> newParams = new ArrayList<String>();

        SpinnerItem selected = mSpinnerMultiAdapter.getItem(mIfItemSpinner.getSelectedItemPosition());
        switch(selected.getType()){
            case DEVICE:
                Device selectedDevice = (Device) selected.getObject();
                devsIds.add(selectedDevice.getId());
                newParams.add(selectedDevice.getId());
            break;

            default:
                Toast.makeText(this, "NOT IMPLEMENTED", Toast.LENGTH_LONG).show();
                return;
        }

        // operator
        newParams.add(mWatchDog.getOperatorType().getCode());
        // treshold
        newParams.add(mRuleTreshold.getText().toString());
        // notification text
        newParams.add(mNotificationText.getText().toString());

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
                if(success){
                    mIsNew = false;
                    onUpdateOptionsMenu();
                }
                Toast.makeText(WatchDogEditRuleActivity.this, getResources().getString(success ? R.string.toast_success_save_data : R.string.toast_fail_save_data), Toast.LENGTH_LONG).show();
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

