package com.rehivetech.beeeon.activity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.adapter.device.Facility;
import com.rehivetech.beeeon.asynctask.ReloadFacilitiesTask;
import com.rehivetech.beeeon.base.BaseApplicationActivity;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.util.Log;

import java.util.List;


public class WatchDogEditRuleActivity extends BaseApplicationActivity {
    private static final String TAG = WatchDogEditRuleActivity.class.getSimpleName();

    private static final String ADAPTER_ID = "lastAdapterId";
    private String mActiveAdapterId;

    private Controller mController;
    private Toolbar mToolbar;

    private List<Facility> mFacilities;
    private ReloadFacilitiesTask mReloadFacilitiesTask;

    RadioGroup mActionType;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watchdog_edit_rule);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            mToolbar.setTitle(R.string.watchdog_rule);
            setSupportActionBar(mToolbar);
            setActionBarLayout();
        }

        //getSupportActionBar().setHomeButtonEnabled(true);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mController = Controller.getInstance(this);

        Log.d(TAG, "onCreate()");

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            mActiveAdapterId = savedInstanceState.getString(ADAPTER_ID);
        }
        else{
            bundle = savedInstanceState;
        }

        //mFacilities = mController.getFacilitiesByAdapter(mController.getActiveAdapter().getId());

        EditText ruleName = (EditText) findViewById(R.id.watchdog_edit_name);
        SwitchCompat ruleActivate = (SwitchCompat) findViewById(R.id.watchdog_edit_switch);
        Spinner sensorSpinner = (Spinner) findViewById(R.id.watchdog_edit_sensor_spinner);
        FloatingActionButton glButton = (FloatingActionButton) findViewById(R.id.watchdog_edit_greatless);
        EditText ruleTreshold = (EditText) findViewById(R.id.watchdog_edit_treshold);
        TextView ruleTresholdValue = (TextView) findViewById(R.id.watchdog_edit_treshold_unit);
        mActionType = (RadioGroup) findViewById(R.id.watchdog_edit_action_radiogroup);

        EditText notificationText = (EditText) findViewById(R.id.watchdog_edit_notification_text);
        Spinner actorSpinner = (Spinner) findViewById(R.id.watchdog_edit_actor_spinner);

        String[] xxx = {
            "Sensor1",
                "Sensor2",
                "Sensor XXx"
        };
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, xxx);

        //ArrayAdapter<Facility> dataAdapter = new ArrayAdapter<Facility>(this, android.R.layout.simple_spinner_dropdown_item);

        sensorSpinner.setAdapter(dataAdapter);



        glButton.setOnClickListener(new View.OnClickListener() {
            boolean isLess = true;

            @Override
            public void onClick(View v) {
                ImageView img = (ImageView) v;
                Drawable drawable = img.getDrawable();

                img.setImageResource(isLess ? R.drawable.ic_action_next_item : R.drawable.ic_action_previous_item);
                isLess = !isLess;
            }
        });

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



    @Override
    protected void onAppResume() {

    }

    @Override
    protected void onAppPause() {

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

