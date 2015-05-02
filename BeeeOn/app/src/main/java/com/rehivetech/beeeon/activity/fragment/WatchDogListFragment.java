package com.rehivetech.beeeon.activity.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.activity.MainActivity;
import com.rehivetech.beeeon.activity.WatchDogEditRuleActivity;
import com.rehivetech.beeeon.arrayadapter.WatchDogListAdapter;
import com.rehivetech.beeeon.asynctask.CallbackTask;
import com.rehivetech.beeeon.asynctask.ReloadAdapterDataTask;
import com.rehivetech.beeeon.asynctask.RemoveWatchDogTask;
import com.rehivetech.beeeon.asynctask.SaveWatchDogTask;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.watchdog.WatchDog;
import com.rehivetech.beeeon.pair.DelWatchDogPair;
import com.rehivetech.beeeon.util.Log;

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
    private Button mRefreshBtn;

    List<WatchDog> mWatchDogs;

    private String mActiveAdapterId;

    private View mView;
    private ActionMode mMode;
    ProgressBar mProgressBar;

    private ReloadAdapterDataTask mReloadWatchDogTask;
    private RemoveWatchDogTask mRemoveWatchDogTask;
    private SaveWatchDogTask mSaveWatchDogTask;

    private WatchDog mSelectedItem;
    private int mSelectedItemPos;

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

        if (savedInstanceState != null) {
            mActiveAdapterId = savedInstanceState.getString(ADAPTER_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        mView = inflater.inflate(R.layout.fragment_watchdog, container, false);
        return mView;
    }

    /**
     * Init layout
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated()");

        mProgressBar = (ProgressBar) mActivity.findViewById(R.id.toolbar_progress);
        mSwipeLayout = (SwipeRefreshLayout) mActivity.findViewById(R.id.swipe_container);

        initLayout();

        // Init swipe-refreshig layout
        if (mSwipeLayout == null) {
            return;
        }
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                refreshListListener();
            }
        });

        mSwipeLayout.setColorSchemeColors(R.color.beeeon_primary_cyan, R.color.beeeon_text_color, R.color.beeeon_secundary_pink);
    }

    private void refreshListListener() {
        Adapter adapter = mController.getActiveAdapter();
        if (adapter == null) {
            mSwipeLayout.setRefreshing(false);
            return;
        }
        doReloadWatchDogsTask(adapter.getId(), true, true);
    }

    @Override
    public void onResume() {
        super.onResume();
        mController = Controller.getInstance(mActivity);

        if(mActiveAdapterId == null){
            Adapter adapter = mController.getActiveAdapter();
            if(adapter == null)
                return;
            mActiveAdapterId = adapter.getId();
        }

        // if we don't have any data first time shows button to refresh
        redrawRules();
        // try to reload data
        doReloadWatchDogsTask(mActiveAdapterId, false, false);
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

        // always hide progressbar
        mProgressBar.setVisibility(View.GONE);
    }

    /**
     * Cancels async task before destroing fragment
     */
    public void onDestroy(){
        super.onDestroy();
        Log.d(TAG, "onDestroy()");

        if (mReloadWatchDogTask != null) {
            mReloadWatchDogTask.cancel(true);
        }

        if(mRemoveWatchDogTask != null){
            mRemoveWatchDogTask.cancel(true);
        }

        if(mSaveWatchDogTask != null){
            mSaveWatchDogTask.cancel(true);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putString(ADAPTER_ID, mActiveAdapterId);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Sets empty elements for design without filling with data
     */
    private void initLayout() {
        mWatchDogListView = (ListView) mView.findViewById(R.id.watchdog_list);
        mWatchDogAdapter = new WatchDogListAdapter(mActivity, getActivity().getLayoutInflater());
        mWatchDogListView.setAdapter(mWatchDogAdapter);

        // onclicklistener for Switch button in one row
        mWatchDogAdapter.setSwitchOnclickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int objPosition = (int) v.getTag();
                WatchDog watchDog = (WatchDog) mWatchDogAdapter.getItem(objPosition);
                if (watchDog == null) return;

                // so that progress bar can be seen
                if (mMode != null) mMode.finish();

                SwitchCompat sw = (SwitchCompat) v;
                doSaveWatchDogTask(watchDog, sw);
            }
        });

        // when listview is empty
        TextView emptyView = (TextView) mView.findViewById(R.id.watchdog_list_empty);
        mWatchDogListView.setEmptyView(emptyView);

        // refresh button
        mRefreshBtn = (Button) mView.findViewById(R.id.watchdog_list_refresh_btn);
        mRefreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshListListener();
            }
        });

        // add new watchdog rule
        FloatingActionButton fab = (FloatingActionButton) mView.findViewById(R.id.fab);
        fab.attachToListView(mWatchDogListView);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, WatchDogEditRuleActivity.class);
                startActivity(intent);
            }
        });

        // switch activity to detail
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

        // shows actionMode with delete option
        mWatchDogListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                mMode = mActivity.startSupportActionMode(new ActionModeEditRules());
                mSelectedItem = mWatchDogAdapter.getRule(position);
                mSelectedItemPos = position;
                setRuleSelected();
                return true;
            }
        });
    }

    /**
     * Redraw GUI rules, called asynchronously (callback) when new data available
     */
    private void redrawRules() {
        mWatchDogs = mController.getWatchDogsModel().getWatchDogsByAdapter(mActiveAdapterId);

        boolean haveWatchDogs = mWatchDogs.size() > 0;

        if(!haveWatchDogs) {
            mRefreshBtn.setVisibility(View.VISIBLE);

            mWatchDogListView.setVisibility(View.GONE);
            if(mSwipeLayout != null){
                mSwipeLayout.setVisibility(View.GONE);
            }
        }
        else{
            mRefreshBtn.setVisibility(View.GONE);
            mWatchDogListView.setVisibility(View.VISIBLE);
            if (mSwipeLayout != null) {
                mSwipeLayout.setVisibility(View.VISIBLE);
            }
        }

        mWatchDogAdapter.updateData(mWatchDogs);
    }

    // ----- ASYNC TASKS ----- //
    private void doSaveWatchDogTask(WatchDog watchDog, final SwitchCompat sw){
        // disable so that nobody can change it now
        sw.setEnabled(false);
        //Make progress bar appear when you need it
        mProgressBar.setVisibility(View.VISIBLE);
        // other option is to set Swipe refreshing
        //mSwipeLayout.setRefreshing(true);

        watchDog.setEnabled(sw.isChecked());

        mSaveWatchDogTask = new SaveWatchDogTask(mActivity);
        mSaveWatchDogTask.setListener(new CallbackTask.CallbackTaskListener() {
            @Override
            public void onExecute(boolean success) {
                //Toast.makeText(mActivity, getResources().getString(success ? R.string.toast_success_save_data : R.string.toast_fail_save_data), Toast.LENGTH_LONG).show();
                sw.setEnabled(true);
                //Make progress bar disappear
                mProgressBar.setVisibility(View.GONE);
                // other option is to set Swipe refreshing
                //mSwipeLayout.setRefreshing(false);
            }
        });

        mSaveWatchDogTask.execute(watchDog);
    }

    /**
     * Async task for reloading fresh watchdog data
     * @param adapterId
     */
    public void doReloadWatchDogsTask(String adapterId, boolean forceReload, final boolean isSwipeRefresh){
        Log.d(TAG, "reloadWatchDogsTask()");

        mReloadWatchDogTask = new ReloadAdapterDataTask(mActivity, forceReload, ReloadAdapterDataTask.ReloadWhat.WATCHDOGS);

        mReloadWatchDogTask.setListener(new CallbackTask.CallbackTaskListener() {
            @Override
            public void onExecute(boolean success) {
                redrawRules();
                if(isSwipeRefresh)
                    mSwipeLayout.setRefreshing(false);
                else
                    mActivity.setBeeeOnProgressBarVisibility(false);
            }
        });

        if(!isSwipeRefresh) {
            mActivity.setBeeeOnProgressBarVisibility(true);
        }
        mReloadWatchDogTask.execute(adapterId);
    }

    /**
     * Async task for deleting watchDog
     * @param watchdog
     */
    private void doRemoveWatchDogTask(WatchDog watchdog) {
        mRemoveWatchDogTask = new RemoveWatchDogTask(mActivity, false);
        DelWatchDogPair pair = new DelWatchDogPair(watchdog.getId(), watchdog.getAdapterId());

        mRemoveWatchDogTask.setListener(new CallbackTask.CallbackTaskListener() {
            @Override
            public void onExecute(boolean success) {
                Toast.makeText(mActivity, getResources().getString(success ? R.string.toast_delete_success : R.string.toast_delete_fail), Toast.LENGTH_SHORT).show();
                if (success) {
                    redrawRules();
                }
            }
        });
        mRemoveWatchDogTask.execute(pair);
    }

    // ----- HELPERS + ACTIONMODE ----- //

    /**
     * Changes color of selected item row
     */
    private void setRuleSelected() {
        getViewByPosition(mSelectedItemPos, mWatchDogListView).findViewById(R.id.watchdog_item_layout).setBackgroundColor(mActivity.getResources().getColor(R.color.light_gray));
    }
    private void setRuleUnselected() {
        getViewByPosition(mSelectedItemPos, mWatchDogListView).findViewById(R.id.watchdog_item_layout).setBackgroundColor(mActivity.getResources().getColor(R.color.white));
    }

    /**
     * Helper for getting item from listView
     * @param pos
     * @param listView
     * @return
     */
    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    /**
     * Class for managing when longclicked on item (ActionMode)
     */
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
            if(menuItem.getItemId() == R.id.action_delete){
                doRemoveWatchDogTask(mSelectedItem);
            }
            
            actionMode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            setRuleUnselected();
            mSelectedItem = null;
            mSelectedItemPos = 0;
            mMode = null;
        }
    }
}
