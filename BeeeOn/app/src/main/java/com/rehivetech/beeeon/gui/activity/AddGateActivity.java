package com.rehivetech.beeeon.gui.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gui.adapter.ImageTextPair;
import com.rehivetech.beeeon.gui.adapter.IntroFragmentPagerAdapter;
import com.rehivetech.beeeon.gui.fragment.AddGateFragment;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.threading.CallbackTask.ICallbackTaskListener;
import com.rehivetech.beeeon.threading.task.RegisterGateTask;
import com.rehivetech.beeeon.util.Log;

import java.util.ArrayList;
import java.util.List;

public class AddGateActivity extends AddGadgetActivity implements AddGateFragment.OnAddGateListener {
	private static final String TAG = AddGateActivity.class.getSimpleName();
	private ProgressDialog mProgress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// creating list of objects that will be used as params for the constructor of AddingUniversalFragment
		List<ImageTextPair> pairs = new ArrayList<ImageTextPair>();
		pairs.add(new ImageTextPair(R.drawable.beeeon_tutorial_aa_second_step,getResources().getString(R.string.tut_add_gate_text_1)));
		pairs.add(new ImageTextPair(R.drawable.beeeon_tutorial_aa_first_step, getResources().getString(R.string.tut_add_gate_text_2)));
		pairs.add(new ImageTextPair(R.drawable.beeeon_tutorial_aa_third_step, getResources().getString(R.string.tut_add_gate_text_3)));

		// FragmentManager object is necessary for the contructor of Adding....
		FragmentManager fm = getSupportFragmentManager();
		mPagerAdapter = new IntroFragmentPagerAdapter(fm,pairs,new AddGateFragment());

		super.onCreate(savedInstanceState);

		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		if (mToolbar != null) {
			mToolbar.setTitle(R.string.title_activity_add_gate);
			setSupportActionBar(mToolbar);
		}

		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// Get controller
		mController = Controller.getInstance(this);

		mIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			}

			@Override
			public void onPageSelected(int position) {
				if (position == mPagerAdapter.getCount() - 1) {
					mSkip.setVisibility(View.INVISIBLE);
					mNext.setText(AddGateActivity.this.getString(R.string.tutorial_add));
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
				} else {
					mSkip.setVisibility(View.VISIBLE);
					mNext.setText(AddGateActivity.this.getString(R.string.tutorial_next));
				}
			}

			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});

		initButtons();

		// Prepare progress dialog
		mProgress = new ProgressDialog(this);
		mProgress.setMessage(getString(R.string.progress_saving_data));
		mProgress.setCancelable(false);
		mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	}

	@Override
	protected void initButtons() {
		super.initButtons();

		Cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SharedPreferences prefs = mController.getUserSettings();
				if (prefs != null) {
					prefs.edit().putBoolean(Constants.PERSISTENCE_PREF_IGNORE_NO_GATE, true).apply();
				}
				setResult(Constants.ADD_GATE_CANCELED);
				InputMethodManager imm = (InputMethodManager) AddGateActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
				finish();
			}
		});

		mNext.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mPager.getCurrentItem() == mPagerAdapter.getCount() - 1) {
					registerGate();
				} else {
					mPager.setCurrentItem(mPager.getCurrentItem() + 1);
				}
			}
		});


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

	public void doRegisterGateTask(Gate gate) {
		RegisterGateTask registerGateTask = new RegisterGateTask(this);

		registerGateTask.setListener(new ICallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				mProgress.cancel();

				if (success) {
					Toast.makeText(AddGateActivity.this, R.string.toast_adapter_activated, Toast.LENGTH_LONG).show();

					setResult(Constants.ADD_GATE_SUCCESS);
					InputMethodManager imm = (InputMethodManager) AddGateActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
					finish();
				}
			}
		});

		// Execute and remember task so it can be stopped automatically
		callbackTaskManager.executeTask(registerGateTask, gate);
	}

	@Override
	public void onCodeScanned() {
		// automatically clicks the next button
		registerGate();
	}

	public void registerGate() {
		AddGateFragment fragment = (AddGateFragment) mPagerAdapter.getFinalFragment();
		if (fragment == null) {
			return;
		}

		String gateName = fragment.getGateName();
		String gateCode = fragment.getGateCode();
		Log.d(TAG, String.format("Name: %s Code: %s", gateName, gateCode));

		if (gateCode.isEmpty()) {
			Toast.makeText(this, R.string.addadapter_fill_code, Toast.LENGTH_LONG).show();
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
