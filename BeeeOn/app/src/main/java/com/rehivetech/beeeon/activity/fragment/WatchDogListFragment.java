package com.rehivetech.beeeon.activity.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.activity.MainActivity;
import com.rehivetech.beeeon.activity.WatchDogDetailActivity;
import com.rehivetech.beeeon.adapter.Adapter;
import com.rehivetech.beeeon.arrayadapter.WatchDogListAdapter;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.thread.ToastMessageThread;
import com.rehivetech.beeeon.util.Log;

import java.sql.Struct;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by Tomáš on 23. 2. 2015.
 */
public class WatchDogListFragment extends SherlockFragment{
    private static final String TAG = WatchDogListFragment.class.getSimpleName();

    private MainActivity mActivity;
    private Controller mController;
    private ListView mWatchDogListView;
    private WatchDogListAdapter mWatchDogAdapter;

    private View mView;

    // mod actionbaru
    private ActionMode mMode;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        mActivity = (MainActivity) getSherlockActivity();
        mController = Controller.getInstance(mActivity);
    }

    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");

        if(mMode != null) {
            mMode.finish();
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        mView = inflater.inflate(R.layout.fragment_watchdog, container, false);

        mWatchDogListView = (ListView) mView.findViewById(R.id.watchdogListView);

        // pokusny seznam
        List<WatchDogListAdapter.WRule> rulesList = new ArrayList<>();
        rulesList.add(new WatchDogListAdapter.WRule("První senzor", "<", WatchDogListAdapter.ActionType.ACTOR_ACTION, "30°C", true));
        rulesList.add(new WatchDogListAdapter.WRule("PDruhý senzor", ">", WatchDogListAdapter.ActionType.NOTIFICATION, "90%", true));
        rulesList.add(new WatchDogListAdapter.WRule("Třetí senzor", ">", WatchDogListAdapter.ActionType.ACTOR_ACTION, "60LUX", false));
        rulesList.add(new WatchDogListAdapter.WRule("Čtvrtý senzor", "<", WatchDogListAdapter.ActionType.NOTIFICATION, "30°C", true));

        mWatchDogAdapter = new WatchDogListAdapter(mActivity, rulesList, getActivity().getLayoutInflater());

        mWatchDogListView.setAdapter(mWatchDogAdapter);
        mWatchDogListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(mActivity, WatchDogDetailActivity.class);
                startActivity(intent);
            }
        });

        mWatchDogListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                // TODO treba zmenit switcher na checkboxy ?

                mMode = getSherlockActivity().startActionMode(new ActionModeEditRules());
                return true;
            }
        });

        ImageView addButton = (ImageView) mView.findViewById(R.id.watchdogAddButton);
        addButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ImageView img = (ImageView) v;
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN){
                    img.setImageResource(R.drawable.ic_add_pressed);
                }else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL){
                    img.setImageResource(R.drawable.ic_add);

                    // zapne aktivitu
                    Intent intent = new Intent(mActivity, WatchDogDetailActivity.class);
                    startActivity(intent);

                    // v.performClick(); // pokud bude takto, pak funguje normalne setOnClickListener
                }
                return true;
            }
        });

        return mView;
    }

    public void prepareDevices(){
        Adapter adapter = mController.getActiveAdapter();
        if(adapter == null) return;

    }

    public void onDestroyActionMode(ActionMode mode) {
        mMode = null;
    }

    class ActionModeEditRules implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.watchdoglist_actionmode, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getTitle().equals(getResources().getString(R.string.action_hide_sensor))) {
                // doHideSensorTask(mDeviceHide);
            } else if (item.getTitle().equals(getResources().getString(R.string.action_hide_facility))) {
                new ToastMessageThread(mActivity, R.string.toast_not_implemented).start();
            } else if (item.getTitle().equals(getResources().getString(R.string.action_unregist_facility))) {
                new ToastMessageThread(mActivity, R.string.toast_not_implemented).start();
            }

            mode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mMode = null;

        }
    }

}
