package com.rehivetech.beeeon.activity.fragment;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.activity.MainActivity;
import com.rehivetech.beeeon.activity.WatchDogEditRuleActivity;
import com.rehivetech.beeeon.adapter.Adapter;
import com.rehivetech.beeeon.adapter.WatchDog;
import com.rehivetech.beeeon.arrayadapter.WatchDogListAdapter;
import com.rehivetech.beeeon.asynctask.CallbackTask;
import com.rehivetech.beeeon.asynctask.ReloadWatchDogsTask;
import com.rehivetech.beeeon.asynctask.RemoveWatchDogTask;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.util.Log;

import java.util.ArrayList;
import net.i2p.android.ext.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * Fragment for list of rules for algorithm WatchDog
 * @author mlyko
 */
public class WatchDogListFragment extends Fragment{
    private static final String TAG = WatchDogListFragment.class.getSimpleName();

    private static final String ADAPTER_ID = "lastAdapterId";

    private SwipeRefreshLayout mSwipeLayout;
    private MainActivity mActivity;
    private Controller mController;
    private ListView mWatchDogListView;
    private WatchDogListAdapter mWatchDogAdapter;

    List<WatchDog> mWatchDogs;

    private String mActiveAdapterId;

    private View mView;
    private ActionMode mMode;

    private boolean isFirstLoading = true;

    /**
     * Initialize variables
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        mActivity = (MainActivity) getActivity();
        if (!(mActivity instanceof MainActivity)) {
            throw new IllegalStateException("Activity holding SensorListFragment must be MainActivity");
        }

        mController = Controller.getInstance(mActivity);

        if (savedInstanceState != null) {
            mActiveAdapterId = savedInstanceState.getString(ADAPTER_ID);
        }
        else{
            // TODO v seznamu sensoru je adapter nastavovan z MainActivity
            mActiveAdapterId = mController.getActiveAdapter().getId();
        }

        // TODO tutorial zobrazit pri prvnim pouziti
    }

    /**
     * When view created, get rules, redraw gui
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return View
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        mView = inflater.inflate(R.layout.fragment_watchdog, container, false);
        initLayout();
        return mView;
    }

    /**
     * Init swipe-refreshing
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated()");

        // Init swipe-refreshig layout
        mSwipeLayout = (SwipeRefreshLayout) mActivity.findViewById(R.id.swipe_container);
        if (mSwipeLayout == null) {
            return;
        }
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {

                Adapter adapter = mController.getActiveAdapter();
                if (adapter == null) {
                    mSwipeLayout.setRefreshing(false);
                    return;
                }
                // TODO add reload async task
                // mActivity.redrawMenu();
                doReloadWatchDogsTask(adapter.getId());
            }
        });

        mSwipeLayout.setColorSchemeColors(R.color.beeeon_primary_cyan, R.color.beeeon_text_color, R.color.beeeon_secundary_pink);

        // if we don't have any data first time, try to reload
        // TODO resolve it
        if(isFirstLoading){ // && mWatchDogs == null
            mSwipeLayout.post(new Runnable() {
                public void run() {
                    mSwipeLayout.setRefreshing(true);
                    doReloadWatchDogsTask(mActiveAdapterId);
                }
            });
            isFirstLoading = false;
        }
    }

    /**
     * Finish actionMode
     */
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");

        if(mMode != null) {
            mMode.finish();
        }
    }

    /**
     * Cancels async task
     */
    public void onDestroy(){
        super.onDestroy();
        Log.d(TAG, "onDestroy()");

        // TODO cancel async task here
        /*
        if (mReloadFacilitiesTask != null) {
            mReloadFacilitiesTask.cancel(true);
        }
        //*/
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putString(ADAPTER_ID, mActiveAdapterId);
        super.onSaveInstanceState(savedInstanceState);
    }

    public void doReloadWatchDogsTask(String adapterId){
        ReloadWatchDogsTask mRel = new ReloadWatchDogsTask(getActivity().getApplicationContext(), true);

        mRel.setListener(new CallbackTask.CallbackTaskListener() {
            @Override
            public void onExecute(boolean success) {
                redrawRules();
                mSwipeLayout.setRefreshing(false);
            }
        });

        mRel.execute(adapterId);
    }

    private void initLayout() {
        mWatchDogListView = (ListView) mView.findViewById(R.id.watchdog_list);
        mWatchDogAdapter = new WatchDogListAdapter(mActivity, getActivity().getLayoutInflater());
        mWatchDogListView.setAdapter(mWatchDogAdapter);

        // ---- when listview is empty
        TextView emptyView = (TextView) mView.findViewById(R.id.watchdog_list_empty);
        mWatchDogListView.setEmptyView(emptyView);

        // ---- floating action button
        FloatingActionButton fab = (FloatingActionButton) mView.findViewById(R.id.fab);
        fab.attachToListView(mWatchDogListView);
        fab.show();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, WatchDogEditRuleActivity.class);
                startActivity(intent);
            }
        });

        // ----- onitemclick
        mWatchDogListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WatchDog rule = mWatchDogAdapter.getRule(position);

                Bundle bundle = new Bundle();
                bundle.putString(WatchDogEditRuleActivity.EXTRA_ADAPTER_ID, rule.getAdapterId());
                bundle.putString(WatchDogEditRuleActivity.EXTRA_RULE_ID, rule.getId());

                Intent intent = new Intent(mActivity, WatchDogEditRuleActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        // ---- on long click
        mWatchDogListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                // TODO treba zmenit switcher na checkboxy ?

                mMode = mActivity.startSupportActionMode(new ActionModeEditRules());
                return true;
            }
        });

        // ---- on switch checked
        //mWatchDogAdapter.onCheckedChanged();

        redrawRules();
    }

    /**
     * Redraw GUI rules
     */
    private void redrawRules() {
        mWatchDogs = mController.getAllWatchDogs(mActiveAdapterId);

        Log.d(TAG, String.format("watchdogs length: %d", mWatchDogs.size()));
        for(WatchDog w : mWatchDogs ){
            Log.d(TAG, String.format("Watch: %s", w.getName()));
        }

        mWatchDogAdapter.updateData(mWatchDogs);

        /*
        HumidityValue val = new HumidityValue();
        val.setValue("50");
        Device dev = new Device(DeviceType.TYPE_HUMIDITY, val);
        dev.setName("Vlhkost ve sklepě");

        TemperatureValue val1 = new TemperatureValue();
        val1.setValue("32");

        List<WatchDogRule> rulesList = new ArrayList<>();
        rulesList.add(new WatchDogRule("1", mActiveAdapterId, "Hlídání ohně", dev, WatchDogRule.OperatorType.GREATER, WatchDogRule.ActionType.ACTOR_ACTION, val1, false));
        rulesList.add(new WatchDogRule("2", mActiveAdapterId, "Hlídání smradu", dev, WatchDogRule.OperatorType.SMALLER, WatchDogRule.ActionType.NOTIFICATION, val, true));
        rulesList.add(new WatchDogRule("3", mActiveAdapterId, "Hlídání dětí", dev, WatchDogRule.OperatorType.GREATER, WatchDogRule.ActionType.NOTIFICATION, val1, true));
        rulesList.add(new WatchDogRule("4", mActiveAdapterId, "Hlídání cen", dev, WatchDogRule.OperatorType.SMALLER, WatchDogRule.ActionType.ACTOR_ACTION, val, false));
        rulesList.add(new WatchDogRule("5", mActiveAdapterId, "Hlídání cen", dev, WatchDogRule.OperatorType.SMALLER, WatchDogRule.ActionType.ACTOR_ACTION, val, false));
        rulesList.add(new WatchDogRule("6", mActiveAdapterId, "Hlídání cen", dev, WatchDogRule.OperatorType.SMALLER, WatchDogRule.ActionType.ACTOR_ACTION, val, false));
        rulesList.add(new WatchDogRule("7", mActiveAdapterId, "Hlídání cen", dev, WatchDogRule.OperatorType.SMALLER, WatchDogRule.ActionType.ACTOR_ACTION, val, false));
        rulesList.add(new WatchDogRule("8", mActiveAdapterId, "Hlídání cen", dev, WatchDogRule.OperatorType.SMALLER, WatchDogRule.ActionType.ACTOR_ACTION, val, false));
        rulesList.add(new WatchDogRule("9", mActiveAdapterId, "Hlídání cen", dev, WatchDogRule.OperatorType.SMALLER, WatchDogRule.ActionType.ACTOR_ACTION, val, false));
        rulesList.add(new WatchDogRule("10", mActiveAdapterId, "Hlídání cen", dev, WatchDogRule.OperatorType.SMALLER, WatchDogRule.ActionType.ACTOR_ACTION, val, false));
        mWatchDogAdapter = new WatchDogListAdapter(mActivity, rulesList, getActivity().getLayoutInflater());

        //*/
    }

    class ActionModeEditRules implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            MenuInflater inflater = actionMode.getMenuInflater();
            inflater.inflate(R.menu.watchdoglist_actionmode, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            /*if (menuItem.getTitle().equals(getResources().getString(R.string.action_hide_sensor))) {
                // doHideSensorTask(mDeviceHide);
            } else if (menuItem.getTitle().equals(getResources().getString(R.string.action_hide_facility))) {
				Toast.makeText(mActivity, R.string.toast_not_implemented, Toast.LENGTH_LONG).show();
            } else if (menuItem.getTitle().equals(getResources().getString(R.string.action_unregist_facility))) {
				Toast.makeText(mActivity, R.string.toast_not_implemented, Toast.LENGTH_LONG).show();
            }
            //*/
/*
            if(menuItem.getItemId() == R.id.action_delete){

                RemoveWatchDogTask task = new RemoveWatchDogTask(mActivity, false);
                task.setListener(new CallbackTask.CallbackTaskListener() {
                    @Override
                    public void onExecute(boolean success) {
                        Toast.makeText(mActivity, "Deleting watchdog...", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            task.execute(pair);

            //*/
            Toast.makeText(mActivity, "Deleting watchdog...", Toast.LENGTH_SHORT).show();


            actionMode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            mMode = null;
        }
    }

}
