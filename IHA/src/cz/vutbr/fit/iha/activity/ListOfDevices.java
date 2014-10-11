package cz.vutbr.fit.iha.activity;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.asynctask.CallbackTask.CallbackTaskListener;
import cz.vutbr.fit.iha.asynctask.ReloadFacilitiesTask;
import cz.vutbr.fit.iha.controller.Controller;

public class ListOfDevices extends SherlockFragment {

	private static final String TAG = ListOfDevices.class.getSimpleName();
	public static boolean ready = false;
	private SwipeRefreshLayout mSwipeLayout;
	private LocationScreenActivity mActivity;
	private Controller mController;
	private ReloadFacilitiesTask mReloadFacilitiesTask;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate()");
		ready = false;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.listofsensors, container, false);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d(TAG, "onActivityCreated()");
		ready = true;

		mActivity = (LocationScreenActivity) getActivity();
		mController = Controller.getInstance(mActivity.getApplicationContext());
		
		// Init swipe-refreshig layout
		mSwipeLayout = (SwipeRefreshLayout) mActivity.findViewById(R.id.swipe_container);
		mSwipeLayout.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				
				Adapter adapter = mController.getActiveAdapter();
				if (adapter == null) {
					mSwipeLayout.setRefreshing(false);
					return;
				}
				
				doReloadFacilitiesTask(adapter.getId());
			}
		});
		mSwipeLayout.setColorScheme(R.color.iha_separator, R.color.iha_item_bg, R.color.iha_secundary_pink, R.color.iha_text_color);
	}
	
	public void onPause() {
		super.onPause();
		Log.d(TAG, "onPause()");
		ready = false;
	}

	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume()");
		ready = true;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy()");
		
		ready = false;

		if (mReloadFacilitiesTask != null) {
			mReloadFacilitiesTask.cancel(true);
		}
	}
	
	private void doReloadFacilitiesTask(String adapterId) {
		mReloadFacilitiesTask = new ReloadFacilitiesTask(getActivity().getApplicationContext(), true);
		
		mReloadFacilitiesTask.setListener(new CallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				mActivity.redrawDevices();
				mSwipeLayout.setRefreshing(false);
			}
		});
		
		mReloadFacilitiesTask.execute(adapterId);
	}
	



}