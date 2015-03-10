package com.rehivetech.beeeon.activity;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.adapter.Adapter;
import com.rehivetech.beeeon.adapter.WatchDogRule;
import com.rehivetech.beeeon.adapter.device.Device;
import com.rehivetech.beeeon.adapter.device.DeviceType;
import com.rehivetech.beeeon.adapter.device.Facility;
import com.rehivetech.beeeon.adapter.device.values.HumidityValue;
import com.rehivetech.beeeon.adapter.location.Location;
import com.rehivetech.beeeon.arrayadapter.DeviceArrayAdapter;
import com.rehivetech.beeeon.asynctask.ReloadFacilitiesTask;
import com.rehivetech.beeeon.base.BaseApplicationActivity;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.UnitsHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class WatchDogEditRuleActivity extends BaseApplicationActivity {
    private static final String TAG = WatchDogEditRuleActivity.class.getSimpleName();

    public static final String EXTRA_ADAPTER_ID = "adapter_id";
    public static final String EXTRA_RULE_ID = "rule_id";

    // extras
    private String mActiveAdapterId;
    private String mActiveRuleId;

    private Controller mController;
    private Toolbar mToolbar;

    private boolean mIsNew = false;
    private WatchDogRule mRule;
    private Adapter mAdapter;
    private boolean mIsValueLess = true;        // value for View FAB
    private UnitsHelper mUnitsHelper;

    private List<Facility> mFacilities;
    private ReloadFacilitiesTask mReloadFacilitiesTask;

    private ProgressDialog mProgress;

    // GUI elements
    private RadioGroup mActionType;
    private EditText mRuleName;
    private TextView mRuleTresholdUnit;
    private SwitchCompat mRuleEnabled;
    private Spinner mSensorSpinner;
    private FloatingActionButton mGreatLessButton;
    private EditText mRuleTreshold;
    private SharedPreferences mPrefs;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watchdog_edit_rule);

        // prepare toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            mToolbar.setTitle(R.string.watchdog_rule);
            setSupportActionBar(mToolbar);
            setActionBarLayout();
        }

        // Prepare progress dialog
        mProgress = new ProgressDialog(this);
        mProgress.setMessage(getString(R.string.progress_saving_data));
        mProgress.setCancelable(false);
        mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        Log.d(TAG, "onCreate()");

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            mActiveAdapterId = bundle.getString(EXTRA_ADAPTER_ID);
            mActiveRuleId = bundle.getString(EXTRA_RULE_ID);
        }
        else{
            bundle = savedInstanceState;
            if (bundle != null) {
                mActiveAdapterId = bundle.getString(EXTRA_ADAPTER_ID);
                mActiveRuleId = bundle.getString(EXTRA_RULE_ID);
            }
            else{
                mIsNew = true;
            }
        }

        // TODO muze byt adapter null?
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

        // get watchdog rule
        // TODO ziskat ze site
        HumidityValue val = new HumidityValue();
        val.setValue("50");
        Device dev = new Device(DeviceType.TYPE_HUMIDITY, val);
        dev.setName("Vlhkostní sensor");

        if(!mIsNew)
            mRule = new WatchDogRule("2", mActiveAdapterId, "Hlídání smradu", dev, WatchDogRule.OperatorType.GREATER, WatchDogRule.ActionType.NOTIFICATION, val, false);


        if(mRule != null){
            initLayout(mRule);
        }
    }

    private List<Device> getDevicesArray(){
        List<Facility> facilities = new ArrayList<Facility>();
        facilities = mController.getFacilitiesByAdapter(mAdapter.getId());

        List<Device> devices = new ArrayList<Device>();
        for(Facility facility : facilities){
            devices.addAll(facility.getDevices());
        }

        // Sort them
        Collections.sort(devices);
        return devices;
    }

    private void initLayout(WatchDogRule rule) {
        //mFacilities = mController.getFacilitiesByAdapter(mController.getActiveAdapter().getId());

        mRuleName = (EditText) findViewById(R.id.watchdog_edit_name);
        mRuleEnabled = (SwitchCompat) findViewById(R.id.watchdog_edit_switch);
        mSensorSpinner = (Spinner) findViewById(R.id.watchdog_edit_sensor_spinner);
        mGreatLessButton = (FloatingActionButton) findViewById(R.id.watchdog_edit_greatless);
        mRuleTreshold = (EditText) findViewById(R.id.watchdog_edit_treshold);
        mRuleTresholdUnit = (TextView) findViewById(R.id.watchdog_edit_treshold_unit);
        mActionType = (RadioGroup) findViewById(R.id.watchdog_edit_action_radiogroup);

        // set layout components
        mRuleName.setText(rule.getName());
        mRuleEnabled.setChecked(rule.getEnabled());
        setGreatLess(rule.getOperator(), mGreatLessButton);

        if(mUnitsHelper != null){
            mRuleTreshold.setText(mUnitsHelper.getStringValue(rule.getTreshold()));
            mRuleTresholdUnit.setText(mUnitsHelper.getStringUnit(rule.getTreshold()));
        }
        else{
            mRuleTreshold.setText(String.valueOf(rule.getTreshold().getDoubleValue()));
            mRuleTresholdUnit.setText("?");
        }

        String[] DayOfWeek = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};


        final DeviceArrayAdapter dataAdapter = new DeviceArrayAdapter(this, R.layout.custom_spinner2_item, getDevicesArray(), getLocationsArray());
        dataAdapter.setLayoutInflater(getLayoutInflater());
        dataAdapter.setDropDownViewResource(R.layout.custom_spinner2_dropdown_item);
        mSensorSpinner.setAdapter(dataAdapter);

        mSensorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Device selectedDevice = dataAdapter.getItem(position);

                mRuleTresholdUnit.setText(mUnitsHelper.getStringUnit(selectedDevice.getValue()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.custom_spinner2_dropdown_item, R.id.custom_spinner_dropdown_label, DayOfWeek);
//        mSensorSpinner.setAdapter(adapter);

        // changing mIsValueLess onClick
        // TODO make value not boolean but OperatorType
        mGreatLessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageButton img = (ImageButton) v;
                img.setImageResource(mIsValueLess ? R.drawable.ic_action_next_item : R.drawable.ic_action_previous_item);
                mIsValueLess = !mIsValueLess;
            }
        });


        // TODO udelat nejak,ze ziskat pres parametry + asi include layout pro to
        EditText notificationText = (EditText) findViewById(R.id.watchdog_edit_notification_text);
        Spinner actorSpinner = (Spinner) findViewById(R.id.watchdog_edit_actor_spinner);

        // changing specified layout when checked
        mActionType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RelativeLayout NotifLayout = (RelativeLayout) findViewById(R.id.watchdog_detail_notification);
                RelativeLayout ActionLayout = (RelativeLayout) findViewById(R.id.watchdog_detail_actor);

                switch(checkedId){
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
    }

    private List<Location> getLocationsArray() {
        // Get locations from adapter
        List<Location> locations = new ArrayList<Location>();

        Adapter adapter = mController.getActiveAdapter();
        if (adapter != null) {
            locations = mController.getLocations(adapter.getId());
        }

        // Sort them
        Collections.sort(locations);

        return locations;
    }

    /*
    private WatchDogRule.OperatorType changeOperator(WatchDogRule.OperatorType op){
        return WatchDogRule.OperatorType.values()[(WatchDogRule.OperatorType.ordinal()+1) % WatchDogRule.OperatorType.values().length];
    }
    //*/

    private void setGreatLess(WatchDogRule.OperatorType operator, ImageButton glButton) {
        switch(operator){
            case GREATER:
                mIsValueLess = false;
                glButton.setImageResource(R.drawable.ic_action_next_item);
                break;
            case SMALLER:
                mIsValueLess = true;
                glButton.setImageResource(R.drawable.ic_action_previous_item);
                break;
        }
    }


    @Override
    protected void onAppResume() {

    }

    @Override
    protected void onAppPause() {

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(EXTRA_ADAPTER_ID, mActiveAdapterId);
        savedInstanceState.putString(EXTRA_RULE_ID, mActiveRuleId);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Sets actionbar as two buttons layout -> Done, Cancel
     */
    private void setActionBarLayout(){
        // set actionMode with done and cancel button
        final LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        final View customActionBarView = inflater.inflate(R.layout.actionbar_add_activity, null);
        customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Done

                    }
                });
        customActionBarView.findViewById(R.id.actionbar_cancel).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // "Cancel"
                        finish();
                    }
                });

        // Show the custom action bar view and hide the normal Home icon and title.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(customActionBarView, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }
}

