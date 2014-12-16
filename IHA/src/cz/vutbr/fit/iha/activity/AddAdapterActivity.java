package cz.vutbr.fit.iha.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.actionbarsherlock.view.MenuItem;
import com.viewpagerindicator.CirclePageIndicator;

import cz.vutbr.fit.iha.AddAdapterFragmentAdapter;
import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.activity.fragment.AddAdapterFragment;
import cz.vutbr.fit.iha.asynctask.CallbackTask.CallbackTaskListener;
import cz.vutbr.fit.iha.asynctask.RegisterAdapterTask;
import cz.vutbr.fit.iha.base.BaseApplicationActivity;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.pair.RegisterAdapterPair;
import cz.vutbr.fit.iha.thread.ToastMessageThread;
import cz.vutbr.fit.iha.util.Log;

public class AddAdapterActivity extends BaseApplicationActivity {
	private static final String TAG = AddAdapterActivity.class.getSimpleName();
	
	private Controller mController;
	
	private AddAdapterFragmentAdapter mAdapter;
	private ViewPager mPager;
	private CirclePageIndicator mIndicator;
	
	private AddAdapterFragment mFragment;
	
	private RegisterAdapterTask mRegisterAdapterTask;
	
	private Button mSkip;
	private Button mCancel;
	private Button mNext;
	

	private ProgressDialog mProgress;
	
	private Activity mActivity;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_intro);
		
		// Get controller
		mController = Controller.getInstance(this);
		
		mActivity = this;
		
		mAdapter = new AddAdapterFragmentAdapter(getSupportFragmentManager());

		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setIcon(R.drawable.ic_launcher_null);
		
		mPager = (ViewPager)findViewById(R.id.intro_pager);
		mPager.setAdapter(mAdapter);
		mPager.setOffscreenPageLimit(mAdapter.getCount());
		
		mIndicator = (CirclePageIndicator)findViewById(R.id.intro_indicator);
		mIndicator.setViewPager(mPager);
		
		mIndicator.setPageColor(0x88FFFFFF);
		mIndicator.setFillColor(0xFFFFFFFF);
		mIndicator.setStrokeColor(0x88FFFFFF);
		
		initButtons();
		
		// Prepare progress dialog
		mProgress = new ProgressDialog(mActivity);
		mProgress.setMessage(getString(R.string.progress_saving_data));
		mProgress.setCancelable(false);
		mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	}
	
	
	private void initButtons() {
		mSkip = (Button) findViewById(R.id.add_adapter_skip);
		mCancel = (Button) findViewById(R.id.add_adapter_cancel);
		mNext = (Button) findViewById(R.id.add_adapter_next);
		
		mSkip.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mPager.setCurrentItem(mAdapter.getCount()-1);
			}
		});
		
		mCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SharedPreferences prefs = mController.getUserSettings();
				if (prefs != null) {
					prefs.edit().putBoolean(Constants.PERSISTENCE_PREF_IGNORE_NO_ADAPTER, true).commit();
				}
				setResult(Constants.ADD_ADAPTER_CANCELED);
				finish();
			}
		});
		
		mNext.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mNext.getText().equals("NEXT")){
					mPager.setCurrentItem(mPager.getCurrentItem()+1);
				}
				else if (mNext.getText().equals("ADD")) {
					String adapterName = mFragment.getAdapterName();
					String adapterCode = mFragment.getAdapterCode();
					Log.d(TAG, "adaName: "+adapterName+" adaCode: "+adapterCode);
					
					if(adapterCode.equals("")){
						// TODO: Please fill AdapterCode
						
					}
					else {
						// Show progress bar for saving
						mProgress.show();
						doRegisterAdapterTask(new RegisterAdapterPair(adapterCode, adapterName));
					}
				}
			}
		});
		
		
	}
	
	public void setBtnLastPage() {
		mSkip.setVisibility(View.INVISIBLE);
		mNext.setText("ADD");
	}


	@Override
	protected void onAppResume() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onAppPause() {
		// TODO Auto-generated method stub

	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			SharedPreferences prefs = mController.getUserSettings();
			if (prefs != null) {
				prefs.edit().putBoolean(Constants.PERSISTENCE_PREF_IGNORE_NO_ADAPTER, true).commit();
			}
			setResult(Constants.ADD_ADAPTER_CANCELED);
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}


	public void resetBtn() {
		mSkip.setVisibility(View.VISIBLE);
		mNext.setText("NEXT");
	}


	public void setFragment(AddAdapterFragment addAdapterFragment) {
		mFragment = addAdapterFragment;
	}
	
	public void doRegisterAdapterTask(RegisterAdapterPair pair) {
		mRegisterAdapterTask = new RegisterAdapterTask(this.getApplicationContext());

		mRegisterAdapterTask.setListener(new CallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				mProgress.cancel();
				int messageId = success ? R.string.toast_adapter_activated : R.string.toast_adapter_activate_failed;
				//Log.d(TAG, this.getString(messageId));
				new ToastMessageThread(mActivity, messageId).start();
				
				if (success) {
					setResult(Constants.ADD_ADAPTER_SUCCESS);
					finish();
				}
			}
		});

		mRegisterAdapterTask.execute(pair);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();

		if (mRegisterAdapterTask != null) {
			mRegisterAdapterTask.cancel(true);
		}
	}

}
