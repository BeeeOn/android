package com.rehivetech.beeeon.gui.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.rehivetech.beeeon.AddGateFragmentAdapter;
import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.gui.fragment.AddGateFragment;
import com.rehivetech.beeeon.asynctask.CallbackTask.CallbackTaskListener;
import com.rehivetech.beeeon.asynctask.RegisterGateTask;
import com.rehivetech.beeeon.base.BaseApplicationActivity;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.util.Log;
import com.viewpagerindicator.CirclePageIndicator;

public class AddGateActivity extends BaseApplicationActivity {
	private static final String TAG = AddGateActivity.class.getSimpleName();

	private Controller mController;

	private AddGateFragmentAdapter mGate;
	private ViewPager mPager;
	private CirclePageIndicator mIndicator;

	private AddGateFragment mFragment;

	private Button mSkip;
	private Button mCancel;
	private Button mNext;


	private ProgressDialog mProgress;

	private Activity mActivity;
	private Toolbar mToolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_intro);

		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		if (mToolbar != null) {
			mToolbar.setTitle(R.string.title_activity_add_gate);
			setSupportActionBar(mToolbar);
		}
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		// Get controller
		mController = Controller.getInstance(this);

		mActivity = this;

		mGate = new AddGateFragmentAdapter(getSupportFragmentManager(), mActivity);


		mPager = (ViewPager) findViewById(R.id.intro_pager);
		mPager.setAdapter(mGate);
		mPager.setOffscreenPageLimit(mGate.getCount());

		mIndicator = (CirclePageIndicator) findViewById(R.id.intro_indicator);
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
		mSkip = (Button) findViewById(R.id.add_gate_skip);
		mCancel = (Button) findViewById(R.id.add_gate_cancel);
		mNext = (Button) findViewById(R.id.add_gate_next);

		mSkip.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mPager.setCurrentItem(mGate.getCount() - 1);
			}
		});

		mCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SharedPreferences prefs = mController.getUserSettings();
				if (prefs != null) {
					prefs.edit().putBoolean(Constants.PERSISTENCE_PREF_IGNORE_NO_GATE, true).apply();
				}
				setResult(Constants.ADD_GATE_CANCELED);
				InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
				finish();
			}
		});

		mNext.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mNext.getText().equals(mActivity.getString(R.string.tutorial_next))) {
					mPager.setCurrentItem(mPager.getCurrentItem() + 1);
				} else if (mNext.getText().equals(mActivity.getString(R.string.tutorial_add))) {
					String gateName = mFragment.getGateName();
					String gateCode = mFragment.getGateCode();
					Log.d(TAG, "adaName: " + gateName + " adaCode: " + gateCode);

					if (gateCode.isEmpty()) {
						// TODO: Please fill AdapterCode
						Toast.makeText(mActivity, R.string.addadapter_fill_code, Toast.LENGTH_LONG).show();
					} else {
						// Show progress bar for saving
						mProgress.show();
						Gate gate = new Gate();
						gate.setId(gateCode);
						gate.setName(gateName);
						doRegisterGateTask(gate);
					}
				}
			}
		});


	}

	public void setBtnLastPage() {
		mSkip.setVisibility(View.INVISIBLE);
		mNext.setText(mActivity.getString(R.string.tutorial_add));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case android.R.id.home:
				SharedPreferences prefs = mController.getUserSettings();
				if (prefs != null) {
					prefs.edit().putBoolean(Constants.PERSISTENCE_PREF_IGNORE_NO_GATE, true).apply();
				}
				setResult(Constants.ADD_GATE_CANCELED);
				finish();
				break;
		}
		return super.onOptionsItemSelected(item);
	}


	public void resetBtn() {
		mSkip.setVisibility(View.VISIBLE);
		mNext.setText(mActivity.getString(R.string.tutorial_next));
	}


	public void setFragment(AddGateFragment addGateFragment) {
		mFragment = addGateFragment;
	}

	public void doRegisterGateTask(Gate gate) {
		RegisterGateTask registerGateTask = new RegisterGateTask(this);

		registerGateTask.setListener(new CallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				mProgress.cancel();

				if (success) {
					Toast.makeText(mActivity, R.string.toast_adapter_activated, Toast.LENGTH_LONG).show();

					setResult(Constants.ADD_GATE_SUCCESS);
					InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
					finish();
				}
			}
		});

		// Execute and remember task so it can be stopped automatically
		callbackTaskManager.executeTask(registerGateTask, gate);
	}

}
