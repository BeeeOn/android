package com.rehivetech.beeeon.activity;

import android.os.Bundle;
import android.view.View;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.base.BaseApplicationActivity;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.util.Log;

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
    }

    @Override
    protected void onAppResume() {

    }

    @Override
    protected void onAppPause() {

    }
}
