package cz.vutbr.fit.iha.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

import cz.vutbr.fit.iha.R;

public class ListOfDevices extends SherlockFragment {
	
	private static final String TAG = ListOfDevices.class.getSimpleName();
	public static boolean ready = false;
	private SwipeRefreshLayout mSwipeLayout;
	private LocationScreenActivity mActivity;

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		ready = false;
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.listofsensors, container, false);
		return view;
	}
	
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		ready = true;
		
		mActivity = (LocationScreenActivity) getActivity();
		// Init swipe-refreshig layout
		mSwipeLayout = (SwipeRefreshLayout) mActivity.findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				mActivity.refreshListing();
				isFinish();
			}
		});
        mSwipeLayout.setColorScheme(R.color.iha_separator, 
                R.color.iha_item_bg, 
                R.color.iha_secundary_pink,
                R.color.iha_text_color);
	}
	
	public void onPause(){
		super.onPause();
		ready = false;
	}
	
	public void onResume(){
		super.onResume();
		ready = true;
	}
	
	private void isFinish() {
		if(mActivity.isRefreshListingDone()) { // is FINISH REFRESH
			 mSwipeLayout.setRefreshing(false);
			 Log.d(TAG, "Refresh listing finish");
			 return;
		}
		// NOT FINISH REFRESH
		new Handler().postDelayed(new Runnable() {
            @Override public void run() {
            Log.d(TAG, "Refresh listing not finish - again");
               isFinish();
            }
        }, 1000);
	}

}