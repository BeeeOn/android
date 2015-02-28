package com.rehivetech.beeeon.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Spinner;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.adapter.Adapter;
import com.rehivetech.beeeon.adapter.location.Location;
import com.rehivetech.beeeon.arrayadapter.LocationArrayAdapter;
import com.rehivetech.beeeon.base.BaseApplicationActivity;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Tomáš on 26. 2. 2015.
 */
public class WatchDogDetailActivity extends BaseApplicationActivity {
    private static final String TAG = WatchDogDetailActivity.class.getSimpleName();

    private Controller mController;

    private View mView;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watchdog_detail);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_launcher_null);

        // Get controller
        mController = Controller.getInstance(getApplicationContext());

        Log.d(TAG, "onCreate()");

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){

        }
        else{
            bundle = savedInstanceState;
        }

        //LocationArrayAdapter dataAdapter = new LocationArrayAdapter(this, R.layout.custom_spinner_item, getLocationsArray());

        Spinner mSpinnerSensor = (Spinner) findViewById(R.id.watchdogDetailSpinnerChooseSensor);
        //mSpinnerSensor.setAdapter(dataAdapter);
    }

    @Override
    protected void onAppResume() {

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

    @Override
    protected void onAppPause() {

    }
}
