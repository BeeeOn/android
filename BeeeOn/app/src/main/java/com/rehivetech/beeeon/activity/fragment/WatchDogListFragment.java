package com.rehivetech.beeeon.activity.fragment;

import android.content.Intent;
import android.graphics.Rect;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tomáš on 23. 2. 2015.
 */
public class WatchDogListFragment extends SherlockFragment{
    private static final String TAG = WatchDogListFragment.class.getSimpleName();

    private static final String ADAPTER_ID = "lastAdapterId";

    private MainActivity mActivity;
    private Controller mController;
    private ListView mWatchDogListView;
    private WatchDogListAdapter mWatchDogAdapter;

    private String mActiveAdapterId;

    private View mView;

    // mod actionbaru
    private ActionMode mMode;

    /**
     * Initialize variables
     * @param savedInstanceState
     */
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        if (!(getSherlockActivity() instanceof MainActivity)) {
            throw new IllegalStateException("Activity holding SensorListFragment must be MainActivity");
        }

        mActivity = (MainActivity) getSherlockActivity();
        mController = Controller.getInstance(mActivity);

        if (savedInstanceState != null) {
            mActiveAdapterId = savedInstanceState.getString(ADAPTER_ID);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        mView = inflater.inflate(R.layout.fragment_watchdog, container, false);
        redrawRules();
        return mView;
    }

    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
    }

    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");

        if(mMode != null) {
            mMode.finish();
        }
    }

    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putString(ADAPTER_ID, mActiveAdapterId);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void redrawRules() {
        mWatchDogListView = (ListView) mView.findViewById(R.id.watchdogListView);

        // pokusny seznam
        List<WatchDogListAdapter.WRule> rulesList = new ArrayList<>();
        rulesList.add(new WatchDogListAdapter.WRule("První senzor", WatchDogListAdapter.OperatorType.GREATER, WatchDogListAdapter.ActionType.ACTOR_ACTION, "30°C", true));
        rulesList.add(new WatchDogListAdapter.WRule("Druhý senzor", WatchDogListAdapter.OperatorType.SMALLER, WatchDogListAdapter.ActionType.NOTIFICATION, "90%", true));
        rulesList.add(new WatchDogListAdapter.WRule("Třetí senzor", WatchDogListAdapter.OperatorType.SMALLER, WatchDogListAdapter.ActionType.ACTOR_ACTION, "60LUX", false));
        rulesList.add(new WatchDogListAdapter.WRule("Čtvrtý senzor", WatchDogListAdapter.OperatorType.GREATER, WatchDogListAdapter.ActionType.NOTIFICATION, "30°C", true));

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
            private Rect rect;
            public boolean onTouch(View v, MotionEvent event) {
                ImageView img = (ImageView) v;
                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        img.setImageResource(R.drawable.ic_add_pressed);
                        rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                        break;

                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_OUTSIDE:
                        img.setImageResource(R.drawable.ic_add);
                        break;

                    case MotionEvent.ACTION_UP:
                        img.setImageResource(R.drawable.ic_add);
                        // this way, cause some devices don't accept ACTION_CANCEL
                        if (rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) {
                            // zapne aktivitu
                            Intent intent = new Intent(mActivity, WatchDogDetailActivity.class);
                            startActivity(intent);
                            // v.performClick(); // is possible to create default OnClickListener
                        }
                        break;
                    default:
                        return v.onTouchEvent(event);
                }
                return true;
            }
        });
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
